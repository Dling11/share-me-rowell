using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Windows.Forms;
using ShareMeRowell;

class TransferShowcaseChecks {
    static void Require(bool value,string message){if(!value)throw new Exception(message);}
    static IEnumerable<Control> All(Control root){foreach(Control child in root.Controls){yield return child;foreach(var nested in All(child))yield return nested;}}
    static object Field(object value,string name){return value.GetType().GetField(name,BindingFlags.Instance|BindingFlags.NonPublic).GetValue(value);}
    static void Snapshot(Form form,string path){using(var image=new Bitmap(form.Width,form.Height)){form.DrawToBitmap(image,new Rectangle(Point.Empty,image.Size));image.Save(path);}}
    static void Show(Form form){form.ShowInTaskbar=false;form.StartPosition=FormStartPosition.Manual;form.Location=new Point(-20000,-20000);form.Show();Application.DoEvents();}
    [STAThread]static int Main(string[] args){try{
        Application.EnableVisualStyles();string root=Path.GetFullPath(args[0]);Directory.CreateDirectory(root);var checks=new List<string>();
        var header=new Dictionary<string,object>{{"appearance","5.4.5.4.3.1.13.5"},{"level",82}};var peer=PeerAppearance.Parse(header);Require(peer!=null&&peer.Level==82&&peer.Profile().Role==13&&peer.Profile().Look==1,"Peer appearance failed to round trip");
        Require(PeerAppearance.Parse(new Dictionary<string,object>())==null,"Old peers acquired a fake avatar");foreach(string invalid in new[]{"0.0.0.0.0.0.999.0","-1.0.0.0.0.0.0.0","0.0.0",new string('1',81)}){header["appearance"]=invalid;Require(PeerAppearance.Parse(header)==null,"Malformed optional appearance accepted");}checks.Add("Optional appearance accepts bounded shared styles and ignores old or malformed cosmetics");
        var local=new HeroProfile{Role=23,Animations=true,MotionOverride=true};var connection=new ConnectionSnapshot("Nearby phone",ConnectionPhase.Sending,1,2,peer);
        using(var store=new JourneyStore(Path.Combine(root,"isolated-state")))using(var form=new Form{ClientSize=new Size(360,160),BackColor=Palette.Card}){
            var showcase=new TransferShowcase{Dock=DockStyle.Fill};form.Controls.Add(showcase);Show(form);showcase.Refresh(local,96,connection,true);Application.DoEvents();var cards=All(showcase).OfType<TransferAvatarCard>().ToArray();Require(cards.Length==2,"Both endpoints were not displayed");
            cards[0].ActivateAnimation();cards[1].ActivateAnimation();Require(All(showcase).OfType<CharacterSkillPreview>().All(c=>c.Running),"Both supported avatars did not animate in their frames");Require(Application.OpenForms.Count==1&&store.Xp==0&&store.Profile.Role==0,"Tap opened another form, equipped a peer or awarded XP");Snapshot(form,Path.Combine(root,"both-avatars.png"));checks.Add("Both endpoints use independent in-frame mastery skills without a popup or saved progress change");
            var off=local.Copy();off.Animations=false;showcase.Refresh(off,96,connection,true);foreach(var card in cards)card.ActivateAnimation();Require(All(showcase).OfType<CharacterSkillPreview>().All(c=>!c.Running),"Transfer avatars bypassed Animation Off");checks.Add("Animation Off stops both scenes and prevents tapping from starting background playback");
            showcase.Refresh(local,96,new ConnectionSnapshot("Older Android",ConnectionPhase.Receiving),false);cards[0].ActivateAnimation();Require(!All(cards[0]).OfType<CharacterSkillPreview>().Any(c=>c.Running),"Unknown peer used an invented character");Require(All(cards[0]).OfType<Label>().Any(l=>l.Text=="Older Android"),"Receiving peer order is wrong");checks.Add("Receiving reverses endpoint order and older peers retain an explicit device placeholder");form.Close();
        }
        using(var main=new MainForm(Path.Combine(root,"main"))){Show(main);All(main).OfType<SoftButton>().Single(b=>b.Text=="Receive files").PerformClick();var server=(TransferServer)Field(main,"server");typeof(TransferServer).GetField("connection",BindingFlags.Instance|BindingFlags.NonPublic).SetValue(server,new ConnectionSnapshot("Nearby phone",ConnectionPhase.Receiving,1,2,peer));server.Progress("holiday-photo.jpg",262144,1048576);typeof(MainForm).GetMethod("UpdateConnectionCard",BindingFlags.Instance|BindingFlags.NonPublic).Invoke(main,null);Application.DoEvents();
            foreach(var size in new[]{((Form)main).Size,main.MinimumSize}){((Form)main).Size=size;Application.DoEvents();var showcase=All(main).OfType<TransferShowcase>().Single();Require(showcase.Visible,"Active transfer showcase missing");foreach(var card in All(showcase).OfType<TransferAvatarCard>()){var bounds=new Rectangle(main.PointToClient(card.PointToScreen(Point.Empty)),card.Size);Require(main.ClientRectangle.Contains(bounds)&&card.Width>=80&&card.Height>=100,"A transfer avatar is clipped at a supported size");}Require(All(main).OfType<TransferBar>().Single().Visible,"Avatar cards hid progress");Snapshot(main,Path.Combine(root,size==main.MinimumSize?"transfer-minimum.png":"transfer-desktop.png"));}
            Require(server.Journey.Xp==0&&server.Journey.Profile.Role==0,"Peer's reported level changed the real journey");main.Close();
        }checks.Add("Actual desktop progress and two avatar frames fit default/minimum layouts without changing the journey");
        File.WriteAllText(Path.Combine(root,"result.json"),Wire.Encode(new{passed=checks.Count,checks=checks,artifactDirectory=root,ownerStateUsed=false,phoneUsed=false,liveFocusForced=false}));Console.WriteLine(string.Join(Environment.NewLine,checks));return 0;
    }catch(Exception error){Console.Error.WriteLine(error);return 1;}}
}
