// Optional build-time tool: sharp 0.34.5. The app consumes only the compiled WebP cels.
import {createRequire} from 'node:module';
import {fileURLToPath} from 'node:url';
import path from 'node:path';
import {mkdir,readFile,writeFile,readdir} from 'node:fs/promises';
import {createHash} from 'node:crypto';
const require=createRequire(import.meta.url);
const sharp=require(process.env.SHARE_MODE_SHARP_MODULE||'sharp');
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const output=path.join(root,'android/app/src/main/assets/avatars');
const names=['portrait_custom_boy','portrait_custom_girl','portrait_seraph','portrait_sovereign','portrait_special','portrait_yugi',...['luffy','zoro','boa','shanks','eren','levi','mikasa','sukuna','itachi','rock_lee','might_guy','madara'].map(n=>'portrait_'+n),'madara_cast','madara_guardian','madara_eye','sukuna_cast','sukuna_shrine','sukuna_eye','zoro_ready','zoro_combo','zoro_finish','zoro_slashes'];
const manifest={format:1,cellSize:256,atlases:[],bytes:0,files:0};
await mkdir(output,{recursive:true});
for(const name of names){
 const input=path.join(root,'windows/art',name+'.png');const raw=await readFile(input);const {width,height}=await sharp(raw).metadata();
 if(width%4||height%(width/4))throw Error('Invalid shared atlas '+name);
 const cell=width/4,count=height/cell*4,folder=path.join(output,name);await mkdir(folder,{recursive:true});
 for(let index=0;index<count;index++){
  const inset=name.endsWith('_eye')?Math.floor(cell/10):0;
  const image=await sharp(raw).extract({left:index%4*cell+inset,top:Math.floor(index/4)*cell+inset,width:cell-inset*2,height:cell-inset*2}).resize(256,256,{kernel:'lanczos3'}).webp({quality:92,alphaQuality:100,effort:5,lossless:name.startsWith('portrait_custom_')}).toBuffer();
  await writeFile(path.join(folder,index+'.webp'),image);manifest.bytes+=image.length;manifest.files++;
 }
 if((await readdir(folder)).length!==count)throw Error('Unexpected stale cels in '+name);
 manifest.atlases.push({name,frames:count,sourceSha256:createHash('sha256').update(raw).digest('hex')});
}
await writeFile(path.join(output,'manifest.json'),JSON.stringify(manifest,null,2)+'\n');
console.log(JSON.stringify({atlases:names.length,cels:manifest.files,bytes:manifest.bytes,cellSize:manifest.cellSize,oldPixelArtCopied:false}));
