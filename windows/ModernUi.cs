using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Runtime.InteropServices;
using System.Windows.Forms;

namespace ShareMeRowell {
    public static class Palette {
        public static readonly Color Page=Color.FromArgb(12,19,30);
        public static readonly Color Sidebar=Color.FromArgb(15,24,38);
        public static readonly Color Card=Color.FromArgb(22,34,50);
        public static readonly Color Raised=Color.FromArgb(29,45,63);
        public static readonly Color Mint=Color.FromArgb(112,226,194);
        public static readonly Color Text=Color.FromArgb(245,248,255);
        public static readonly Color Muted=Color.FromArgb(156,175,195);
        public static readonly Color Border=Color.FromArgb(39,57,76);
        public static GraphicsPath Round(RectangleF box,float radius) {
            var p=new GraphicsPath();float d=Math.Min(radius*2,Math.Min(box.Width,box.Height));
            p.AddArc(box.X,box.Y,d,d,180,90);p.AddArc(box.Right-d,box.Y,d,d,270,90);
            p.AddArc(box.Right-d,box.Bottom-d,d,d,0,90);p.AddArc(box.X,box.Bottom-d,d,d,90,90);p.CloseFigure();return p;
        }
        public static Font Font(float size,bool bold=false){return new Font(bold?"Segoe UI Semibold":"Segoe UI",size,FontStyle.Regular);}
    }
    public sealed class SoftCard:Panel {
        public Color Fill=Palette.Card;
        public bool Dashed;
        public SoftCard(){DoubleBuffered=true;BackColor=Palette.Page;Padding=new Padding(22);}
        protected override void OnPaintBackground(PaintEventArgs e){e.Graphics.Clear(BackColor);if(Width<4||Height<4)return;e.Graphics.SmoothingMode=SmoothingMode.AntiAlias;using(var path=Palette.Round(new RectangleF(1,1,Width-3,Height-3),22))using(var brush=new SolidBrush(Fill))using(var border=new Pen(Palette.Border,1)){if(Dashed)border.DashStyle=DashStyle.Dash;e.Graphics.FillPath(brush,path);e.Graphics.DrawPath(border,path);}}
    }
    public sealed class SoftButton:Button {
        public bool Primary,Selected,Ghost;
        private bool hover;
        public SoftButton(){FlatStyle=FlatStyle.Flat;FlatAppearance.BorderSize=0;Cursor=Cursors.Hand;Font=Palette.Font(10,true);Height=46;DoubleBuffered=true;BackColor=Palette.Card;}
        protected override void OnMouseEnter(EventArgs e){hover=true;Invalidate();base.OnMouseEnter(e);}
        protected override void OnMouseLeave(EventArgs e){hover=false;Invalidate();base.OnMouseLeave(e);}
        protected override void OnPaint(PaintEventArgs e){
            e.Graphics.SmoothingMode=SmoothingMode.AntiAlias;
            e.Graphics.Clear(BackColor);
            Color fill=Primary?Palette.Mint:(Selected?Color.FromArgb(30,60,65):(hover?Palette.Raised:Ghost?BackColor:Palette.Card));
            if(!Enabled)fill=Palette.Raised;
            using(var path=Palette.Round(new RectangleF(1,1,Width-3,Height-3),13))using(var brush=new SolidBrush(fill))using(var pen=new Pen(Primary||Selected||Ghost?fill:Palette.Border,1)){e.Graphics.FillPath(brush,path);e.Graphics.DrawPath(pen,path);}
            TextRenderer.DrawText(e.Graphics,Text,Font,ClientRectangle,!Enabled?Palette.Muted:Primary?Palette.Page:Selected?Palette.Mint:Palette.Text,TextFormatFlags.HorizontalCenter|TextFormatFlags.VerticalCenter|TextFormatFlags.EndEllipsis);
        }
    }
    public sealed class TransferBar:Control {
        private int amount;
        public int Value {get{return amount;}set{amount=Math.Max(0,Math.Min(100,value));Invalidate();}}
        public TransferBar(){Height=8;DoubleBuffered=true;}
        protected override void OnPaint(PaintEventArgs e){e.Graphics.Clear(BackColor);if(Width<5)return;e.Graphics.SmoothingMode=SmoothingMode.AntiAlias;using(var path=Palette.Round(new RectangleF(0,0,Width-1,Height-1),4))using(var brush=new SolidBrush(Palette.Border))e.Graphics.FillPath(brush,path);if(amount>0)using(var path=Palette.Round(new RectangleF(0,0,Math.Max(8,(Width-1)*amount/100f),Height-1),4))using(var brush=new SolidBrush(Palette.Mint))e.Graphics.FillPath(brush,path);}
    }
    public static class Brand {
        public static Bitmap Bitmap(int size){
            var image=new Bitmap(size,size);using(var g=Graphics.FromImage(image)){
                g.SmoothingMode=SmoothingMode.AntiAlias;g.Clear(Color.Transparent);g.ScaleTransform(size/256f,size/256f);
                using(var path=Palette.Round(new RectangleF(8,8,240,240),60))using(var brush=new SolidBrush(Color.FromArgb(20,35,58)))g.FillPath(brush,path);
                using(var mint=new Pen(Palette.Mint,22))using(var white=new Pen(Palette.Text,22)){
                    mint.StartCap=mint.EndCap=white.StartCap=white.EndCap=LineCap.Round;mint.LineJoin=white.LineJoin=LineJoin.Round;
                    using(var p=new GraphicsPath()){p.AddLine(177,80,105,80);p.AddBezier(105,80,77,80,77,121,105,121);p.AddLine(105,121,151,121);g.DrawPath(mint,p);}
                    g.DrawLines(mint,new[]{new Point(161,64),new Point(177,80),new Point(161,96)});
                    using(var p=new GraphicsPath()){p.AddLine(79,176,151,176);p.AddBezier(151,176,179,176,179,135,151,135);p.AddLine(151,135,105,135);g.DrawPath(white,p);}
                    g.DrawLines(white,new[]{new Point(95,160),new Point(79,176),new Point(95,192)});
                }
            }return image;
        }
        [DllImport("user32.dll")]private static extern bool DestroyIcon(IntPtr handle);
        public static void Export(string folder){System.IO.Directory.CreateDirectory(folder);using(var bitmap=Bitmap(512)){bitmap.Save(System.IO.Path.Combine(folder,"icon-preview.png"));}using(var bitmap=Bitmap(256)){IntPtr handle=bitmap.GetHicon();try{using(var icon=Icon.FromHandle(handle))using(var stream=System.IO.File.Create(System.IO.Path.Combine(folder,"shareme.ico")))icon.Save(stream);}finally{DestroyIcon(handle);}}}
    }
}
