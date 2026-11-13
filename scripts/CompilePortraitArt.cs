using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;
using System.Runtime.InteropServices;

// Optional authoring utility. Application builds embed the precompiled PNGs and need no extra runtime.
class CompilePortraitArt {
    static void Compile(string root,string slug,string sourceName){
        string input=Path.Combine(root,"android","art",sourceName),output=Path.Combine(root,"windows","art","portrait_"+slug+".png");
        using(var source=new Bitmap(input)){
            int rows=(int)Math.Round(source.Height*4.0/source.Width);if(source.Width<256||source.Width*rows!=source.Height*4)throw new Exception("Non-square cells: "+slug);
            const int size=256;
            using(var atlas=new Bitmap(size*4,rows*size,PixelFormat.Format32bppArgb)){
                var outputPixels=new int[atlas.Width*atlas.Height];
                for(int row=0;row<rows;row++)for(int column=0;column<4;column++)using(var tile=source.Width/4==size?source.Clone(new Rectangle(column*size,row*size,size,size),PixelFormat.Format32bppArgb):new Bitmap(size,size,PixelFormat.Format32bppArgb)){
                    if(source.Width/4!=size)using(var g=Graphics.FromImage(tile)){g.Clear(Color.Transparent);g.CompositingMode=CompositingMode.SourceCopy;g.InterpolationMode=InterpolationMode.HighQualityBicubic;g.PixelOffsetMode=PixelOffsetMode.Half;using(var attributes=new ImageAttributes()){attributes.SetWrapMode(WrapMode.TileFlipXY);g.DrawImage(source,new Rectangle(0,0,size,size),column*source.Width/4,row*source.Height/rows,source.Width/4,source.Height/rows,GraphicsUnit.Pixel,attributes);}}
                    int baseline=246;bool found=false;for(int y=size-1;y>=160&&!found;y--)for(int x=72;x<188;x++)if(tile.GetPixel(x,y).A>=128){baseline=y;found=true;break;}
                    int shift=Math.Max(-16,Math.Min(16,246-baseline));var pixels=new int[size*size];var bits=tile.LockBits(new Rectangle(0,0,size,size),ImageLockMode.ReadOnly,PixelFormat.Format32bppArgb);try{Marshal.Copy(bits.Scan0,pixels,0,pixels.Length);}finally{tile.UnlockBits(bits);}for(int y=0;y<size;y++){int destination=y+shift;if(destination>=0&&destination<size)Array.Copy(pixels,y*size,outputPixels,(row*size+destination)*atlas.Width+column*size,size);}
                }var outputBits=atlas.LockBits(new Rectangle(0,0,atlas.Width,atlas.Height),ImageLockMode.WriteOnly,PixelFormat.Format32bppArgb);try{Marshal.Copy(outputPixels,0,outputBits.Scan0,outputPixels.Length);}finally{atlas.UnlockBits(outputBits);}atlas.Save(output,ImageFormat.Png);
            }
        }Console.WriteLine("Compiled whole-character desktop portrait: "+slug);
    }
    static int Main(string[] args){try{string root=Path.GetFullPath(args[0]);Compile(root,"special","pixel-special-source.png");Compile(root,"yugi","pixel-yugi-source.png");foreach(string slug in new[]{"luffy","zoro","boa","shanks","eren","levi","mikasa","sukuna","itachi","rock-lee","might-guy","madara"})Compile(root,slug.Replace('-','_'),"pixel-"+slug+"-source.png");Compile(root,"seraph","pc-seraph-source-1.2.7.png");Compile(root,"sovereign","pc-sovereign-source-1.2.7.png");Compile(root,"custom_boy","pc-custom_boy-source-1.2.7.png");Compile(root,"custom_girl","pc-custom_girl-source-1.2.7.png");return 0;}catch(Exception error){Console.Error.WriteLine(error);return 1;}}
}
