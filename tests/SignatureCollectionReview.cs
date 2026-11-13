using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using ShareMeRowell;

class SignatureCollectionReview {
    static readonly Color Background=Color.FromArgb(22,34,50);
    static void Draw(Graphics g,int role,int level,int x,int y,int pose=0,int hop=0){using(var avatar=PixelArt.Render(new HeroProfile{Role=role},level,pose,hop)){PixelArt.Crisp(g);g.DrawImage(avatar,new Rectangle(x,y,160,160),0,0,80,80,GraphicsUnit.Pixel);}}
    [STAThread] static int Main(string[] args){try{
        string folder=Path.GetFullPath(args[0]);Directory.CreateDirectory(folder);
        using(var image=new Bitmap(1440,908))using(var g=Graphics.FromImage(image))using(var title=new Font("Segoe UI",17,FontStyle.Bold))using(var caption=new Font("Segoe UI",11)){
            g.Clear(Background);g.DrawString("Share Mode · One character, one animation sheet",title,Brushes.White,24,12);g.DrawString("12 authored cels per outfit · Starter / earned mastery · Preview levels only",caption,Brushes.LightGray,24,47);
            for(int index=0;index<12;index++){int role=12+index,x=index%4*360,y=84+index/4*272;int first=Adventure.RoleLevels[role],master=Adventure.FavouriteMasteries[index];
                g.DrawString(Adventure.Roles[role],caption,Brushes.White,x+16,y);Draw(g,role,first,x+12,y+28);Draw(g,role,master,x+184,y+28);
                g.DrawString("Lv "+first+" · "+Adventure.Gear(role,first).Title,caption,Brushes.LightGray,x+12,y+194);g.DrawString("Lv "+master+" · "+Adventure.Gear(role,master).Title,caption,Brushes.LightGray,x+12,y+220);
            }image.Save(Path.Combine(folder,"signature-collection.png"));
        }
        foreach(int role in new[]{12,23}){
            string frames=Path.Combine(folder,role==12?"luffy-frames":"madara-frames");Directory.CreateDirectory(frames);
            for(int frame=0;frame<80;frame++)using(var image=new Bitmap(440,248))using(var g=Graphics.FromImage(image))using(var caption=new Font("Segoe UI",10)){
                g.Clear(Background);int pose=PixelMotion.At(frame*120),hop=frame>=56&&frame<64?new[]{0,1,2,3,3,2,1,0}[frame-56]:0;
                for(int outfit=0;outfit<2;outfit++){int level=outfit==0?Adventure.RoleLevels[role]:Adventure.FavouriteMasteries[role-12];Draw(g,role,level,30+outfit*220,12,pose,hop);g.DrawString(Adventure.Gear(role,level).Title+" · Lv "+level,caption,Brushes.White,10+outfit*220,184);}
                g.DrawString("Actual app renderer · 12 authored cels per outfit · preview",caption,Brushes.LightGray,8,222);image.Save(Path.Combine(frames,"idle-"+frame.ToString("000")+".png"));if(frame==0)image.Save(Path.Combine(folder,role==12?"luffy-preview.png":"madara-preview.png"));
            }
        }
        Console.WriteLine("Saved full collection and two 80-frame actual-app animation previews: "+folder);return 0;
    }catch(Exception error){Console.Error.WriteLine(error);return 1;}}
}
