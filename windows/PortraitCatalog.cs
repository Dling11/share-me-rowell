using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.Reflection;

namespace ShareMeRowell {
    // One HD atlas set. Pixel previews derive whole cels without another embedded art set.
    public static class PortraitCatalog {
        static readonly Dictionary<string,Bitmap> atlases=new Dictionary<string,Bitmap>();
        static readonly Queue<string> atlasOrder=new Queue<string>();
        static readonly Dictionary<string,Bitmap> pixels=new Dictionary<string,Bitmap>();
        static readonly Queue<string> pixelOrder=new Queue<string>();
        static readonly string[] names={"luffy","zoro","boa","shanks","eren","levi","mikasa","sukuna","itachi","rock_lee","might_guy","madara"};
        public static string Asset(int role,int look=0){return role==0?(look==1?"custom_girl":"custom_boy"):role==5?"seraph":role==6?"sovereign":role>=12?names[role-12]:role==11?"yugi":role>=8?"special":null;}
        static Bitmap Atlas(int role,int look=0){string name=Asset(role,look);if(name==null)return null;Bitmap result;if(atlases.TryGetValue(name,out result))return result;using(var source=Assembly.GetExecutingAssembly().GetManifestResourceStream("ShareMeRowell.portrait_"+name+".png")){if(source==null)return null;using(var bitmap=new Bitmap(source))result=bitmap.Clone(new Rectangle(0,0,bitmap.Width,bitmap.Height),PixelFormat.Format32bppArgb);}atlases.Add(name,result);atlasOrder.Enqueue(name);while(atlasOrder.Count>4){string oldest=atlasOrder.Dequeue();atlases[oldest].Dispose();atlases.Remove(oldest);}return result;}
        public static bool HasAuthoredPortrait(int role,int look=0){return Atlas(role,look)!=null;}
        static Bitmap PixelCel(Bitmap source,Rectangle crop,string key){Bitmap result;if(pixels.TryGetValue(key,out result))return result;result=new Bitmap(128,128,PixelFormat.Format32bppArgb);using(var g=Graphics.FromImage(result)){g.CompositingMode=CompositingMode.SourceCopy;g.InterpolationMode=InterpolationMode.NearestNeighbor;g.PixelOffsetMode=PixelOffsetMode.Half;g.DrawImage(source,new Rectangle(0,0,128,128),crop,GraphicsUnit.Pixel);}pixels.Add(key,result);pixelOrder.Enqueue(key);while(pixelOrder.Count>48){string oldest=pixelOrder.Dequeue();pixels[oldest].Dispose();pixels.Remove(oldest);}return result;}
        static void Paint(Graphics g,Bitmap source,Rectangle crop,Rectangle bounds,bool pixel,int lift){
            int available=Math.Min(bounds.Width,bounds.Height);if(available<=0)return;
            int extent=pixel?(available>=crop.Width?available/crop.Width*crop.Width:available):Math.Min(available,crop.Width);
            var target=new Rectangle(bounds.X+(bounds.Width-extent)/2,bounds.Y+(bounds.Height-extent)/2-lift,extent,extent);
            g.InterpolationMode=pixel||extent==crop.Width?InterpolationMode.NearestNeighbor:InterpolationMode.HighQualityBicubic;g.PixelOffsetMode=PixelOffsetMode.Half;
            using(var attributes=new ImageAttributes()){attributes.SetWrapMode(WrapMode.TileFlipXY);g.DrawImage(source,target,crop.X,crop.Y,crop.Width,crop.Height,GraphicsUnit.Pixel,attributes);}
        }
        public static void Draw(Graphics graphics,HeroProfile profile,int level,int gearLevel,int pose,int hop,Rectangle bounds){
            var state=graphics.Save();try{
                int role=profile.Role;var atlas=Atlas(role,profile.Look);
                if(atlas!=null){Bitmap source=atlas;int size=atlas.Width/4;Rectangle crop;string key;int lift;
                    if(role==0){source=CustomPortraits.Get(atlas,profile,pose,hop);crop=new Rectangle(0,0,size,size);int eyes=(pose&2)!=0||hop>0?1:profile.Eyes;key="custom:"+profile.Look+":"+profile.Hair+":"+eyes+":"+profile.Skin+":"+profile.HairColour+":"+profile.Shirt+":"+profile.Element;lift=((pose&1)!=0?1:0)+hop;}
                    else{int cell=role==5||role==6?(hop>0?11:(pose>>12)&15):PixelArt.SpecialPose(role,pose,hop);int row=role==5||role==6?(gearLevel>=Adventure.MasteryLevel(role)?3:0):Adventure.SpecialRow(role,gearLevel);crop=new Rectangle(cell%4*size,(row+cell/4)*size,size,size);key=Asset(role)+":"+row+":"+cell;lift=hop*Math.Min(Math.Min(bounds.Width,bounds.Height),size)/96;}
                    if(profile.PixelPreview){source=PixelCel(source,crop,key);crop=new Rectangle(0,0,source.Width,source.Height);}
                    Paint(graphics,source,crop,bounds,profile.PixelPreview,lift);return;
                }
                // Classic artwork already has a pixel palette. Preserve its integer edges.
                using(var image=PixelArt.Render(profile,level,pose,hop,gearLevel,false,false))Paint(graphics,image,new Rectangle(8,8,64,64),bounds,true,0);
            }finally{graphics.Restore(state);}
        }
        public static Bitmap Thumbnail(HeroProfile profile,int level,int gearLevel){var copy=profile.Copy();copy.PixelPreview=false;var image=new Bitmap(112,112,PixelFormat.Format32bppArgb);using(var g=Graphics.FromImage(image)){g.Clear(Color.Transparent);Draw(g,copy,level,gearLevel,0,0,new Rectangle(0,0,112,112));}return image;}
    }
}
