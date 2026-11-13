using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using ShareMeRowell;

class AvatarMotionReview {
    static readonly Color Background=Color.FromArgb(22,34,50);
    static void Draw(Graphics g,HeroProfile p,int level,int x,int y,int pose=0,int hop=0){using(var image=PixelArt.Render(p,level,pose,hop)){PixelArt.Crisp(g);g.DrawImage(image,new Rectangle(x,y,160,160),0,0,80,80,GraphicsUnit.Pixel);}}
    [STAThread] static int Main(string[] args){try{
        string folder=Path.GetFullPath(args[0]);Directory.CreateDirectory(folder);string frames=Path.Combine(folder,"frames");Directory.CreateDirectory(frames);
        string yugiFrames=Path.Combine(folder,"yugi-frames");Directory.CreateDirectory(yugiFrames);
        int[] roles={0,8,9,10,11},first={1,40,65,85,50},master={25,80,90,100,95};
        using(var image=new Bitmap(1000,536))using(var g=Graphics.FromImage(image))using(var title=new Font("Segoe UI",14,FontStyle.Bold))using(var caption=new Font("Segoe UI",10)){
            g.Clear(Background);g.DrawString("Share Mode · Personal avatars and signature characters",title,Brushes.White,20,12);g.DrawString("Original RPG avatars are customizable. Anime heroes have fixed designs. Preview only.",caption,Brushes.LightGray,20,42);
            for(int row=0;row<2;row++)for(int column=0;column<roles.Length;column++){
                int role=roles[column],level=row==0?first[column]:master[column];var p=Adventure.Signature(new HeroProfile{Hair=row==0?1:3,HairColour=5,Look=row,Skin=row==0?0:4,Shirt=row==0?0:2},role);int x=column*200+20,y=76+row*224;
                Draw(g,p,level,x,y);g.DrawString(Adventure.Gear(role,level).Title,caption,Brushes.White,column*200+10,y+164);g.DrawString("Lv "+level+" · "+(Adventure.IsSpecial(role)?"Fixed design":p.Look==0?"Boy":"Girl"),caption,Brushes.LightGray,column*200+10,y+188);
            }
            image.Save(Path.Combine(folder,"avatar-lineup.png"));
        }
        int[] movingRoles={0,8,9,10,11},levels={1,80,90,100,95};
        for(int frame=0;frame<80;frame++)using(var image=new Bitmap(1000,248))using(var g=Graphics.FromImage(image))using(var caption=new Font("Segoe UI",10)){
            g.Clear(Background);int pose=PixelMotion.At(frame*120);int hop=frame>=56&&frame<64?new[]{0,1,2,3,3,2,1,0}[frame-56]:0;
            for(int i=0;i<movingRoles.Length;i++){var p=Adventure.Signature(new HeroProfile{Hair=1,HairColour=5},movingRoles[i]);Draw(g,p,levels[i],i*200+20,16,pose,hop);g.DrawString(Adventure.Gear(movingRoles[i],levels[i]).Title,caption,Brushes.White,i*200+10,184);}
            g.DrawString("Actual renderer · personal-avatar rig and dedicated full-character idle cels",caption,Brushes.LightGray,12,222);
            image.Save(Path.Combine(frames,"idle-"+frame.ToString("000")+".png"));
            using(var duo=new Bitmap(440,248))using(var duoGraphics=Graphics.FromImage(duo)){
                duoGraphics.Clear(Background);for(int costume=0;costume<2;costume++){int level=costume==0?50:95;Draw(duoGraphics,Adventure.Signature(new HeroProfile(),11),level,30+costume*220,12,pose,hop);duoGraphics.DrawString(Adventure.Gear(11,level).Title+" · Lv "+level,caption,Brushes.White,14+costume*220,184);}
                duoGraphics.DrawString("Actual app renderer · fixed signature · preview levels",caption,Brushes.LightGray,12,222);duo.Save(Path.Combine(yugiFrames,"idle-"+frame.ToString("000")+".png"));if(frame==0)duo.Save(Path.Combine(folder,"yugi-preview.png"));
            }
        }
        Console.WriteLine("Saved the actual-renderer lineup and 80 motion frames: "+folder);return 0;
    }catch(Exception error){Console.Error.WriteLine(error);return 1;}}
}
