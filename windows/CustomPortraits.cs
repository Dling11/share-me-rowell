using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.Runtime.InteropServices;

namespace ShareMeRowell {
    // Whole authored characters: hair/expression choose complete cels, colours tint semantic masks.
    public static class CustomPortraits {
        sealed class Cell {public int[] Pixels;public byte[] Mask;public float[] Shade;}
        static readonly Dictionary<int,Cell> cells=new Dictionary<int,Cell>();
        static readonly Queue<int> cellOrder=new Queue<int>();
        static readonly Dictionary<string,Bitmap> coloured=new Dictionary<string,Bitmap>();static readonly Queue<string> order=new Queue<string>();
        static Cell Data(Bitmap atlas,int look,int hair,int eyes){int key=look*100+hair*10+eyes;Cell result;if(cells.TryGetValue(key,out result))return result;int size=atlas.Width/4;using(var source=atlas.Clone(new Rectangle(eyes*size,hair*size,size,size),PixelFormat.Format32bppArgb)){
            var pixels=new int[size*size];var bits=source.LockBits(new Rectangle(0,0,size,size),ImageLockMode.ReadOnly,PixelFormat.Format32bppArgb);try{Marshal.Copy(bits.Scan0,pixels,0,pixels.Length);}finally{source.UnlockBits(bits);}var mask=new byte[pixels.Length];var shade=new float[pixels.Length];var possible=new bool[pixels.Length];var skin=new bool[pixels.Length];var skinSeeds=new Queue<int>();var queue=new Queue<int>();
            for(int y=0;y<size;y++)for(int x=0;x<size;x++){int i=y*size+x;var c=Color.FromArgb(pixels[i]);if(c.A<16)continue;float h=c.GetHue(),s=c.GetSaturation(),v=Math.Max(c.R,Math.Max(c.G,c.B))/255f;float px=x*96f/size,py=y*96f/size;
                if(py<73&&h>=5&&h<=42&&s>.14&&v>.32&&(py<54||px<36||px>60)){skin[i]=true;if(s>.25&&py>=40&&py<53&&px>33&&px<64||s>.18&&py>=55&&py<72&&(px<36||px>60)){mask[i]=2;skinSeeds.Enqueue(i);}}
                if(py>=43&&h>=135&&h<195&&s>.12){mask[i]=3;shade[i]=v/.84f;}
                else if(py>=48&&py<75&&h>=195&&h<=250&&s>.25){mask[i]=4;shade[i]=v/.86f;}
                else if(hair!=5&&py<72&&s<.32&&v>.18)possible[i]=true;
            }
            while(skinSeeds.Count>0){int i=skinSeeds.Dequeue(),x=i%size,y=i/size;foreach(int n in new[]{x>0?i-1:-1,x+1<size?i+1:-1,y>0?i-size:-1,y+1<size?i+size:-1})if(n>=0&&skin[n]&&mask[n]==0){mask[n]=2;skinSeeds.Enqueue(n);}}
            for(int i=0;i<mask.Length;i++){if(mask[i]==2){var c=Color.FromArgb(pixels[i]);shade[i]=Math.Max(c.R,Math.Max(c.G,c.B))/255f/.98f;}if(possible[i]&&mask[i]==0){float px=i%size*96f/size,py=i/size*96f/size;if(py<26||px<27||px>69){mask[i]=1;queue.Enqueue(i);}}}
            while(queue.Count>0){int i=queue.Dequeue(),x=i%size,y=i/size;foreach(int n in new[]{x>0?i-1:-1,x+1<size?i+1:-1,y>0?i-size:-1,y+1<size?i+size:-1})if(n>=0&&possible[n]&&mask[n]==0){mask[n]=1;queue.Enqueue(n);}}
            for(int i=0;i<mask.Length;i++)if(mask[i]==1){var c=Color.FromArgb(pixels[i]);shade[i]=Math.Max(c.R,Math.Max(c.G,c.B))/255f/.86f;}
            result=new Cell{Pixels=pixels,Mask=mask,Shade=shade};cells.Add(key,result);cellOrder.Enqueue(key);while(cellOrder.Count>12)cells.Remove(cellOrder.Dequeue());return result;
        }}
        public static Bitmap Get(Bitmap atlas,HeroProfile profile,int pose,int hop){int eyes=(pose&2)!=0||hop>0?1:profile.Eyes;string key=profile.Look+":"+profile.Hair+":"+eyes+":"+profile.Skin+":"+profile.HairColour+":"+profile.Shirt+":"+profile.Element;Bitmap result;if(coloured.TryGetValue(key,out result))return result;int size=atlas.Width/4;var data=Data(atlas,profile.Look,profile.Hair,eyes);var pixels=(int[])data.Pixels.Clone();Color[] palette={Color.Empty,PixelArt.Hair[profile.HairColour],PixelArt.Skin[profile.Skin],PixelArt.Shirts[profile.Shirt],PixelArt.Elements[profile.Element]};for(int i=0;i<pixels.Length;i++){int group=data.Mask[i];if(group==0)continue;var c=palette[group];float shade=data.Shade[i];int r=Math.Min(255,(int)(c.R*shade)),g=Math.Min(255,(int)(c.G*shade)),b=Math.Min(255,(int)(c.B*shade));pixels[i]=(pixels[i]&unchecked((int)0xff000000))|(r<<16)|(g<<8)|b;}result=new Bitmap(size,size,PixelFormat.Format32bppArgb);var bits=result.LockBits(new Rectangle(0,0,size,size),ImageLockMode.WriteOnly,PixelFormat.Format32bppArgb);try{Marshal.Copy(pixels,0,bits.Scan0,pixels.Length);}finally{result.UnlockBits(bits);}coloured.Add(key,result);order.Enqueue(key);while(order.Count>32){string oldest=order.Dequeue();coloured[oldest].Dispose();coloured.Remove(oldest);}return result;}
    }
}
