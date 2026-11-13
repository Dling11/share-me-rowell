using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Windows.Forms;

namespace ShareMeRowell {
    public static class PixelArt {
        public static readonly Color[] Skin=Colours(0xFFD9BC,0xEFB58D,0xD59467,0xAB704C,0x74472F);
        public static readonly Color[] Hair=Colours(0x302536,0x79492E,0xE7B95B,0xB073AD,0x4B8095,0xD8DDED);
        public static readonly Color[] Shirts=Colours(0x58D6B0,0x9775E8,0xE06185,0xE9BE59,0x69A9E2);
        public static readonly Color[] Elements=Colours(0x70E2C2,0xFFA071,0x85C9FF,0xA6E48B,0xBFA3FF,0xFFDA89);
        public static readonly Color[] RankColours=Colours(0xB5C4D1,0xE0AC76,0x85D7EA,0x70E2C2,0xBFA3FF,0xFF857F,0xFFDA89,0xE6D2FF);
        static readonly Color Ink=Color.FromArgb(21,19,33);
        static readonly Bitmap[] Atlases={Atlas("pixel_armory"),Atlas("pixel_progression"),Atlas("pixel_heads"),Atlas("pixel_anime"),Atlas("pixel_focus"),Atlas("pixel_special"),Atlas("pixel_yugi"),Atlas("pixel_luffy"),Atlas("pixel_zoro"),Atlas("pixel_boa"),Atlas("pixel_shanks"),Atlas("pixel_eren"),Atlas("pixel_levi"),Atlas("pixel_mikasa"),Atlas("pixel_sukuna"),Atlas("pixel_itachi"),Atlas("pixel_rock_lee"),Atlas("pixel_might_guy"),Atlas("pixel_madara")};
        static readonly Dictionary<string,Bitmap> Cache=new Dictionary<string,Bitmap>();
        static readonly Queue<string> CacheOrder=new Queue<string>();
        static Color[] Colours(params int[] rgb){return rgb.Select(value=>Color.FromArgb(255,(value>>16)&255,(value>>8)&255,value&255)).ToArray();}
        static Bitmap Atlas(string name){using(var stream=Assembly.GetExecutingAssembly().GetManifestResourceStream("ShareMeRowell."+name+".png")){if(stream==null)throw new IOException("Missing bundled pixel artwork: "+name);using(var image=new Bitmap(stream))return new Bitmap(image);}}
        static Bitmap Cached(string key,Func<Bitmap> make){Bitmap image;if(Cache.TryGetValue(key,out image))return image;image=make();while(Cache.Count>=24){string oldest=CacheOrder.Dequeue();Cache[oldest].Dispose();Cache.Remove(oldest);}Cache.Add(key,image);CacheOrder.Enqueue(key);return image;}
        static float[] Hsv(Color c){float max=Math.Max(c.R,Math.Max(c.G,c.B))/255f,min=Math.Min(c.R,Math.Min(c.G,c.B))/255f;return new[]{c.GetHue(),max==0?0:(max-min)/max,max};}
        static Color Hsv(float h,float s,float v){s=Math.Max(0,Math.Min(1,s));v=Math.Max(0,Math.Min(1,v));float c=v*s,x=c*(1-Math.Abs((h/60)%2-1)),m=v-c;float r=0,g=0,b=0;if(h<60){r=c;g=x;}else if(h<120){r=x;g=c;}else if(h<180){g=c;b=x;}else if(h<240){g=x;b=c;}else if(h<300){r=x;b=c;}else{r=c;b=x;}return Color.FromArgb(255,(int)Math.Round((r+m)*255),(int)Math.Round((g+m)*255),(int)Math.Round((b+m)*255));}
        static Color Recolour(float[] source,Color target,float referenceValue,float referenceSaturation,float minSaturation){var t=Hsv(target);return Hsv(t[0],Math.Max(minSaturation,Math.Min(1,source[1]*t[1]/referenceSaturation)),Math.Max(.05f,Math.Min(1,source[2]*t[2]/referenceValue)));}
        static Bitmap Equipment(HeroProfile p,HeroGear gear){int slot=gear.Pair*2+p.Look;return Cached("b:"+gear.Atlas+":"+slot+":"+p.Shirt+":"+p.Element+":"+p.Skin,()=>{
            var source=Atlases[gear.Atlas==2?3:gear.Atlas];var image=new Bitmap(64,64,PixelFormat.Format32bppArgb);int baseline=61;
            for(int y=63;y>=45;y--){bool found=false;for(int x=23;x<=41;x++)if(source.GetPixel(slot%4*64+x,slot/4*64+y).A>=128){found=true;break;}if(found){baseline=y;break;}}
            int shift=Math.Max(-4,Math.Min(4,61-baseline));
            for(int y=0;y<64;y++)for(int x=0;x<64;x++){int ty=y+shift;if(ty<0||ty>=64||(x>=18&&x<=46&&ty<35))continue;var c=source.GetPixel(slot%4*64+x,slot/4*64+y);if(c.A<128)continue;var hsv=Hsv(c);Color colour=Color.FromArgb(255,c);if(gear.Atlas==2&&hsv[0]>=0&&hsv[0]<=40&&hsv[1]>.12f&&hsv[1]<.42f&&hsv[2]>.65f)colour=Recolour(hsv,Skin[p.Skin],.96f,.24f,.03f);else if(hsv[1]>.28f&&hsv[0]>=260&&hsv[0]<=335)colour=Recolour(hsv,Shirts[p.Shirt],.78f,.65f,.15f);else if(hsv[1]>.32f&&hsv[0]>=165&&hsv[0]<=205)colour=Recolour(hsv,Elements[p.Element],.78f,.65f,.15f);image.SetPixel(x,ty,colour);}return image;
        });}
        static Bitmap HeadLayer(int slot,int width,int height,HeroProfile p,bool hair){return Cached((hair?"h:":"f:")+slot+":"+p.Skin+":"+p.HairColour+":"+p.Element,()=>{
            bool focused=!hair&&(slot==3||slot>=5&&slot<=7);int actualSlot=focused?(slot==3?0:slot-4):!hair&&slot==4?3:slot;var source=Atlases[focused?4:2];int columns=source.Width/48;using(var cell=new Bitmap(48,48,PixelFormat.Format32bppArgb)){
                int left=48,top=48,right=0,bottom=0;
                for(int y=0;y<48;y++)for(int x=0;x<48;x++){var c=source.GetPixel(actualSlot%columns*48+x,actualSlot/columns*48+y);if(c.A<128)continue;left=Math.Min(left,x);top=Math.Min(top,y);right=Math.Max(right,x);bottom=Math.Max(bottom,y);var hsv=Hsv(c);Color colour=Color.FromArgb(255,c);
                    if(hair&&hsv[2]>.15f&&hsv[1]>.12f&&hsv[0]>=240&&hsv[0]<=335)colour=Recolour(hsv,Hair[p.HairColour],.7f,.65f,.03f);
                    else if(!hair&&hsv[2]>.25f&&hsv[1]>.12f&&hsv[0]>=0&&hsv[0]<=45)colour=Recolour(hsv,Skin[p.Skin],.96f,.24f,.03f);
                    else if(!hair&&hsv[1]>.35f&&hsv[0]>=165&&hsv[0]<=210)colour=Recolour(hsv,Elements[p.Element],.9f,.65f,.03f);
                    cell.SetPixel(x,y,colour);
                }
                if(left>right)throw new IOException("Pixel head layer is missing.");var result=new Bitmap(width,height,PixelFormat.Format32bppArgb);using(var g=Graphics.FromImage(result)){Crisp(g);g.DrawImage(cell,new Rectangle(0,0,width,height),new Rectangle(left,top,right-left+1,bottom-top+1),GraphicsUnit.Pixel);}return result;
            }
        });}
        public static void Crisp(Graphics g){g.SmoothingMode=SmoothingMode.None;g.InterpolationMode=InterpolationMode.NearestNeighbor;g.PixelOffsetMode=PixelOffsetMode.Half;}
        static Color Shade(Color c,float amount){return amount<=1?Color.FromArgb(c.A,(int)(c.R*amount),(int)(c.G*amount),(int)(c.B*amount)):Color.FromArgb(c.A,(int)(c.R+(255-c.R)*Math.Min(1,amount-1)),(int)(c.G+(255-c.G)*Math.Min(1,amount-1)),(int)(c.B+(255-c.B)*Math.Min(1,amount-1)));}
        static void Px(Graphics g,Color c,int x,int y,int w=1,int h=1){using(var b=new SolidBrush(c))g.FillRectangle(b,x,y,w,h);}
        static void Poly(Graphics g,Color c,params int[] xy){int count=xy.Length/2,top=Enumerable.Range(0,count).Min(i=>xy[i*2+1]),bottom=Enumerable.Range(0,count).Max(i=>xy[i*2+1]);for(int y=top;y<=bottom;y++){var crossings=new List<float>();float py=y+.5f;for(int i=0;i<count;i++){int j=(i+1)%count,y1=xy[i*2+1],y2=xy[j*2+1];if((y1<=py&&y2>py)||(y2<=py&&y1>py))crossings.Add(xy[i*2]+(py-y1)*(xy[j*2]-xy[i*2])/(y2-y1));}crossings.Sort();for(int i=0;i+1<crossings.Count;i+=2){int left=(int)Math.Ceiling(crossings[i]-.5f),right=(int)Math.Ceiling(crossings[i+1]-.5f);if(right>left)Px(g,c,left,y,right-left);}}}
        static void Star(Graphics g,Color c,int x,int y,int r){Px(g,c,x-r,y,r*2+1);Px(g,c,x,y-r,1,r*2+1);}
        static void Gem(Graphics g,Color c,int x,int y,int r){for(int dy=-r;dy<=r;dy++){int span=r-Math.Abs(dy);Px(g,c,x-span,y+dy,span*2+1);}Px(g,Shade(c,1.3f),x,y-r);}
        static void Border(Graphics g,Color c,int x,int y,int w,int h,int cut){Px(g,c,x+cut,y,w-cut*2);Px(g,c,x+cut,y+h-1,w-cut*2);Px(g,c,x,y+cut,1,h-cut*2);Px(g,c,x+w-1,y+cut,1,h-cut*2);for(int i=0;i<cut;i+=2)foreach(int sx in new[]{1,-1})foreach(int sy in new[]{1,-1})Px(g,c,(sx==1?x:x+w-2)+sx*i,(sy==1?y:y+h-2)+sy*(cut-i-2),2,2);}
        static void Frame(Graphics g,int level){var c=RankColours[Adventure.RankIndex(level)];Border(g,Color.FromArgb(190,c),3,3,74,74,8);if(level>=25)Border(g,Shade(c,1.2f),6,6,68,68,6);if(level>=45)foreach(int x in new[]{10,69})foreach(int y in new[]{10,69})Gem(g,c,x,y,2);if(level>=60){Gem(g,c,40,4,3);Gem(g,c,40,75,2);foreach(int sign in new[]{-1,1})Poly(g,c,40+sign*30,61,40+sign*38,53,40+sign*36,65,40+sign*25,74);}if(level>=75){Poly(g,c,8,10,2,1,3,12,10,17);Poly(g,c,72,10,78,1,77,12,70,17);}if(level>=85){Star(g,c,40,3,3);foreach(int y in new[]{23,34,45}){Px(g,c,1,y,2,5);Px(g,c,77,y,2,5);}}if(level>=100){Star(g,Color.FromArgb(246,230,255),40,3,2);Star(g,c,4,40,3);Star(g,c,75,40,3);}}
        public static Bitmap Render(HeroProfile profile,int level,int pose=0,int hop=0,int gearLevel=0,bool locked=false,bool framed=true){
            var p=profile.Copy();p.Clean(100);var gear=Adventure.Gear(p.Role,gearLevel==0?level:gearLevel);if(!Adventure.CanEquip(p.Role,gearLevel==0?level:gearLevel))p.Role=0;
            if(Adventure.IsSpecial(p.Role))return RenderSpecial(p.Role,level,pose,hop,gearLevel==0?level:gearLevel,locked,framed);
            var image=new Bitmap(80,80,PixelFormat.Format32bppArgb);using(var g=Graphics.FromImage(image)){Crisp(g);if(framed)Frame(g,level);Px(g,Color.FromArgb(72,0,0,0),26+hop,69,28-hop*2,2);var transform=g.Save();g.TranslateTransform(8,8-hop);int bob=(pose&1)!=0?-1:0,sway=PixelMotion.Direction(pose,5),turn=PixelMotion.Direction(pose,7),hairLag=(pose>>9)&1,leftArm=hop>0?-1:((pose>>10)&1)!=0?-1:0,rightArm=hop>0?-1:((pose>>11)&1)!=0?-1:0;
                if(p.Hair==3){var back=g.Save();g.TranslateTransform(sway+turn-hairLag,bob+2);Poly(g,Ink,18,14,46,14,49,43,45,48,40,46,23,49,16,44);Poly(g,Shade(Hair[p.HairColour],.65f),20,16,44,16,47,43,43,45,40,43,23,46,18,43);g.Restore(back);}
                var body=Equipment(p,gear);
                // Feet stay planted. Torso and the two arm/weapon groups share a hip anchor.
                g.DrawImage(body,new Rectangle(0,54,64,10),new Rectangle(0,54,64,10),GraphicsUnit.Pixel);
                g.DrawImage(body,new Rectangle(23+sway,bob,18,54-bob),new Rectangle(23,0,18,54),GraphicsUnit.Pixel);
                g.DrawImage(body,new Rectangle(sway,bob+leftArm,25,54-bob-leftArm),new Rectangle(0,0,25,54),GraphicsUnit.Pixel);
                g.DrawImage(body,new Rectangle(40+sway,bob+rightArm,24,54-bob-rightArm),new Rectangle(40,0,24,54),GraphicsUnit.Pixel);
                // The neck sits behind the chin and inside the collar; it follows the torso,
                // while the head may turn a single pixel without opening a transparent seam.
                Px(g,Shade(Skin[p.Skin],.76f),30+sway,35+bob,4,3);Px(g,Skin[p.Skin],31+sway,36+bob,2,2);
                g.TranslateTransform(sway+turn,bob+2);var cloth=Shirts[p.Shirt];var accent=Elements[p.Element];var metal=Color.FromArgb(218,229,239);
                if(p.Role==2||p.Role==3){Poly(g,Ink,14,22,17,12,24,5,32,2,40,5,47,12,50,22,46,33,18,33);Poly(g,Shade(cloth,.55f),16,22,19,13,25,7,32,4,39,7,45,13,48,22,44,31,20,31);}
                int expression=(pose&2)!=0&&p.Eyes!=1?(p.Eyes==3?5:4):p.Eyes==3&&turn<0?6:p.Eyes==3&&turn>0?7:p.Eyes;var face=HeadLayer(expression,28,28,p,false);g.DrawImageUnscaled(face,18,8);
                if(p.Hair!=5){int[] widths={34,36,38,36,40},heights={23,27,28,38,35};var hair=HeadLayer(4+p.Hair,widths[p.Hair],heights[p.Hair],p,true);g.DrawImageUnscaled(hair,32-widths[p.Hair]/2-hairLag,(p.Hair==1||p.Hair==2?0:p.Hair==4?1:3)+hairLag);}
                switch(p.Role){
                    case 0:if(gear.Ornate){Px(g,Ink,42,18,5,4);Px(g,metal,43,19,3,2);Px(g,accent,44,19);}break;
                    case 1:Poly(g,Ink,13,15,23,12,29,0,35,4,39,12,51,15,49,18,15,18);Poly(g,Shade(cloth,.72f),17,15,24,13,30,2,34,5,38,14,47,15,47,17,17,17);if(gear.Ornate){Px(g,metal,21,15,23,1);Star(g,accent,32,10,2);}break;
                    case 2:case 3:Poly(g,Ink,15,18,18,10,25,4,32,1,40,5,47,12,49,19,45,16,39,9,32,6,25,9,20,14);Poly(g,Shade(cloth,p.Role==3?.58f:.76f),17,17,20,11,26,6,32,3,39,7,45,12,47,17,44,14,38,8,32,5,25,8,21,13);if(gear.Ornate)Star(g,accent,32,8,1);break;
                    case 4:if(gear.Ornate){Poly(g,Ink,19,14,15,9,15,1,20,6,22,12,42,12,44,6,49,1,49,9,45,14,43,17,21,17);Poly(g,Color.FromArgb(197,154,101),19,12,17,8,17,4,20,8,23,14,41,14,44,8,47,4,47,8,45,12,42,16,22,16);Px(g,metal,22,15,20,1);Gem(g,accent,32,13,3);}break;
                    case 5:if(gear.Ornate){var gold=Color.FromArgb(255,211,122);Px(g,gold,23,1,18,1);Px(g,gold,19,2,4,1);Px(g,gold,41,2,4,1);Px(g,gold,17,3,2,2);Px(g,gold,45,3,2,2);Px(g,gold,19,5,4,1);Px(g,gold,41,5,4,1);Px(g,gold,23,6,18,1);Star(g,accent,32,1,1);}break;
                    case 6:if(gear.Ornate){Poly(g,Ink,19,4,26,8,32,0,38,8,45,4,44,15,20,15);Poly(g,Color.FromArgb(233,188,101),21,7,27,10,32,3,37,10,43,7,42,14,22,14);Px(g,metal,22,13,20,1);Gem(g,accent,32,10,2);}break;
                    case 7:Px(g,Ink,19,13,27,3);Px(g,cloth,20,14,25,1);break;
                    case 8:if(gear.Ornate){Poly(g,Ink,17,18,25,19,38,18,47,16,46,24,38,26,24,26,18,24);Px(g,Shade(cloth,.35f),20,20,24,3);}break;
                    case 10:Px(g,Ink,17,13,30,6);Px(g,Color.FromArgb(65,79,105),18,14,28,4);Px(g,metal,27,14,12,4);Px(g,Ink,31,15,4,1);Px(g,Ink,32,16,1,1);foreach(int y in new[]{26,29}){Px(g,Shade(Skin[p.Skin],.5f),21,y,3,1);Px(g,Shade(Skin[p.Skin],.5f),40,y,3,1);}break;
                }
                if(p.Look==1){Poly(g,Ink,44,18,41,16,41,23,45,21,49,23,49,16);Poly(g,cloth,43,18,42,18,42,21,45,20,48,21,48,18,46,19);Px(g,metal,45,19);}
                g.Restore(transform);if(gear.Ornate&&((pose>>2)&7)>0){int sparkle=(pose>>2)&7;Star(g,accent,sparkle%2==0?61:17,sparkle%3==0?38:54,1);}if(gear.Ornate&&hop>0){Star(g,accent,23,24,2);Star(g,accent,58,19,1);}
            }
            if(locked)for(int y=0;y<80;y++)for(int x=0;x<80;x++){var c=image.GetPixel(x,y);if(c.A>0)image.SetPixel(x,y,Color.FromArgb(c.A,43,58,80));}
            return image;
        }
        static Bitmap SpecialCell(int role,int row,int cell){return Cached("s:"+role+":"+row+":"+cell,()=>Atlases[5+Adventure.SpecialSheet(role)].Clone(new Rectangle(cell%4*64,(row+cell/4)*64,64,64),PixelFormat.Format32bppArgb));}
        public static int SpecialPose(int role,int pose,int hop=0){if(role>=12)return hop>0?11:(pose>>12)&15;if(role==11&&hop>0)return 6;if((pose&2)!=0)return 1;int turn=PixelMotion.Direction(pose,5);if(turn!=0)return turn<0?2:3;if(role!=11)return 0;switch((pose>>2)&7){case 1:return 4;case 3:return 5;case 5:return 7;default:return 0;}}
        static Bitmap RenderSpecial(int role,int level,int pose,int hop,int gearLevel,bool locked,bool framed){
            int row=Adventure.SpecialRow(role,gearLevel),cell=SpecialPose(role,pose,hop),bob=role<12&&(pose&1)!=0?-1:0;
            var image=new Bitmap(80,80,PixelFormat.Format32bppArgb);
            using(var g=Graphics.FromImage(image)){Crisp(g);if(framed)Frame(g,level);Px(g,Color.FromArgb(72,0,0,0),26+hop,69,28-hop*2,2);g.TranslateTransform(8,8-hop);
                g.DrawImage(SpecialCell(role,row,cell),new Rectangle(0,bob,64,54-bob),new Rectangle(0,0,64,54),GraphicsUnit.Pixel);
                g.DrawImage(SpecialCell(role,row,0),new Rectangle(0,54,64,10),new Rectangle(0,54,64,10),GraphicsUnit.Pixel);
            }
            if(locked)for(int y=0;y<80;y++)for(int x=0;x<80;x++){var c=image.GetPixel(x,y);if(c.A>0)image.SetPixel(x,y,Color.FromArgb(c.A,43,58,80));}
            return image;
        }
    }
    public sealed class PixelAvatar:Control {
        HeroProfile profile=new HeroProfile();int level=1,pose,hop,cheer;
        readonly Timer clock=new Timer{Interval=120};
        readonly System.Diagnostics.Stopwatch elapsed=System.Diagnostics.Stopwatch.StartNew();
        bool animate=true,locked;
        Form motionOwner;
        public bool Animate{get{return animate;}set{animate=value;UpdateClock();}}
        public bool Locked{get{return locked;}set{locked=value;UpdateClock();Invalidate();}}
        public int GearLevel; public bool PortraitOnly;
        public HeroProfile Profile{get{return profile;}set{profile=value.Copy();UpdateClock();Invalidate();}}
        public int Level{get{return level;}set{level=Math.Max(1,Math.Min(100,value));Invalidate();}}
        public PixelAvatar(){DoubleBuffered=true;Size=new Size(160,160);BackColor=Palette.Card;Cursor=Cursors.Hand;AccessibleName="Pixel adventurer. Click to cheer.";clock.Tick+=(s,e)=>TickPose();Click+=(s,e)=>{if(Animate&&profile.Animations&&!Locked)cheer=8;};}
        void UpdateClock(){clock.Enabled=IsHandleCreated&&Visible&&animate&&!locked&&Form.ActiveForm==FindForm()&&PixelMotion.Allowed(profile.Animations,SystemInformation.HighContrast,PixelMotion.SystemAnimationsEnabled,profile.MotionOverride);if(!clock.Enabled&&(pose!=0||hop!=0)){pose=hop=cheer=0;Invalidate();}}
        void OwnerMotion(object sender,EventArgs e){UpdateClock();}
        protected override void OnHandleCreated(EventArgs e){base.OnHandleCreated(e);motionOwner=FindForm();if(motionOwner!=null){motionOwner.Activated+=OwnerMotion;motionOwner.Deactivate+=OwnerMotion;}UpdateClock();}
        protected override void OnVisibleChanged(EventArgs e){base.OnVisibleChanged(e);UpdateClock();}
        protected override void OnHandleDestroyed(EventArgs e){clock.Stop();if(motionOwner!=null){motionOwner.Activated-=OwnerMotion;motionOwner.Deactivate-=OwnerMotion;motionOwner=null;}base.OnHandleDestroyed(e);}
        void TickPose(){bool moving=Animate&&Visible&&!Locked&&Form.ActiveForm==FindForm()&&PixelMotion.Allowed(profile.Animations,SystemInformation.HighContrast,PixelMotion.SystemAnimationsEnabled,profile.MotionOverride);int next=moving?PixelMotion.At(elapsed.ElapsedMilliseconds):0;int nextHop=moving&&cheer>0?new[]{0,1,2,3,3,2,1,0}[8-cheer--]:0;if(pose!=next||hop!=nextHop){pose=next;hop=nextHop;Invalidate();}}
        protected override void OnPaint(PaintEventArgs e){base.OnPaint(e);if(PortraitOnly){int extent=Math.Min(Width,Height);PortraitCatalog.Draw(e.Graphics,profile,level,GearLevel==0?level:GearLevel,pose,hop,new Rectangle((Width-extent)/2,(Height-extent)/2,extent,extent));return;}using(var image=PixelArt.Render(profile,level,pose,hop,GearLevel,Locked)){PixelArt.Crisp(e.Graphics);int size=Math.Max(1,Math.Min(Width,Height)/80)*80;e.Graphics.DrawImage(image,new Rectangle((Width-size)/2,(Height-size)/2,size,size),0,0,80,80,GraphicsUnit.Pixel);}}
        protected override void Dispose(bool disposing){if(disposing)clock.Stop();base.Dispose(disposing);if(disposing)clock.Dispose();}
    }
    public static class PixelMotion {
        [DllImport("user32.dll",EntryPoint="SystemParametersInfoW")]
        [return:MarshalAs(UnmanagedType.Bool)]
        static extern bool ReadMotion(uint action,uint parameter,[MarshalAs(UnmanagedType.Bool)] out bool value,uint flags);
        public static bool SystemAnimationsEnabled{get{bool enabled;return ReadMotion(0x1042,0,out enabled,0)&&enabled;}}
        public static bool Allowed(bool enabled,bool highContrast,bool systemMotion,bool explicitOptIn){return enabled&&!highContrast&&(systemMotion||explicitOptIn);}
        static int SoloFrame(int c){if(c>=8&&c<=13)return 1;if(c>=14&&c<=19)return 2;if(c>=20&&c<=21||c>=72&&c<=73)return 3;if(c>=29&&c<=31)return 4;if(c>=32&&c<=37)return 5;if(c>=38&&c<=40||c>=44&&c<=46)return 6;if(c>=41&&c<=43)return 7;if(c>=54&&c<=57)return 8;if(c>=58&&c<=63)return 9;if(c>=64&&c<=68)return 10;return 0;}
        public static int Direction(int pose,int shift){int value=(pose>>shift)&3;return value==1?-1:value==2?1:0;}
        public static int At(long milliseconds){int c=(int)(Math.Max(0,milliseconds)%9600/120);int breath=(c>=8&&c<=21||c>=48&&c<=61)?1:0,blink=(c>=20&&c<=21||c==24||c>=72&&c<=73)?2:0,sway=c>=30&&c<=45?1:c>=62&&c<=75?2:0,turn=c>=34&&c<=42?1:c>=68&&c<=74?2:0,hair=(c>=9&&c<=12||c>=49&&c<=52)?1:0;return breath|blink|((c*6/80)<<2)|(sway<<5)|(turn<<7)|(hair<<9)|((c>=36&&c<=42?1:0)<<10)|((c>=68&&c<=74?1:0)<<11)|(SoloFrame(c)<<12);}
    }
    public sealed class XpBar:Control {
        public long Xp;
        public XpBar(){DoubleBuffered=true;Height=12;AccessibleName="Experience progress";}
        protected override void OnPaint(PaintEventArgs e){base.OnPaint(e);int level=Adventure.Level(Xp);double fraction=level==100?1:(double)(Xp-Adventure.Threshold(level))/(Adventure.Threshold(level+1)-Adventure.Threshold(level));for(int i=0;i<20;i++){int x=i*Width/20,w=Math.Max(1,(i+1)*Width/20-x-2);using(var brush=new SolidBrush(i<Math.Ceiling(fraction*20)?PixelArt.RankColours[Adventure.RankIndex(level)]:Palette.Border))e.Graphics.FillRectangle(brush,x,0,w,Height);}}
    }
}
