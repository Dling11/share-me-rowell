using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Windows.Forms;
using ShareMeRowell;

class PortraitSharpnessChecks {
    static void Require(bool condition,string message){if(!condition)throw new Exception(message);}
    static bool Near(Color a,Color b){return Math.Abs(a.A-b.A)<=1&&Math.Abs(a.R-b.R)<=2&&Math.Abs(a.G-b.G)<=2&&Math.Abs(a.B-b.B)<=2;}
    static IEnumerable<Control> All(Control root){foreach(Control c in root.Controls){yield return c;foreach(var child in All(c))yield return child;}}
    static Bitmap Render(HeroProfile profile,int extent,int pose=0){var result=new Bitmap(extent,extent,PixelFormat.Format32bppArgb);using(var g=Graphics.FromImage(result)){g.Clear(Color.Transparent);PortraitCatalog.Draw(g,profile,100,100,pose,0,new Rectangle(0,0,extent,extent));}return result;}
    static void Snapshot(Form form,string path){Application.DoEvents();using(var image=new Bitmap(form.Width,form.Height)){form.DrawToBitmap(image,new Rectangle(Point.Empty,image.Size));image.Save(path);}}
    [STAThread] static int Main(string[] args){try{
        Application.EnableVisualStyles();string folder=Path.GetFullPath(args[0]),root=Path.GetFullPath(args[1]);Directory.CreateDirectory(folder);var checks=new List<string>();
        foreach(int role in Enumerable.Range(0,Adventure.Roles.Length).Where(Adventure.IsFixedDesign)){
            string asset=PortraitCatalog.Asset(role);using(var stream=typeof(PortraitCatalog).Assembly.GetManifestResourceStream("ShareMeRowell.portrait_"+asset+".png"))using(var atlas=new Bitmap(stream)){
                Require(atlas.Width==1024,"HD atlas was reduced before rendering");int row=role==5||role==6?3:Adventure.SpecialRow(role,100),cell=role==5||role==6?0:PixelArt.SpecialPose(role,0,0);
                foreach(int extent in new[]{256,384})using(var rendered=Render(new HeroProfile{Role=role},extent)){
                    int compared=0,offset=(extent-256)/2;for(int y=0;y<256;y++)for(int x=0;x<256;x++){Color source=atlas.GetPixel(cell%4*256+x,(row+cell/4)*256+y);if(source.A>=240){Require(Near(rendered.GetPixel(offset+x,offset+y),source),"HD rendering changed source detail beyond GDI alpha rounding: "+role);compared++;}}
                    Require(compared>3000,"Insufficient complete portrait content");if(extent>256)Require(rendered.GetPixel(30,30).A==0,"HD portrait was enlarged beyond its native resolution");
                }
            }
        }checks.Add("Every fixed HD portrait preserves source detail at native and larger preview bounds within GDI alpha rounding");
        foreach(var profile in new[]{new HeroProfile{Role=23,PixelPreview=true},new HeroProfile{Look=1,Hair=3,PixelPreview=true}})using(var rendered=Render(profile,256)){
            int visible=0;for(int y=0;y<256;y+=2)for(int x=0;x<256;x+=2){int first=rendered.GetPixel(x,y).ToArgb();for(int dy=0;dy<2;dy++)for(int dx=0;dx<2;dx++)Require(rendered.GetPixel(x+dx,y+dy).ToArgb()==first,"Pixel mode blended adjacent blocks");if(rendered.GetPixel(x,y).A>128)visible++;}Require(visible>2000,"Pixel portrait was incomplete");
        }checks.Add("Personal and fixed Pixel previews use exact uniform two-by-two blocks without smoothing");
        using(var first=Render(new HeroProfile(),256))using(var second=Render(new HeroProfile(),256,1)){int compared=0;for(int y=1;y<256;y++)for(int x=0;x<256;x++)if(first.GetPixel(x,y).A>=240){Require(Near(first.GetPixel(x,y),second.GetPixel(x,y-1)),"Idle breathing stretched the portrait");compared++;}Require(compared>3000,"Idle test had insufficient visible content");}checks.Add("Personal-avatar breathing moves whole pixels instead of stretching the face or neck");
        for(int pass=0;pass<3;pass++)foreach(int role in Enumerable.Range(0,24))using(var image=Render(new HeroProfile{Role=role,Look=pass%2,Hair=pass,PixelPreview=pass%2==0},256))Require(image.Width==256,"Cache eviction invalidated a portrait");checks.Add("Repeated full-collection rendering survives HD and Pixel cache eviction");
        string state=Path.Combine(folder,"isolated-state");using(var store=new JourneyStore(state))using(var form=new AdventurerForm(store,()=>{})){
            form.ShowInTaskbar=false;form.StartPosition=FormStartPosition.Manual;form.Location=new Point(-20000,-20000);form.Show();Application.DoEvents();Require(!store.Profile.PixelPreview,"An older/default profile did not start in HD");
            All(form).OfType<CharacterTile>().Single(c=>c.RoleIndex==23).PerformClick();var hd=All(form).OfType<SoftButton>().Single(c=>c.AccessibleName=="HD portrait preview");var pixel=All(form).OfType<SoftButton>().Single(c=>c.AccessibleName=="Pixel portrait preview");hd.PerformClick();Snapshot(form,Path.Combine(folder,"studio-hd.png"));pixel.PerformClick();Snapshot(form,Path.Combine(folder,"studio-pixel.png"));Require(pixel.Selected&&!hd.Selected&&store.Profile.PixelPreview,"Pixel selector did not autosave");Require(store.Profile.Role==0&&store.Xp==0,"Changing preview mode equipped a locked character or granted XP");
            foreach(var size in new[]{form.Size,form.MinimumSize}){form.Size=size;Application.DoEvents();foreach(var mode in new[]{hd,pixel}){var area=new Rectangle(form.PointToClient(mode.PointToScreen(Point.Empty)),mode.Size);Require(form.ClientRectangle.Contains(area)&&mode.Width>=40&&mode.Height>=20,"A preview-mode control was clipped at a supported size");}Require(!hd.Bounds.IntersectsWith(pixel.Bounds),"Preview-mode buttons overlapped");}checks.Add("HD and Pixel controls remain visible and separate at default and minimum window sizes");form.Close();
        }
        using(var store=new JourneyStore(state)){Require(store.Profile.PixelPreview&&store.Profile.Role==0&&store.Xp==0,"Pixel preference did not survive restart");using(var form=new AdventurerForm(store,()=>{})){form.ShowInTaskbar=false;form.StartPosition=FormStartPosition.Manual;form.Location=new Point(-20000,-20000);form.Show();Application.DoEvents();All(form).OfType<SoftButton>().Single(c=>c.AccessibleName=="HD portrait preview").PerformClick();form.Close();}}
        using(var store=new JourneyStore(state))Require(!store.Profile.PixelPreview&&store.Xp==0,"HD preference did not survive restart");checks.Add("HD and Pixel controls autosave across restarts without changing equipment or XP");
        // Optional owner comparison; the regression suite needs no historical installation or private data.
        string baselineFile=Path.Combine(root,".local","sharp-baseline-1.2.8.json");if(File.Exists(baselineFile)){
        var baseline=Wire.Decode(File.ReadAllText(baselineFile));string backup=Convert.ToString(baseline["backup"]);
        using(var board=new Bitmap(824,630))using(var g=Graphics.FromImage(board))using(var title=new Font("Segoe UI",13,FontStyle.Bold))using(var small=new Font("Segoe UI",10)){
            g.Clear(Color.FromArgb(19,24,34));string[] labels={"Before: smoothed 128 px","HD: native 256 px","Pixel: crisp 2x blocks"};for(int i=0;i<3;i++)g.DrawString(labels[i],small,Brushes.White,new PointF(16+i*270,14));
            int index=0;foreach(int role in new[]{12,23}){int y=48+index*285;string asset=PortraitCatalog.Asset(role);using(var old=new Bitmap(Path.Combine(backup,"windows","art","portrait_"+asset+".png"))){g.InterpolationMode=InterpolationMode.HighQualityBicubic;g.PixelOffsetMode=PixelOffsetMode.HighQuality;g.DrawImage(old,new Rectangle(16,y,256,256),new Rectangle(0,0,128,128),GraphicsUnit.Pixel);}foreach(bool pixel in new[]{false,true}){var profile=new HeroProfile{Role=role,PixelPreview=pixel};PortraitCatalog.Draw(g,profile,100,Adventure.RoleLevels[role],0,0,new Rectangle(pixel?556:286,y,256,256));}g.DrawString(Adventure.Roles[role],title,Brushes.White,16,y+252);index++;}board.Save(Path.Combine(folder,"portrait-comparison.png"));
        }}
        File.WriteAllText(Path.Combine(folder,"result.json"),Wire.Encode(new{passed=checks.Count,checks=checks,artifactDirectory=folder,phoneUsed=false}));Console.WriteLine(string.Join(Environment.NewLine,checks));return 0;
    }catch(Exception error){Console.Error.WriteLine(error);return 1;}}
}
