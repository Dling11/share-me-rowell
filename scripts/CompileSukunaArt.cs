using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;
using System.Runtime.InteropServices;

// Optional authoring utility; whole cels, original source alpha and no game-engine dependency.
class CompileSukunaArt {
    static void Compile(string root,string slug,int rows,int maximum){
        using(var source=new Bitmap(Path.Combine(root,"windows","art-source","sukuna-"+slug+"-source-1.2.11.png"))){
            if(source.Height*4!=source.Width*rows)throw new Exception("Unexpected cinematic grid: "+slug);
            int original=source.Width/4,size=Math.Min(maximum,original);
            using(var atlas=new Bitmap(size*4,size*rows,PixelFormat.Format32bppArgb)){
                var output=new int[atlas.Width*atlas.Height];
                for(int frame=0;frame<rows*4;frame++){
                    int left=(int)Math.Round(frame%4*source.Width/4.0),top=(int)Math.Round(frame/4*source.Height/(double)rows),right=(int)Math.Round((frame%4+1)*source.Width/4.0),bottom=(int)Math.Round((frame/4+1)*source.Height/(double)rows);var crop=new Rectangle(left,top,right-left,bottom-top);
                    using(var tile=crop.Width==size&&crop.Height==size?source.Clone(crop,PixelFormat.Format32bppArgb):new Bitmap(size,size,PixelFormat.Format32bppArgb)){
                    if(crop.Width!=size||crop.Height!=size)using(var g=Graphics.FromImage(tile)){g.CompositingMode=CompositingMode.SourceCopy;g.InterpolationMode=InterpolationMode.HighQualityBicubic;g.PixelOffsetMode=PixelOffsetMode.Half;using(var attributes=new ImageAttributes()){attributes.SetWrapMode(WrapMode.TileFlipXY);g.DrawImage(source,new Rectangle(0,0,size,size),crop.X,crop.Y,crop.Width,crop.Height,GraphicsUnit.Pixel,attributes);}}
                    var data=new int[size*size];var bits=tile.LockBits(new Rectangle(0,0,size,size),ImageLockMode.ReadOnly,PixelFormat.Format32bppArgb);try{Marshal.Copy(bits.Scan0,data,0,data.Length);}finally{tile.UnlockBits(bits);}for(int y=0;y<size;y++)Array.Copy(data,y*size,output,(frame/4*size+y)*atlas.Width+frame%4*size,size);
                }}
                var target=atlas.LockBits(new Rectangle(0,0,atlas.Width,atlas.Height),ImageLockMode.WriteOnly,PixelFormat.Format32bppArgb);try{Marshal.Copy(output,0,target.Scan0,output.Length);}finally{atlas.UnlockBits(target);}atlas.Save(Path.Combine(root,"windows","art","sukuna_"+slug+".png"),ImageFormat.Png);
                Console.WriteLine(slug+": "+(rows*4)+" complete cels at "+size+" pixels");
            }
        }
    }
    static int Main(string[] args){try{string root=Path.GetFullPath(args[0]);Compile(root,"cast",6,256);Compile(root,"shrine",6,256);Compile(root,"eye",4,320);return 0;}catch(Exception error){Console.Error.WriteLine(error);return 1;}}
}
