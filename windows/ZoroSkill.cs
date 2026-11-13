using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.Reflection;

namespace ShareMeRowell {
    public static class ZoroSequence {
        public const int Duration=2800;
        public static MadaraMoment At(long time){
            time=Math.Max(0,time);var m=new MadaraMoment{Body=0,Guardian=-1,Eye=-1,ActorScale=.54f,Caption="Three-Sword Tempest"};
            if(time<650){m.Body=Math.Min(23,(int)(time*24/650));m.Caption="Mastery · Emerald eye";return m;}
            if(time<1450){float p=(time-650)/800f;m.Body=24+Math.Min(23,(int)(p*24));m.Guardian=Math.Min(15,(int)(p*16));m.Aura=1;m.Caption="Three-Sword Style · Crosscut";return m;}
            if(time<2600){float p=(time-1450)/1150f;m.Body=48+Math.Min(23,(int)(p*24));m.Guardian=time<1900?12+Math.Min(3,(int)((time-1450)*4/450)):16+Math.Min(7,(int)((time-1900)*8/700));m.Aura=time<1900?.65f:Math.Min(1,(2600-time)/240f);m.Caption=time<1900?"Mastery · Final draw":time<2400?"Emerald Tempest · Sever":"Blades settle";return m;}
            m.Body=71;m.Complete=time>=Duration;return m;
        }
        static float Cut(long time,int start,int decay){long elapsed=time-start;if(elapsed<0)return 0;return elapsed<20?elapsed/20f:Math.Max(0,1-(elapsed-20)/(float)decay);}
        public static float Impact(long time){return Math.Max(Math.Max(Cut(time,850,100),Cut(time,1180,100)),Finisher(time));}
        public static float Finisher(long time){return Cut(time,1900,380);}
    }
    // Three complete body banks, one effect bank, and a small reusable buffer for local screen cuts.
    public sealed class ZoroSkillScene:IDisposable {
        readonly Bitmap[] bodies={Load("zoro_ready"),Load("zoro_combo"),Load("zoro_finish")};
        readonly Bitmap slashes=Load("zoro_slashes");readonly Dictionary<string,Bitmap> pixels=new Dictionary<string,Bitmap>();Bitmap sliced;bool disposed;
        static Bitmap Load(string name){using(var stream=Assembly.GetExecutingAssembly().GetManifestResourceStream("ShareMeRowell."+name+".png")){if(stream==null)throw new InvalidOperationException("Missing Zoro artwork: "+name);using(var image=new Bitmap(stream))return image.Clone(new Rectangle(0,0,image.Width,image.Height),PixelFormat.Format32bppArgb);}}
        void Cel(Graphics g,Bitmap atlas,string key,int frame,Rectangle area,float opacity,bool pixel){
            int size=atlas.Width/4;var crop=new Rectangle(frame%4*size,frame/4*size,size,size);Bitmap source=atlas;int available=Math.Min(area.Width,area.Height),extent=Math.Min(available,size);
            if(pixel){Bitmap sampled;key+=":"+frame;if(!pixels.TryGetValue(key,out sampled)){sampled=new Bitmap(128,128,PixelFormat.Format32bppArgb);using(var pg=Graphics.FromImage(sampled)){pg.CompositingMode=CompositingMode.SourceCopy;pg.InterpolationMode=InterpolationMode.NearestNeighbor;pg.PixelOffsetMode=PixelOffsetMode.Half;pg.DrawImage(atlas,new Rectangle(0,0,128,128),crop,GraphicsUnit.Pixel);}pixels.Add(key,sampled);}source=sampled;crop=new Rectangle(0,0,128,128);size=128;if(extent>=128)extent=extent/128*128;}
            var target=new Rectangle(area.X+(area.Width-extent)/2,area.Y+(area.Height-extent)/2,extent,extent);g.InterpolationMode=pixel||extent==size?InterpolationMode.NearestNeighbor:InterpolationMode.HighQualityBicubic;g.PixelOffsetMode=PixelOffsetMode.Half;
            using(var attributes=new ImageAttributes()){attributes.SetWrapMode(WrapMode.TileFlipXY);var matrix=new ColorMatrix();matrix.Matrix33=opacity;attributes.SetColorMatrix(matrix);g.DrawImage(source,target,crop.X,crop.Y,crop.Width,crop.Height,GraphicsUnit.Pixel,attributes);}
        }
        void Core(Graphics g,Rectangle bounds,HeroProfile profile,long time){
            var m=ZoroSequence.At(time);int extent=Math.Min(bounds.Width,bounds.Height);var stage=new Rectangle(bounds.X+(bounds.Width-extent)/2,bounds.Y+(bounds.Height-extent)/2,extent,extent);
            using(var background=new SolidBrush(Palette.Card))g.FillRectangle(background,bounds);using(var wash=new LinearGradientBrush(stage,Color.FromArgb(20,38,30),Palette.Card,90f))g.FillRectangle(wash,stage);
            g.SmoothingMode=profile.PixelPreview?SmoothingMode.None:SmoothingMode.AntiAlias;
            if(time>=650&&time<2400){for(int i=0;i<10;i++){float x=stage.Left+(float)((time*.7+i*41)%extent);using(var line=new Pen(Color.FromArgb(36,115,244,160),1))g.DrawLine(line,x,stage.Top+extent*.1f,x-extent*.32f,stage.Bottom-extent*.1f);}}
            Cel(g,bodies[m.Body/24],"body"+(m.Body/24),m.Body%24,stage,1,profile.PixelPreview);
            if(time>=50&&time<260){float glow=(float)Math.Sin((time-50)/210f*Math.PI);int imageSize=Math.Min(extent,256);if(profile.PixelPreview&&imageSize>=128)imageSize=imageSize/128*128;float eyeX=stage.Left+(extent-imageSize)/2+imageSize*.43f,eyeY=stage.Top+(extent-imageSize)/2+imageSize*(.42f+.045f*time/300f);using(var glint=new Pen(Color.FromArgb((int)(220*glow),130,255,160),Math.Max(1,imageSize/128f)))g.DrawLine(glint,eyeX-imageSize*.1f*glow,eyeY,eyeX+imageSize*.025f*glow,eyeY);}
            if(m.Guardian>=0&&m.Aura>0)Cel(g,slashes,"slashes",m.Guardian,stage,m.Aura,profile.PixelPreview);
            float pulse=ZoroSequence.Impact(time);if(pulse>0){bool reverse=time>=1180&&time<1900;var start=new PointF(reverse?stage.Right:stage.Left,stage.Top+extent*.06f);var end=new PointF(reverse?stage.Left:stage.Right,stage.Bottom-extent*.06f);using(var edge=new Pen(Color.FromArgb((int)(210*pulse),95,255,145),Math.Max(1,12*pulse)))g.DrawLine(edge,start,end);using(var core=new Pen(Color.FromArgb((int)(245*pulse),225,255,235),Math.Max(1,3*pulse)))g.DrawLine(core,start,end);}
        }
        public void Draw(Graphics graphics,Rectangle bounds,HeroProfile profile,long time){
            if(disposed)throw new ObjectDisposedException("ZoroSkillScene");if(bounds.Width<=0||bounds.Height<=0)return;var saved=graphics.Save();try{
                graphics.SetClip(bounds,CombineMode.Intersect);float pulse=ZoroSequence.Impact(time);if(pulse<=0){Core(graphics,bounds,profile,time);return;}
                if(sliced==null||sliced.Size!=bounds.Size){if(sliced!=null)sliced.Dispose();sliced=new Bitmap(bounds.Width,bounds.Height,PixelFormat.Format32bppArgb);}
                using(var buffer=Graphics.FromImage(sliced))Core(buffer,new Rectangle(Point.Empty,bounds.Size),profile,time);
                using(var fill=new SolidBrush(Color.FromArgb(7,16,12)))graphics.FillRectangle(fill,bounds);bool final=time>=1900;int shift=Math.Max(1,(int)Math.Round((final?22:7)*pulse));bool reverse=time>=1180&&time<1900;int x=bounds.X,y=bounds.Y,w=bounds.Width,h=bounds.Height;
                for(int half=0;half<2;half++)using(var clip=new GraphicsPath()){
                    if(reverse)clip.AddPolygon(half==0?new[]{new Point(x,y),new Point(x+w,y),new Point(x,y+h)}:new[]{new Point(x+w,y),new Point(x+w,y+h),new Point(x,y+h)});
                    else clip.AddPolygon(half==0?new[]{new Point(x,y),new Point(x+w,y),new Point(x+w,y+h)}:new[]{new Point(x,y),new Point(x+w,y+h),new Point(x,y+h)});
                    var layer=graphics.Save();graphics.SetClip(clip,CombineMode.Intersect);int direction=half==0?1:-1;graphics.DrawImageUnscaled(sliced,x+direction*shift,y+(reverse?direction:-direction)*shift);graphics.Restore(layer);
                }
                if(final){using(var rim=new Pen(Color.FromArgb((int)(220*pulse),115,255,165),Math.Max(1,3*pulse)))graphics.DrawRectangle(rim,x+1,y+1,w-3,h-3);using(var cut=new Pen(Color.FromArgb((int)(245*pulse),215,255,225),Math.Max(1,5*pulse)))graphics.DrawLine(cut,x,y,x+w,y+h);}
            }finally{graphics.Restore(saved);}
        }
        public void Dispose(){if(disposed)return;disposed=true;foreach(var image in bodies)image.Dispose();slashes.Dispose();foreach(var image in pixels.Values)image.Dispose();pixels.Clear();if(sliced!=null)sliced.Dispose();}
    }
}
