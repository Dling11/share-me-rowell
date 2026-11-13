using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.Reflection;
using System.Windows.Forms;

namespace ShareMeRowell {
    public struct MadaraMoment {
        public int Body,Guardian,Eye;
        public float Aura,Closeup,ActorScale;
        public bool Complete;
        public string Caption;
    }
    public static class MadaraSequence {
        public const int Duration=5200;
        public static bool CanAutoUse(HeroProfile profile,int role,int level){return profile.AutoSkills&&role==23&&Adventure.CanEquip(role,level);}
        public static bool ShouldPlay(HeroProfile profile,bool highContrast,bool systemMotion,bool visible,bool active,bool minimized,bool finished){return visible&&active&&!minimized&&!finished&&PixelMotion.Allowed(profile.Animations,highContrast,systemMotion,profile.MotionOverride);}
        static float Progress(long time,int start,int length){return Math.Max(0,Math.Min(1,(time-start)/(float)length));}
        public static MadaraMoment At(long time){
            time=Math.Max(0,time);long realTime=time;time=time>=Duration?9400:time*9400/Duration;var m=new MadaraMoment{Body=0,Guardian=-1,Eye=-1,ActorScale=.54f,Caption="Crimson Awakening"};
            if(time<600)return m;
            if(time<2800){m.Body=Math.Min(11,(int)(Progress(time,600,2200)*12));m.Caption="The hand sign";return m;}
            if(time<4000){m.Body=12+Math.Min(3,(int)(Progress(time,2800,1200)*4));m.Eye=Math.Min(15,(int)(Progress(time,2800,1200)*16));m.Closeup=Math.Min(Progress(time,2800,160),1-Progress(time,3820,180));m.Caption="Eyes of the Uchiha";return m;}
            if(time<5600){float p=Progress(time,4000,1600);m.Body=16+Math.Min(3,(int)(p*4));m.Guardian=Math.Min(11,(int)(p*12));m.Aura=p;m.ActorScale=.54f-.14f*p;m.Caption="Susanoo emerges";return m;}
            if(time<7400){m.Body=19;m.Guardian=12+Math.Min(11,(int)(Progress(time,5600,1800)*12));m.Aura=1;m.ActorScale=.40f;m.Caption="Susanoo · Awakened";return m;}
            if(time<8600){float p=Progress(time,7400,1200);m.Body=20+Math.Min(3,(int)(p*4));m.Guardian=23-Math.Min(23,(int)(p*24));m.Aura=1-p;m.ActorScale=.40f+.14f*p;m.Caption="Power, controlled";return m;}
            m.Body=23;m.Caption="Crimson Awakening";m.Complete=realTime>=Duration;return m;
        }
    }
    // Assets are owned by the open scene, never loaded by the normal sharing/idle path.
    public sealed class CharacterSkillScene:IDisposable {
        readonly int role;readonly Bitmap cast,guardian,eye;readonly ZoroSkillScene zoro;public CharacterSkillScene(int character){role=character;if(role==13){zoro=new ZoroSkillScene();return;}string prefix=CharacterSkills.Prefix(role);cast=Load(prefix+"_cast");guardian=Load(prefix+(role==19?"_shrine":"_guardian"));eye=Load(prefix+"_eye");}
        readonly Dictionary<string,Bitmap> pixels=new Dictionary<string,Bitmap>();bool disposed;
        static Bitmap Load(string name){using(var stream=Assembly.GetExecutingAssembly().GetManifestResourceStream("ShareMeRowell."+name+".png")){if(stream==null)throw new InvalidOperationException("Missing cinematic artwork: "+name);using(var image=new Bitmap(stream))return image.Clone(new Rectangle(0,0,image.Width,image.Height),PixelFormat.Format32bppArgb);}}
        void Frame(Graphics g,Bitmap atlas,string name,int frame,int rows,Rectangle box,float opacity,bool pixel){
            if(frame<0||opacity<=0)return;int cell=atlas.Width/4;if(frame>=4*rows)throw new ArgumentOutOfRangeException("frame");var crop=new Rectangle(frame%4*cell,frame/4*cell,cell,cell);if(name=="eye"){crop.Inflate(-cell/10,-cell/10);cell=crop.Width;}Bitmap source=atlas;
            if(pixel){string key=name+":"+frame;Bitmap sampled;if(!pixels.TryGetValue(key,out sampled)){sampled=new Bitmap(128,128,PixelFormat.Format32bppArgb);using(var pg=Graphics.FromImage(sampled)){pg.CompositingMode=CompositingMode.SourceCopy;pg.InterpolationMode=InterpolationMode.NearestNeighbor;pg.PixelOffsetMode=PixelOffsetMode.Half;pg.DrawImage(atlas,new Rectangle(0,0,128,128),crop,GraphicsUnit.Pixel);}pixels.Add(key,sampled);}source=sampled;crop=new Rectangle(0,0,128,128);cell=128;}
            int extent=Math.Min(box.Width,box.Height);extent=pixel&&extent>=cell?extent/cell*cell:Math.Min(extent,cell);var target=new Rectangle(box.X+(box.Width-extent)/2,box.Bottom-extent,extent,extent);
            g.InterpolationMode=pixel||extent==cell?InterpolationMode.NearestNeighbor:InterpolationMode.HighQualityBicubic;g.PixelOffsetMode=PixelOffsetMode.Half;
            using(var attributes=new ImageAttributes()){attributes.SetWrapMode(WrapMode.TileFlipXY);var matrix=new ColorMatrix();matrix.Matrix33=Math.Min(1,opacity);attributes.SetColorMatrix(matrix);g.DrawImage(source,target,crop.X,crop.Y,crop.Width,crop.Height,GraphicsUnit.Pixel,attributes);}
        }
        public void Draw(Graphics graphics,Rectangle bounds,HeroProfile profile,long milliseconds,bool inline=false){
            if(disposed)throw new ObjectDisposedException("CharacterSkillScene");if(zoro!=null){zoro.Draw(graphics,bounds,profile,milliseconds);return;}if(bounds.Width<=0||bounds.Height<=0)return;var saved=graphics.Save();try{
                Color backdrop=inline?Palette.Card:Palette.Page;graphics.SetClip(bounds,CombineMode.Intersect);using(var background=new SolidBrush(backdrop))graphics.FillRectangle(background,bounds);int size=Math.Min(bounds.Width,bounds.Height);var stage=new Rectangle(bounds.X+(bounds.Width-size)/2,bounds.Y+(bounds.Height-size)/2,size,size);var m=CharacterSkills.At(role,milliseconds);var energy=CharacterSkills.Energy(role);
                using(var wash=new LinearGradientBrush(stage,(role==19?Color.FromArgb(45,22,29):Color.FromArgb(26,22,45)),backdrop,90f))graphics.FillRectangle(wash,stage);
                graphics.SmoothingMode=profile.PixelPreview?SmoothingMode.None:SmoothingMode.AntiAlias;
                int bottom=stage.Bottom-18,body=Math.Max(96,(int)(size*(inline?(.48f+(m.ActorScale-.4f)/.14f*.46f):(profile.PixelPreview?.42f:m.ActorScale))));
                if(m.Aura>0){int radius=(int)(size*(.28f+.1f*m.Aura));var ring=new Rectangle(stage.Left+size/2-radius,bottom-18-radius/3,radius*2,Math.Max(12,radius/3));using(var pen=new Pen(Color.FromArgb((int)(110*m.Aura),energy),2))graphics.DrawEllipse(pen,ring);
                    for(int i=0;i<18;i++){double phase=milliseconds*.0004+i*2.399;float x=stage.Left+size/2+(float)Math.Cos(phase)*size*(.22f+i%3*.04f),y=bottom-(float)((milliseconds*.035+i*29)%(size*.8));using(var brush=new SolidBrush(Color.FromArgb((int)(m.Aura*(65+i%4*20)),energy)))graphics.FillRectangle(brush,(int)x,(int)y,i%3==0?3:2,i%3==0?6:3);}
                    if(role==19)for(int i=0;i<5;i++){float x=stage.Left+(float)((milliseconds*.15+i*67)%size);using(var cut=new Pen(Color.FromArgb((int)(80*m.Aura),energy),1.4f))graphics.DrawLine(cut,x,stage.Top+size*.15f,x-size*.2f,stage.Bottom-size*.15f);}int aura=(int)(size*.98f);Frame(graphics,guardian,"guardian",m.Guardian,6,new Rectangle(stage.Left+(size-aura)/2,bottom-aura,aura,aura),m.Aura,profile.PixelPreview);
                }
                Frame(graphics,cast,"cast",m.Body,6,new Rectangle(stage.Left+(size-body)/2,bottom-body,body,body),1,profile.PixelPreview);
                if(m.Closeup>0){using(var shade=new SolidBrush(Color.FromArgb((int)(225*m.Closeup),backdrop)))graphics.FillRectangle(shade,stage);int native=eye.Width/4-eye.Width/4/10*2;int close=(int)(Math.Min(size*.86f,native)*(.72f+.28f*m.Closeup));var cutin=new Rectangle(stage.Left+(size-close)/2,stage.Top+(size-close)/2,close,close);Frame(graphics,eye,"eye",m.Eye,4,cutin,m.Closeup,profile.PixelPreview);using(var line=new Pen(Color.FromArgb((int)(145*m.Closeup),195,66,85),1)){graphics.DrawLine(line,stage.Left+18,cutin.Top,cutin.Left-8,cutin.Top);graphics.DrawLine(line,cutin.Right+8,cutin.Bottom,stage.Right-18,cutin.Bottom);}}
            }finally{graphics.Restore(saved);}
        }
        public void Dispose(){if(disposed)return;disposed=true;foreach(var image in pixels.Values)image.Dispose();pixels.Clear();if(zoro!=null)zoro.Dispose();if(cast!=null)cast.Dispose();if(guardian!=null)guardian.Dispose();if(eye!=null)eye.Dispose();}
    }
    public sealed class MadaraScene:IDisposable {
        readonly CharacterSkillScene scene=new CharacterSkillScene(23);
        public void Draw(Graphics graphics,Rectangle bounds,HeroProfile profile,long time,bool inline=false){scene.Draw(graphics,bounds,profile,time,inline);}
        public void Dispose(){scene.Dispose();}
    }
    // Monotonic playback time advances only while the previous interval was active.
    public sealed class SkillPlayback {
        long last;int duration=MadaraSequence.Duration;bool advancing;
        public long Position{get;private set;}public bool Running{get;private set;}
        public void Start(long now,int length=MadaraSequence.Duration){if(length<=0)throw new ArgumentOutOfRangeException("length");duration=length;Position=0;last=now;advancing=false;Running=true;}
        public void Update(long now,bool active){if(Running&&advancing)Position=Math.Min(duration,Position+Math.Max(0,now-last));last=now;if(Position>=duration)Running=false;advancing=Running&&active;}
        public void Stop(){Running=false;advancing=false;}
    }
    // This occupies the existing portrait area. No popup, persistent game loop or progress writes.
    public sealed class CharacterSkillPreview:Control {
        readonly Timer clock=new Timer{Interval=33};readonly Stopwatch time=Stopwatch.StartNew();readonly SkillPlayback playback=new SkillPlayback();
        HeroProfile profile=new HeroProfile();CharacterSkillScene scene;Form owner;string caption="";bool releasing;
        public event EventHandler PlaybackEnded;public event EventHandler PhaseChanged;
        public bool Running{get{return playback.Running;}}public bool Playing{get{return clock.Enabled;}}public long Position{get{return playback.Position;}}public string Caption{get{return caption;}}
        public CharacterSkillPreview(){DoubleBuffered=true;BackColor=Palette.Card;Cursor=Cursors.Hand;AccessibleName="Character skill in portrait preview";clock.Tick+=(s,e)=>Activity();}
        public void Configure(HeroProfile source){int previous=profile.Role;profile=source.Copy();if(Running&&(previous!=profile.Role||!CharacterSkills.Has(profile.Role)||!MotionAllowed))Stop();Invalidate();}
        bool MotionAllowed{get{return PixelMotion.Allowed(profile.Animations,SystemInformation.HighContrast,PixelMotion.SystemAnimationsEnabled,profile.MotionOverride);}}
        public bool Start(HeroProfile source){Configure(source);if(!CharacterSkills.Has(profile.Role)||!MotionAllowed)return false;if(scene==null)scene=new CharacterSkillScene(profile.Role);clock.Interval=profile.Role==13?25:33;playback.Start(time.ElapsedMilliseconds,CharacterSkills.Duration(profile.Role));caption=CharacterSkills.At(profile.Role,0).Caption;Activity();Invalidate();return true;}
        public void Stop(){bool was=Running;playback.Stop();clock.Stop();ReleaseScene();if(was&&!releasing){var ended=PlaybackEnded;if(ended!=null)ended(this,EventArgs.Empty);}}
        void ReleaseScene(){if(scene!=null){scene.Dispose();scene=null;}}
        void OwnerActivity(object sender,EventArgs e){Activity();}
        void Activity(){if(IsDisposed||Disposing||releasing)return;bool was=Running;var host=FindForm();bool active=IsHandleCreated&&host!=null&&MadaraSequence.ShouldPlay(profile,SystemInformation.HighContrast,PixelMotion.SystemAnimationsEnabled,Visible,Form.ActiveForm==host,host.WindowState==FormWindowState.Minimized,!Running);playback.Update(time.ElapsedMilliseconds,active);clock.Enabled=Running&&active;
            string next=CharacterSkills.At(profile.Role,Position).Caption;if(next!=caption){caption=next;var phase=PhaseChanged;if(phase!=null)phase(this,EventArgs.Empty);}if(was&&!Running){ReleaseScene();var ended=PlaybackEnded;if(ended!=null)ended(this,EventArgs.Empty);}if(was)Invalidate();
        }
        protected override void OnHandleCreated(EventArgs e){base.OnHandleCreated(e);owner=FindForm();if(owner!=null){owner.Activated+=OwnerActivity;owner.Deactivate+=OwnerActivity;owner.Resize+=OwnerActivity;}Activity();}
        protected override void OnHandleDestroyed(EventArgs e){bool was=Running;clock.Stop();playback.Stop();ReleaseScene();if(owner!=null){owner.Activated-=OwnerActivity;owner.Deactivate-=OwnerActivity;owner.Resize-=OwnerActivity;owner=null;}if(was&&!releasing&&!Disposing){var ended=PlaybackEnded;if(ended!=null)ended(this,EventArgs.Empty);}base.OnHandleDestroyed(e);}
        protected override void OnVisibleChanged(EventArgs e){base.OnVisibleChanged(e);Activity();}
        protected override void OnPaint(PaintEventArgs e){base.OnPaint(e);if(scene!=null&&Running)scene.Draw(e.Graphics,ClientRectangle,profile,Position,true);}
        protected override void Dispose(bool disposing){if(disposing){releasing=true;clock.Stop();clock.Dispose();playback.Stop();time.Stop();ReleaseScene();}base.Dispose(disposing);}
    }
}
