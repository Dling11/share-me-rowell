import assert from 'node:assert/strict';
import fs from 'node:fs/promises';
import path from 'node:path';
import tls from 'node:tls';
import crypto from 'node:crypto';
import {spawn} from 'node:child_process';
import {fileURLToPath} from 'node:url';

const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const work=path.join(root,'.local',`integration-${Date.now()}`);
await fs.mkdir(work,{recursive:true});
const servers=[];
const hash=buffer=>crypto.createHash('sha256').update(buffer).digest('hex');
const pause=ms=>new Promise(r=>setTimeout(r,ms));
async function card(info,phase){let last;for(let attempt=0;attempt<150;attempt++){try{last=JSON.parse(await fs.readFile(path.join(info.folder,'connection.json'),'utf8'));if(last.phase===phase)return last;}catch{}await pause(20);}throw new Error(`Expected connection card ${phase}, received ${JSON.stringify(last)}`);}
async function server(mode='receive',files=[]){
  const folder=path.join(work,`server-${servers.length}`);await fs.mkdir(folder);await fs.writeFile(path.join(folder,'test-installer.apk'),Buffer.from('Dummy APK bootstrap fixture'));
  const process=spawn(path.join(root,'dist','Windows','Share-Mode.exe'),['--test-server',folder,...(mode==='send'?['send',...files]:[])],{windowsHide:true,stdio:'ignore'});
  servers.push({process,folder});
  for(let i=0;i<150;i++){
    try{const info=JSON.parse(await fs.readFile(path.join(folder,'server.json'),'utf8'));return {...info,folder};}catch{}
    try{throw new Error(await fs.readFile(path.join(folder,'error.txt'),'utf8'));}catch(e){if(e.code!=='ENOENT')throw e;}
    await pause(100);
  }
  throw new Error('Test server did not start');
}
class Reader {
  constructor(socket){this.socket=socket;this.buffer=Buffer.alloc(0);this.waiters=[];this.error=null;socket.on('data',data=>{this.buffer=Buffer.concat([this.buffer,data]);this.wake();});socket.on('error',e=>{this.error=e;this.wake();});socket.on('close',()=>{this.error??=new Error('Connection closed');this.wake();});}
  wake(){for(const done of this.waiters.splice(0))done();}
  async more(){if(this.error)throw this.error;await new Promise((resolve,reject)=>{const timer=setTimeout(()=>{this.waiters=this.waiters.filter(x=>x!==done);reject(new Error('Read timed out'));},20000);const done=()=>{clearTimeout(timer);resolve();};this.waiters.push(done);});}
  async line(){while(true){const end=this.buffer.indexOf(10);if(end>=0){const line=this.buffer.subarray(0,end);this.buffer=this.buffer.subarray(end+1);return JSON.parse(line.toString());}await this.more();}}
  async bytes(count){const chunks=[];let left=count;while(left>0){if(!this.buffer.length){await this.more();continue;}const n=Math.min(left,this.buffer.length);chunks.push(this.buffer.subarray(0,n));this.buffer=this.buffer.subarray(n);left-=n;}return Buffer.concat(chunks);}
}
async function connection(info){
  const socket=tls.connect({host:'127.0.0.1',port:info.port,rejectUnauthorized:false,minVersion:'TLSv1.2',maxVersion:'TLSv1.2'});
  await new Promise((resolve,reject)=>{socket.once('secureConnect',resolve);socket.once('error',reject);});
  assert.equal(hash(socket.getPeerCertificate().raw),info.fp,'TLS identity matches pairing code');
  return {socket,reader:new Reader(socket)};
}
function line(socket,value){socket.write(JSON.stringify(value)+'\n');}
async function push(info,name,data,{code=info.code,claimedHash=hash(data)}={}){
  const {socket,reader}=await connection(info);
  line(socket,{v:1,op:'push',code,name:'Protocol test',files:[{id:crypto.randomUUID(),name,size:data.length,sha256:claimedHash}]});
  const accepted=await reader.line();if(!accepted.ok){socket.destroy();return accepted;}
  socket.write(data.subarray(accepted.offsets[0]));
  const ack=await reader.line();socket.end();await pause(100);return ack;
}
const passed=[];
try{
  const receiver=await server();
  const page=await fetch(receiver.installerUrl);assert.equal(page.status,200);assert.match(await page.text(),/Download Android APK/);
  const download=await fetch(receiver.installerUrl+'/app.apk');assert.equal(download.status,200);assert.equal(download.headers.get('content-type'),'application/vnd.android.package-archive');assert.equal(Buffer.from(await download.arrayBuffer()).toString(),'Dummy APK bootstrap fixture');passed.push('local APK page and exact installer bytes');
  for(const suffix of ['/wrong-token','/../server.json','/../test-installer.apk']){const result=await fetch(new URL(suffix,receiver.installerUrl));assert.equal(result.status,404);}passed.push('installer endpoint only serves token-scoped public APK');
  const data=crypto.randomBytes(1024*1024+13);
  assert.equal((await push(receiver,'test.iso',data)).ok,true);
  assert.equal(hash(await fs.readFile(path.join(receiver.folder,'received','test.iso'))),hash(data));passed.push('binary-ISO round trip and checksum');
  let status=await card(receiver,'Finished');assert.equal(status.peer,'Protocol test');assert.equal(status.active,false);assert.match(status.label,/disconnected/i);passed.push('finished device card retains peer without claiming connection');
  assert.equal((await push(receiver,'empty.txt',Buffer.alloc(0))).ok,true);
  assert.equal((await fs.stat(path.join(receiver.folder,'received','empty.txt'))).size,0);passed.push('empty file');
  assert.equal((await push(receiver,'denied.txt',data,{code:'wrong-code'})).ok,false);
  await assert.rejects(fs.stat(path.join(receiver.folder,'received','denied.txt')));passed.push('invalid pairing rejected');
  assert.equal((await push(receiver,'damaged.apk',data,{claimedHash:'0'.repeat(64)})).ok,false);
  await assert.rejects(fs.stat(path.join(receiver.folder,'received','damaged.apk')));passed.push('checksum failure never published');
  assert.equal((await card(receiver,'Failed')).active,false);passed.push('verification failure closes active device status');
  const resumable=crypto.randomBytes(2*1024*1024+7);const meta={id:'resume',name:'resume.zip',size:resumable.length,sha256:hash(resumable)};
  let peer=await connection(receiver);line(peer.socket,{v:1,op:'push',code:receiver.code,name:'Test',files:[meta]});assert.equal((await peer.reader.line()).offsets[0],0);peer.socket.write(resumable.subarray(0,524288));await pause(300);peer.socket.destroy();await pause(500);
  peer=await connection(receiver);line(peer.socket,{v:1,op:'push',code:receiver.code,name:'Test',files:[meta]});const restart=await peer.reader.line();assert.equal(restart.ok,true);assert(restart.offsets[0]>0&&restart.offsets[0]<resumable.length);peer.socket.write(resumable.subarray(restart.offsets[0]));assert.equal((await peer.reader.line()).ok,true);peer.socket.end();await pause(150);
  assert.equal(hash(await fs.readFile(path.join(receiver.folder,'received','resume.zip'))),meta.sha256);passed.push('disconnect and resume');
  const cancelData=crypto.randomBytes(3*1024*1024+9);const cancelMeta={id:'cancel',name:'cancel-resume.bin',size:cancelData.length,sha256:hash(cancelData)};
  peer=await connection(receiver);line(peer.socket,{v:1,op:'push',code:receiver.code,name:'POCO test device',files:[cancelMeta]});assert.equal((await peer.reader.line()).ok,true);peer.socket.write(cancelData.subarray(0,524288));await pause(150);assert.equal((await card(receiver,'Receiving')).peer,'POCO test device');
  assert.equal((await push(receiver,'intruder.txt',data,{code:'wrong-code'})).ok,false);assert.equal((await card(receiver,'Receiving')).peer,'POCO test device');passed.push('invalid pairing cannot overwrite the active device card');
  await fs.writeFile(path.join(receiver.folder,'cancel'),'cancel');status=await card(receiver,'Disconnected');assert.equal(status.active,false);await pause(150);peer.socket.destroy();await assert.rejects(fs.stat(path.join(receiver.folder,'received',cancelMeta.name)));
  peer=await connection(receiver);line(peer.socket,{v:1,op:'push',code:receiver.code,name:'POCO test device',files:[cancelMeta]});const resume=await peer.reader.line();assert.equal(resume.ok,true);assert(resume.offsets[0]>0);peer.socket.write(cancelData.subarray(resume.offsets[0]));assert.equal((await peer.reader.line()).ok,true);peer.socket.end();assert.equal((await card(receiver,'Finished')).peer,'POCO test device');assert.equal(hash(await fs.readFile(path.join(receiver.folder,'received',cancelMeta.name))),cancelMeta.sha256);passed.push('Disconnect stops transfer and preserves a resumable partial file');
  await fs.writeFile(path.join(receiver.folder,'decline'),'decline');assert.equal((await push(receiver,'declined.bin',data)).ok,false);assert.equal((await card(receiver,'Declined')).active,false);await fs.unlink(path.join(receiver.folder,'decline'));passed.push('declined approval shows a disconnected terminal status');
  await fs.writeFile(path.join(receiver.folder,'wait-approval'),'wait');peer=await connection(receiver);line(peer.socket,{v:1,op:'push',code:receiver.code,name:'Pending device',files:[{...cancelMeta,id:'pending',name:'pending.bin'}]});assert.equal((await card(receiver,'Approval')).peer,'Pending device');await fs.writeFile(path.join(receiver.folder,'cancel'),'cancel');assert.equal((await card(receiver,'Disconnected')).active,false);await fs.unlink(path.join(receiver.folder,'wait-approval'));peer.socket.destroy();await pause(150);passed.push('Disconnect cancels a pending approval without a stale active card');
  assert.equal((await push(receiver,'../../escape.txt',Buffer.from('safe'))).ok,true);await assert.rejects(fs.stat(path.join(receiver.folder,'escape.txt')));passed.push('filename traversal contained');
  assert.equal((await push(receiver,'test.iso',data)).ok,true);assert.equal(hash(await fs.readFile(path.join(receiver.folder,'received','test (2).iso'))),hash(data));passed.push('existing files preserved');
  peer=await connection(receiver);line(peer.socket,{v:1,op:'push',code:receiver.code,name:'Test',files:[{id:'same1',name:'same1.bin',size:data.length,sha256:hash(data)},{id:'same2',name:'same2.bin',size:data.length,sha256:hash(data)}]});assert.deepEqual((await peer.reader.line()).offsets,[0,0]);peer.socket.write(data);assert.equal((await peer.reader.line()).ok,true);peer.socket.write(data);assert.equal((await peer.reader.line()).ok,true);peer.socket.end();await pause(100);assert.equal(hash(await fs.readFile(path.join(receiver.folder,'received','same2.bin'))),hash(data));passed.push('identical content in a batch creates two complete files');
  peer=await connection(receiver);line(peer.socket,{v:1,op:'push',code:receiver.code,name:'Test',files:[{id:'large',name:'over-4GB.iso',size:5000000000,sha256:'a'.repeat(64)}]});assert.equal((await peer.reader.line()).ok,true);peer.socket.destroy();await pause(150);passed.push('64-bit size above 4GB');
  const source=path.join(work,'sender.bin');await fs.writeFile(source,data);const sender=await server('send',[source]);
  peer=await connection(sender);line(peer.socket,{v:1,op:'pull',code:sender.code,name:'Receiver test'});const offer=await peer.reader.line();assert.equal(offer.ok,true);assert.equal(offer.files[0].sha256,hash(data));assert(!JSON.stringify(offer).includes(work));assert.equal((await card(sender,'Waiting')).peer,'Receiver test');line(peer.socket,{ok:true,offsets:[111]});const suffix=await peer.reader.bytes(data.length-111);assert.deepEqual(suffix,data.subarray(111));line(peer.socket,{ok:true,index:0});peer.socket.end();assert.equal((await card(sender,'Finished')).active,false);passed.push('PC sender peer card, pull and resume offset');
  const report={passed:passed.length,checks:passed,phoneUsed:false,workingDirectory:work};await fs.writeFile(path.join(root,'.local','windows-integration.json'),JSON.stringify(report,null,2));console.log(JSON.stringify(report,null,2));
}finally{
  for(const item of servers){await fs.writeFile(path.join(item.folder,'stop'),'stop');}
  await pause(500);
  for(const item of servers)if(item.process.exitCode===null)item.process.kill();
}
