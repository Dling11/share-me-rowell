using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Windows.Forms;
using ShareMeRowell;

class SpecialCharacterChecks {
    static void Require(bool valid,string message){if(!valid)throw new Exception(message);}
    static IEnumerable<Control> All(Control root){foreach(Control child in root.Controls){yield return child;foreach(var nested in All(child))yield return nested;}}
    static void EquipThroughPicker(AdventurerForm editor,int role){All(editor).OfType<SoftButton>().Single(c=>c.Text=="Characters").PerformClick();Application.DoEvents();All(editor).OfType<CharacterTile>().Single(c=>c.RoleIndex==role).PerformClick();Application.DoEvents();var action=All(editor).OfType<SoftButton>().Single(c=>c.AccessibleName=="Equip previewed character");Require(action.Enabled||action.Text=="Equipped","Earned character cannot be equipped");action.PerformClick();Application.DoEvents();}
    static bool SameLook(HeroProfile a,HeroProfile b){return a.Hair==b.Hair&&a.Skin==b.Skin&&a.HairColour==b.HairColour&&a.Shirt==b.Shirt&&a.Eyes==b.Eyes&&a.Look==b.Look&&a.Element==b.Element;}
    // Isolated, labelled old-progress fixture. No real installation or transfer history is used.
    static void SeedJourney(string folder,int level=40){Directory.CreateDirectory(folder);using(var file=File.Create(Path.Combine(folder,"adventurer.receipts")))using(var sha=SHA256.Create()){
        var header=System.Text.Encoding.ASCII.GetBytes("SMXP001\n");file.Write(header,0,header.Length);
        int count=(int)((Adventure.Threshold(level)+49)/50);
        for(int i=0;i<count;i++){var payload=new byte[44];Array.Copy(BitConverter.GetBytes(i+1),0,payload,0,4);Array.Copy(BitConverter.GetBytes(13107200L),0,payload,32,8);Array.Copy(BitConverter.GetBytes(50),0,payload,40,4);file.Write(payload,0,payload.Length);var checksum=sha.ComputeHash(payload);file.Write(checksum,0,checksum.Length);}file.Flush(true);
    }}
    [STAThread] static int Main(string[] args){try{
        string folder=Path.GetFullPath(args[0]),state=Path.Combine(folder,"fixed-character-fixture");SeedJourney(state);var passed=new List<string>();
        var personal=new HeroProfile{Name="Preview courier",Hair=3,Skin=4,HairColour=5,Shirt=2,Eyes=1,Look=1,Element=5};long xp;
        using(var store=new JourneyStore(state)){
            xp=store.Xp;Require(store.Level==40&&store.SaveProfile(personal),"Could not load the labelled progress fixture");
            var equip=personal.Copy();equip.Role=8;equip.Hair=0;equip.Skin=0;equip.Look=0;Require(store.SaveProfile(equip)&&store.Profile.Role==8&&SameLook(store.Profile,personal),"Equipping a special character overwrote personal style");
            var attempt=store.Profile.Copy();attempt.Hair=5;attempt.HairColour=0;attempt.Element=0;attempt.Name="Teacher device";Require(store.SaveProfile(attempt)&&SameLook(store.Profile,personal)&&store.Profile.Name=="Teacher device","Fixed-character edits changed personal style or blocked the device name");passed.Add("Fixed-character save preserves personal appearance while allowing device-name edits");
            using(var editor=new AdventurerForm(store,()=>{})){
                editor.ShowInTaskbar=false;editor.StartPosition=FormStartPosition.Manual;editor.Location=new Point(-20000,-20000);editor.Show();Application.DoEvents();All(editor).OfType<SoftButton>().Single(c=>c.Text=="Style").PerformClick();Application.DoEvents();
                var choices=All(editor).OfType<StyleOption>().ToArray();Require(choices.Length==34&&choices.All(c=>!c.Visible&&!c.Enabled),"Signature style exposed editable personal controls");Require(All(editor).OfType<Label>().Any(c=>c.Text=="Signature character · fixed design"&&c.Visible),"Fixed-character explanation is missing");
                using(var image=new Bitmap(editor.Width,editor.Height)){editor.DrawToBitmap(image,new Rectangle(0,0,editor.Width,editor.Height));image.Save(Path.Combine(folder,"fixed-character-style.png"));}passed.Add("Real WinForms signature screen hides customization and clearly explains the fixed design");
                All(editor).OfType<SoftButton>().First(c=>c.Text=="Characters").PerformClick();Application.DoEvents();EquipThroughPicker(editor,0);Application.DoEvents();All(editor).OfType<SoftButton>().First(c=>c.Text=="Style").PerformClick();Application.DoEvents();Require(store.Profile.Role==0&&SameLook(store.Profile,personal)&&choices.All(c=>c.Visible&&c.Enabled),"Returning to a normal avatar did not restore controls and appearance");
                using(var image=new Bitmap(editor.Width,editor.Height)){editor.DrawToBitmap(image,new Rectangle(0,0,editor.Width,editor.Height));image.Save(Path.Combine(folder,"personal-character-restored.png"));}editor.Close();passed.Add("Equip switches back to a normal character with the saved style and controls restored");
            }
            Require(store.Xp==xp,"Character selection changed earned XP");
        }
        using(var store=new JourneyStore(state)){Require(store.Xp==xp&&SameLook(store.Profile,personal)&&store.Profile.Role==0,"Restart lost personal style or XP");}passed.Add("Appearance and existing progress remain intact after restart");
        string cardState=Path.Combine(folder,"yugi-level50-fixture");SeedJourney(cardState,50);long cardXp;
        using(var store=new JourneyStore(cardState)){
            Require(store.Level==50&&store.SaveProfile(personal),"Could not load the labelled Level50 fixture");cardXp=store.Xp;
            using(var editor=new AdventurerForm(store,()=>{})){
                editor.ShowInTaskbar=false;editor.StartPosition=FormStartPosition.Manual;editor.Location=new Point(-20000,-20000);editor.Show();Application.DoEvents();All(editor).OfType<SoftButton>().Single(c=>c.Text=="Style").PerformClick();Application.DoEvents();
                All(editor).OfType<SoftButton>().First(c=>c.Text=="Characters").PerformClick();Application.DoEvents();
                EquipThroughPicker(editor,11);Application.DoEvents();
                All(editor).OfType<SoftButton>().First(c=>c.Text=="Style").PerformClick();Application.DoEvents();
                Require(store.Profile.Role==11&&SameLook(store.Profile,personal)&&All(editor).OfType<StyleOption>().All(c=>!c.Visible&&!c.Enabled),"Yugi did not equip as a fixed signature");
                using(var image=new Bitmap(editor.Width,editor.Height)){editor.DrawToBitmap(image,new Rectangle(0,0,editor.Width,editor.Height));image.Save(Path.Combine(folder,"yugi-character-style.png"));}editor.Close();
            }
            Require(store.Xp==cardXp,"Yugi selection changed earned XP");passed.Add("Yugi equips through the actual character picker at Level50 with fixed controls and unchanged XP");
        }
        using(var store=new JourneyStore(cardState)){Require(store.Xp==cardXp&&store.Profile.Role==11&&SameLook(store.Profile,personal),"Restart lost Yugi or personal style");var normal=store.Profile.Copy();normal.Role=0;Require(store.SaveProfile(normal)&&SameLook(store.Profile,personal),"Switching from Yugi lost personal style");}passed.Add("Yugi selection survives restart and switching back restores personal style");
        string collectionState=Path.Combine(folder,"collection-level100-fixture");SeedJourney(collectionState,100);long collectionXp;
        using(var store=new JourneyStore(collectionState)){
            Require(store.Level==100&&store.SaveProfile(personal),"Could not load isolated collection fixture");collectionXp=store.Xp;
            using(var editor=new AdventurerForm(store,()=>{})){
                editor.ShowInTaskbar=false;editor.StartPosition=FormStartPosition.Manual;editor.Location=new Point(-20000,-20000);editor.Show();Application.DoEvents();All(editor).OfType<SoftButton>().Single(c=>c.Text=="Style").PerformClick();Application.DoEvents();
                foreach(int role in Enumerable.Range(0,Adventure.Roles.Length).Where(Adventure.IsFixedDesign)){
                    All(editor).OfType<SoftButton>().First(c=>c.Text=="Characters").PerformClick();Application.DoEvents();EquipThroughPicker(editor,role);Application.DoEvents();All(editor).OfType<SoftButton>().First(c=>c.Text=="Style").PerformClick();Application.DoEvents();
                    Require(store.Profile.Role==role&&SameLook(store.Profile,personal)&&All(editor).OfType<StyleOption>().All(c=>!c.Visible&&!c.Enabled),"Collection signature exposed customization or lost personal style");
                    if(role==12||role==23)using(var image=new Bitmap(editor.Width,editor.Height)){editor.DrawToBitmap(image,new Rectangle(0,0,editor.Width,editor.Height));image.Save(Path.Combine(folder,role==12?"luffy-character-style.png":"madara-character-style.png"));}
                }
                editor.Close();
            }
            Require(store.Xp==collectionXp,"Collection selection changed XP");passed.Add("All eighteen fixed signatures equip in the real picker with fixed controls and unchanged personal style and XP");
        }
        using(var store=new JourneyStore(collectionState)){Require(store.Xp==collectionXp&&store.Profile.Role==23&&SameLook(store.Profile,personal),"Collection choice or saved style was lost after restart");var normal=store.Profile.Copy();normal.Role=0;Require(store.SaveProfile(normal)&&SameLook(store.Profile,personal),"Returning from the collection lost personal appearance");}passed.Add("Collection selection persists after restart and returning to a normal avatar restores personal appearance");
        File.WriteAllText(Path.Combine(folder,"result.json"),Wire.Encode(new{passed=passed.Count,checks=passed,realInstallationUsed=false,fixture="Isolated labelled Level40, Level50 and Level100 progress; no real XP or unlocks changed"}));Console.WriteLine(string.Join(Environment.NewLine,passed));return 0;
    }catch(Exception error){Console.Error.WriteLine(error);return 1;}}
}
