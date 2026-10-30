import fs from 'node:fs/promises';
import path from 'node:path';
import tls from 'node:tls';
import crypto from 'node:crypto';
import {execFileSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';

const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const args=process.argv.slice(2);
const option=name=>args.includes(name)?args[args.indexOf(name)+1]:null;
const serial=option('--serial'),image=option('--qr-image'),apk=option('--apk'),version=option('--version'),versionCode=option('--version-code');
if(!serial||!image||!apk||!version||!versionCode)throw Error('Required: --serial, --qr-image, --apk, --version, --version-code. Open Receive and capture its QR privately first.');
const adb=path.join(root,'.local/tools/android-sdk/platform-tools',process.platform==='win32'?'adb.exe':'adb');
const runAdb=argv=>execFileSync(adb,['-s',serial,...argv],{encoding:'utf8',windowsHide:true,timeout:20000});
const jdk=(await fs.readdir(path.join(root,'.local/tools/jdk17'),{withFileTypes:true})).find(x=>x.isDirectory());
if(!jdk)throw Error('Project-local JDK is missing');
const javaBin=path.join(root,'.local/tools/jdk17',jdk.name,'bin');
const executable=name=>path.join(javaBin,name+(process.platform==='win32'?'.exe':''));
const classes=path.join(root,'.local/qr-classes'),decoder=path.join(root,'.local/zxing-core.jar');
await fs.mkdir(classes,{recursive:true});
execFileSync(executable('javac'),['-cp',decoder,'-d',classes,path.join(root,'tests/DecodePairingQr.java')],{windowsHide:true,timeout:20000});
const text=execFileSync(executable('java'),['-Djava.awt.headless=true','-cp',classes+path.delimiter+decoder,'DecodePairingQr',path.resolve(image)],{encoding:'utf8',windowsHide:true,timeout:20000}).trim();
if(!text.startsWith('shareme-rowell://pair/'))throw Error('Not an application pairing code');
const pair=JSON.parse(Buffer.from(text.slice('shareme-rowell://pair/'.length),'base64url').toString());
const localHost=host=>/^(?:10\.|192\.168\.|172\.(?:1[6-9]|2\d|3[01])\.|169\.254\.)\d/.test(host)&&host.split('.').length===4&&host.split('.').every(part=>/^\d+$/.test(part)&&+part<=255);
if(pair.v!==1||pair.mode!=='receive'||!Array.isArray(pair.hosts)||!pair.hosts.length||pair.hosts.some(host=>!localHost(host))||!Number.isInteger(pair.port)||pair.port<1024||pair.port>65535||!/^[a-f0-9]{64}$/.test(pair.fp))throw Error('Unexpected receiver pairing details');
const installed=runAdb(['shell','cmd','package','list','packages','--show-versioncode','com.rowell.shareme']);
if(!installed.includes('versionCode:'+versionCode))throw Error('Installed version code differs from the requested build');
const apkPath=runAdb(['shell','pm','path','com.rowell.shareme']).split(/\r?\n/).find(line=>line.endsWith('/base.apk'))?.replace(/^package:/,'');
if(!apkPath)throw Error('Installed base APK not found');
const installedHash=runAdb(['shell','sha256sum',apkPath]).trim().split(/\s+/)[0];
const apkSha256=crypto.createHash('sha256').update(await fs.readFile(path.resolve(apk))).digest('hex');
if(installedHash!==apkSha256)throw Error('Installed APK differs from the requested file');
let migrated=null;
if(option('--previous-identity')){const previous=JSON.parse(await fs.readFile(option('--previous-identity'),'utf8'));migrated=previous.fingerprint!==pair.fp;if(!migrated)throw Error('Old incompatible TLS identity is still selected');}

async function check(host){return new Promise((resolve,reject)=>{
    let pinned=false,information,buffer='';
    const legacy=args.includes('--legacy-signature');
    const socket=tls.connect({host,port:pair.port,minVersion:'TLSv1.2',maxVersion:'TLSv1.2',rejectUnauthorized:false,...(legacy?{sigalgs:'RSA+SHA256',ciphers:'ECDHE-RSA-AES128-GCM-SHA256'}:{})});
    const timer=setTimeout(()=>{socket.destroy();reject(Error('Secure listener check timed out'));},10000);
    socket.once('secureConnect',()=>{
        const certificate=socket.getPeerCertificate(true);
        if(crypto.createHash('sha256').update(certificate.raw).digest('hex')!==pair.fp){socket.destroy();reject(Error('Certificate differs from pairing QR'));return;}
        const cert=new crypto.X509Certificate(certificate.raw),now=Date.now();
        if(now<Date.parse(cert.validFrom)||now>Date.parse(cert.validTo)){socket.destroy();reject(Error('Phone certificate date is invalid'));return;}
        pinned=true;information={protocol:socket.getProtocol(),cipher:socket.getCipher().standardName,signatureMode:legacy?'RSA PKCS1 SHA256':'TLS defaults'};
        // No valid pairing secret, file offer or payload is sent; no approval is requested.
        socket.write(JSON.stringify({v:1,op:'pull',code:'connection-check-invalid-code',name:'Connection check'})+'\n');
    });
    socket.on('data',chunk=>{
        buffer+=chunk.toString('utf8');if(buffer.length>65536){socket.destroy();reject(Error('Oversized listener response'));return;}
        const end=buffer.indexOf('\n');if(end<0)return;
        try{const reply=JSON.parse(buffer.slice(0,end));if(reply.ok!==false||!String(reply.error).includes('Pairing expired'))throw Error('Invalid pairing was not rejected');clearTimeout(timer);socket.end();resolve(information);}catch(error){socket.destroy();reject(error);}
    });
    socket.once('error',error=>{clearTimeout(timer);reject(error);});
    socket.once('close',()=>{clearTimeout(timer);reject(Error(pinned?'Listener closed before protocol response':'Connection closed before identity verification'));});
});}
let information,lastError;
for(const host of pair.hosts){try{information=await check(host);break;}catch(error){lastError=error;}}
if(!information)throw lastError;
const result={passed:true,phoneVersion:version,apkSha256,tlsIdentityMigrated:migrated,transport:'local Wi-Fi/hotspot',secureConnection:true,fingerprintMatched:true,invalidPairingRejected:true,...information,filesSent:0,filesCreated:0,verifiedAt:new Date().toISOString()};
const report=path.join(root,'.local/phone-verification',args.includes('--legacy-signature')?'secure-listener-check-pkcs1.json':'secure-listener-check.json');
await fs.writeFile(report,JSON.stringify(result,null,2)+'\n');
console.log(JSON.stringify(result));
