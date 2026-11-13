using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;
using System.Runtime.InteropServices;

// Optional authoring tool: register complete generated poses using transparent gutters.
class CompileZoroArt {
    static int[] Pixels(Bitmap image){var result=new int[image.Width*image.Height];var bits=image.LockBits(new Rectangle(0,0,image.Width,image.Height),ImageLockMode.ReadOnly,PixelFormat.Format32bppArgb);try{Marshal.Copy(bits.Scan0,result,0,result.Length);}finally{image.UnlockBits(bits);}return result;}
    static bool Solid(int pixel){return (uint)pixel>>24>=180;}
    static int[] Cuts(int[] density,int count,int radius){
        var cuts=new int[count+1];cuts[count]=density.Length;
        for(int cut=1;cut<count;cut++){
            int expected=density.Length*cut/count,left=Math.Max(cuts[cut-1]+32,expected-radius),right=Math.Min(density.Length-1,expected+radius),best=-1;double score=double.MaxValue;
            for(int at=left;at<=right;at++)if(density[at]==0){int end=at;while(end+1<=right&&density[end+1]==0)end++;int middle=(at+end)/2;double value=Math.Abs(middle-expected)-Math.Min(end-at+1,24)*.4;if(value<score){best=middle;score=value;}at=end;}
            if(best<0)throw new Exception("No transparent gutter near "+expected+"; refine source before compiling");cuts[cut]=best;
        }return cuts;
    }
    static void Compile(string root,string slug){
        string selected="zoro-"+slug+"-source-1.2.13"+(slug=="slashes"?"":"-v2")+".png";
        using(var source=new Bitmap(Path.Combine(root,"windows","art-source",selected)))using(var atlas=new Bitmap(1024,1536,PixelFormat.Format32bppArgb)){
            if(source.Width!=1024||source.Height!=1536)throw new Exception("Unexpected source size: "+slug);
            var pixels=Pixels(source);var density=new int[source.Height];for(int y=0;y<source.Height;y++)for(int x=0;x<source.Width;x++)if(Solid(pixels[y*source.Width+x]))density[y]++;
            int[] rows=slug=="slashes"?new[]{0,256,512,768,1024,1280,1536}:Cuts(density,6,80);var registrations=new List<string>();
            using(var g=Graphics.FromImage(atlas)){
                g.CompositingMode=CompositingMode.SourceCopy;g.InterpolationMode=InterpolationMode.HighQualityBicubic;g.PixelOffsetMode=PixelOffsetMode.Half;
                for(int row=0;row<6;row++){
                    var horizontal=new int[source.Width];for(int y=rows[row];y<rows[row+1];y++)for(int x=0;x<source.Width;x++)if(Solid(pixels[y*source.Width+x]))horizontal[x]++;
                    int[] columns=slug=="slashes"?new[]{0,256,512,768,1024}:Cuts(horizontal,4,56);
                    for(int column=0;column<4;column++){
                        var allocation=Rectangle.FromLTRB(columns[column],rows[row],columns[column+1],rows[row+1]);Rectangle crop=allocation;Rectangle target;
                        if(slug=="slashes")target=new Rectangle(column*256,row*256,256,256);
                        else{
                            int left=allocation.Right,top=allocation.Bottom,right=-1,bottom=-1;
                            for(int y=allocation.Top;y<allocation.Bottom;y++)for(int x=allocation.Left;x<allocation.Right;x++)if(Solid(pixels[y*source.Width+x])){left=Math.Min(left,x);right=Math.Max(right,x);top=Math.Min(top,y);bottom=Math.Max(bottom,y);}
                            if(right<left)throw new Exception("Empty body cel: "+slug+row+column);
                            crop=Rectangle.FromLTRB(Math.Max(allocation.Left,left-4),Math.Max(allocation.Top,top-4),Math.Min(allocation.Right,right+5),Math.Min(allocation.Bottom,bottom+5));
                            float scale=Math.Min(1,224f/Math.Max(crop.Width,crop.Height));int width=(int)Math.Round(crop.Width*scale),height=(int)Math.Round(crop.Height*scale);
                            target=new Rectangle(column*256+(256-width)/2,row*256+240-height,width,height);
                        }
                        using(var attributes=new ImageAttributes()){attributes.SetWrapMode(WrapMode.TileFlipXY);g.DrawImage(source,target,crop.X,crop.Y,crop.Width,crop.Height,GraphicsUnit.Pixel,attributes);}
                        registrations.Add((row*4+column)+": source="+crop+" target="+target);
                    }
                }
            }
            atlas.Save(Path.Combine(root,"windows","art","zoro_"+slug+".png"),ImageFormat.Png);
            File.WriteAllLines(Path.Combine(root,".local","zoro-registration-"+slug+"-1.2.13.txt"),registrations);
            Console.WriteLine(slug+": 24 complete poses/effects registered into 256px runtime cels");
        }
    }
    static int Main(string[] args){try{string root=Path.GetFullPath(args[0]);foreach(string slug in new[]{"ready","combo","finish","slashes"})Compile(root,slug);return 0;}catch(Exception error){Console.Error.WriteLine(error);return 1;}}
}