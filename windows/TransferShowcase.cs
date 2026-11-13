using System;
using System.Drawing;
using System.Windows.Forms;

namespace ShareMeRowell {
    public sealed class TransferAvatarCard:Panel {
        readonly PixelAvatar idle=new PixelAvatar{Dock=DockStyle.Fill,PortraitOnly=true,BackColor=Palette.Card};
        readonly CharacterSkillPreview skill=new CharacterSkillPreview{Dock=DockStyle.Fill,Visible=false};
        readonly Label name=new Label{Dock=DockStyle.Bottom,Height=18,TextAlign=ContentAlignment.MiddleCenter,AutoEllipsis=true,UseMnemonic=false};
        readonly Label level=new Label{Dock=DockStyle.Bottom,Height=15,TextAlign=ContentAlignment.MiddleCenter};
        readonly Label unknown=new Label{Dock=DockStyle.Fill,Text="?",TextAlign=ContentAlignment.MiddleCenter};
        HeroProfile profile;int outfitLevel;string key="";
        public TransferAvatarCard(){BackColor=Palette.Card;Padding=new Padding(3);DoubleBuffered=true;name.ForeColor=Palette.Text;name.Font=Palette.Font(8,true);level.ForeColor=Palette.Muted;level.Font=Palette.Font(7);unknown.ForeColor=Palette.Muted;unknown.Font=Palette.Font(20);Controls.Add(idle);Controls.Add(skill);Controls.Add(unknown);Controls.Add(level);Controls.Add(name);idle.Click+=(s,e)=>ActivateAnimation();skill.Click+=(s,e)=>skill.Stop();skill.PlaybackEnded+=(s,e)=>{idle.Visible=profile!=null;skill.Visible=false;Invalidate();};}
        public void Configure(string label,HeroProfile appearance,int at,HeroProfile settings){
            name.Text=label;string next=appearance==null?"unknown":PeerAppearance.Encode(appearance)+":"+at+":"+settings.Animations+":"+settings.MotionOverride+":"+settings.PixelPreview;
            if(next==key)return;key=next;skill.Stop();outfitLevel=at;profile=appearance==null?null:appearance.Copy();
            if(profile!=null){profile.Animations=settings.Animations;profile.MotionOverride=settings.MotionOverride;profile.PixelPreview=settings.PixelPreview;idle.Profile=profile;idle.Level=at;idle.GearLevel=at;}
            unknown.Visible=profile==null;idle.Visible=profile!=null;skill.Visible=false;level.Text=profile==null?"Device avatar unavailable":"Lv. "+at;AccessibleName=label+" avatar. Tap to animate.";Invalidate();
        }
        public void ActivateAnimation(){
            if(profile==null||!profile.Animations)return;
            if(CharacterSkills.HasForOutfit(profile.Role,outfitLevel)&&skill.Start(profile)){idle.Visible=false;skill.Visible=true;skill.BringToFront();}
            else idle.Cheer();Invalidate();
        }
        protected override void OnVisibleChanged(EventArgs e){base.OnVisibleChanged(e);if(!Visible)skill.Stop();}
        protected override void OnPaint(PaintEventArgs e){base.OnPaint(e);using(var pen=new Pen(skill.Running?Palette.Mint:Palette.Border))e.Graphics.DrawRectangle(pen,0,0,Width-1,Height-1);}
    }
    public sealed class TransferShowcase:Panel {
        readonly TransferAvatarCard first=new TransferAvatarCard{Dock=DockStyle.Fill,Margin=Padding.Empty};
        readonly TransferAvatarCard second=new TransferAvatarCard{Dock=DockStyle.Fill,Margin=Padding.Empty};
        readonly TableLayoutPanel row=new TableLayoutPanel{Dock=DockStyle.Fill,ColumnCount=3,RowCount=1,Margin=Padding.Empty,BackColor=Palette.Card};
        readonly Panel middle=new Panel{Dock=DockStyle.Fill,Margin=Padding.Empty};
        bool sending=true;
        public bool ShowAvatars{set{first.Visible=second.Visible=value;row.ColumnStyles[0].Width=row.ColumnStyles[2].Width=value?120:0;}}
        public TransferShowcase(){BackColor=Palette.Card;AccessibleName="Transfer avatars";row.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute,120));row.ColumnStyles.Add(new ColumnStyle(SizeType.Percent,100));row.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute,120));Controls.Add(row);row.Controls.Add(first,0,0);middle.Controls.Add(new Label{Text="→",Dock=DockStyle.Fill,TextAlign=ContentAlignment.MiddleCenter,ForeColor=Palette.Mint,Font=Palette.Font(12,true),Margin=Padding.Empty});row.Controls.Add(middle,1,0);row.Controls.Add(second,2,0);}
        public void SetProgress(Control progress){while(middle.Controls.Count>0)middle.Controls[0].Dispose();progress.Dock=DockStyle.Fill;middle.Controls.Add(progress);}
        public void Refresh(HeroProfile local,int level,ConnectionSnapshot connection,bool localSending){
            if(!connection.Terminal)sending=localSending;
            var mine=sending?first:second;var peer=sending?second:first;
            mine.Configure("You",local,level,local);peer.Configure(connection.Peer,connection.Appearance==null?null:connection.Appearance.Profile(),connection.Appearance==null?1:connection.Appearance.Level,local);
        }
    }
}
