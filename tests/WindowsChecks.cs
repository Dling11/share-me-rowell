using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading;
using ShareMeRowell;

class WindowsChecks {
    static void Require(bool valid,string message){if(!valid)throw new Exception(message);}
    static int Main(string[] args){try{
        var passed=new List<string>();var lan=new NetworkChoice("192.168.1.55","Ethernet");var wsl=new NetworkChoice("172.29.32.1","vEthernet (WSL)",true);
        Require(NetworkChoice.LocalAddresses(new[]{wsl,lan}).SequenceEqual(new[]{lan.Address}),"WSL was included in normal pairing");passed.Add("Automatic pairing excludes WSL/Docker adapters");
        Require(!NetworkChoice.IsVirtualAdapter("Local Area Connection* 12","Microsoft Wi-Fi Direct Virtual Adapter"),"Phone hotspot was hidden");passed.Add("Wi-Fi Direct hotspot adapters remain available");
        Require(NetworkChoice.LocalAddresses(new[]{wsl}).SequenceEqual(new[]{"127.0.0.1"}),"Virtual-only networking was advertised to phones");passed.Add("Virtual-only networking shows no local phone network");
        var wifi=new NetworkChoice("192.168.43.1","Wi-Fi");Require(NetworkChoice.LocalAddresses(new[]{lan,wifi,lan,wsl}).SequenceEqual(new[]{lan.Address,wifi.Address}),"LAN fallback candidates were not retained");passed.Add("Automatic pairing retains distinct usable LAN candidates");
        string folder=Path.GetFullPath(args[0]);Directory.CreateDirectory(folder);string path=Path.Combine(folder,"prepare-cancel.bin");File.WriteAllBytes(path,new byte[2*1024*1024]);
        string original;using(var input=File.OpenRead(path))original=Wire.Hash(input);
        using(var cancel=new CancellationTokenSource()){bool interrupted=false;try{Source.Prepare(path,cancel.Token,(done,total)=>cancel.Cancel());}catch(OperationCanceledException){interrupted=true;}Require(interrupted,"Preparation ignored cancellation");}
        using(var input=File.OpenRead(path))Require(Wire.Hash(input)==original,"Preparation changed the original file");passed.Add("Cancelling preparation leaves the original file intact");
        Require(Adventure.FileXp(0)==0&&Adventure.FileXp(262143)==0&&Adventure.FileXp(262144)==1&&Adventure.FileXp(1048576)==4&&Adventure.FileXp(long.MaxValue)==50,"XP floor or cap drifted from Android");
        Require(Adventure.Threshold(45)==332992&&Adventure.Threshold(100)==3302937&&Adventure.Level(42)==1&&Adventure.Level(43)==2,"Level costs drifted from Android");passed.Add("Android XP floor, cap and difficult level curve match exactly");
        int[] roles={0,0,0,0,3,4,5,6,6};string[] titles={"Novice Courier","Courier Trainee","Iron Courier","Courier Knight","Shadow Reaper","Demon Warden","Heavenly Seraph","Astral Herald","Astral Sovereign"};for(int i=0;i<Adventure.GearLevels.Length;i++)Require(Adventure.Gear(roles[i],Adventure.GearLevels[i]).Title==titles[i],"Gear milestone is incorrect");
        for(int level=1;level<=100;level++)for(int role=0;role<Adventure.Roles.Length;role++){var gear=Adventure.Gear(role,level);Require(gear.Level<=level,"Future gear was equipped");Require(!gear.Ornate||level>=45,"Ornate gear was granted to a beginner");}Require(!Adventure.CanEquip(6,94)&&Adventure.CanEquip(6,95)&&!Adventure.Gear(6,99).Ornate&&Adventure.Gear(6,100).Ornate,"Ultimate equipment gate failed");passed.Add("Every level gates gear, classes and the level-100 set");
        string state=Path.Combine(folder,"journey");var file=new FileMeta{id="one",name="private-name.txt",size=1048595,sha256=new string('a',64)};
        using(var journey=new JourneyStore(state)){Require(journey.Error==null&&journey.Level==1&&journey.Xp==0,"A new PC did not start as novice");Require(journey.AwardVerified(file)==4&&journey.Xp==4,"Verified credit failed");file.name="renamed.apk";Require(journey.AwardVerified(file)==0&&journey.Files==1,"Renaming awarded duplicate XP");Require(journey.SaveProfile(new HeroProfile{Name="  Pixel\u202e Courier  ",Look=1,Hair=4,HairColour=5,Role=6,Animations=false}),"Profile save failed");Require(journey.Profile.Role==0&&journey.Profile.Look==1,"Locked character equipped or free Girl style rejected");}
        using(var journey=new JourneyStore(state)){Require(journey.Xp==4&&journey.AwardVerified(file)==0&&journey.Files==1,"Restart lost progress or receipts");Require(journey.Profile.Name=="Pixel Courier"&&journey.Profile.HairColour==5&&!journey.Profile.Animations,"Autosaved style did not survive restart");}passed.Add("Progress, duplicate receipts and autosaved appearance survive restart");
        string receipt=Path.Combine(state,"adventurer.receipts");using(var output=new FileStream(receipt,FileMode.Append,FileAccess.Write))output.Write(new byte[]{1,2,3},0,3);
        using(var journey=new JourneyStore(state)){Require(journey.Error==null&&journey.Xp==4&&journey.AwardVerified(file)==0,"Torn-tail recovery changed valid XP");Require(Directory.GetFiles(state,"*.partial-*").Length==1,"Torn tail was not preserved");}passed.Add("Interrupted receipt append preserves previous XP and duplicate protection");
        byte[] damaged=File.ReadAllBytes(receipt);damaged[20]^=1;File.WriteAllBytes(receipt,damaged);
        using(var journey=new JourneyStore(state)){Require(journey.Error!=null&&journey.AwardVerified(file)==0,"Damaged receipt granted new XP");Require(File.ReadAllBytes(receipt).SequenceEqual(damaged),"Damaged ledger was overwritten");}passed.Add("Damaged receipts disable credit and preserve the original ledger");
        var plain=new HeroProfile();using(var novice=PixelArt.Render(plain,1))using(var blink=PixelArt.Render(plain,1,2))using(var idle=PixelArt.Render(plain,1,1))using(var future=PixelArt.Render(new HeroProfile{Role=6},100)){
            Func<System.Drawing.Bitmap,string> digest=image=>{using(var bytes=new MemoryStream()){image.Save(bytes,System.Drawing.Imaging.ImageFormat.Png);bytes.Position=0;return Wire.Hash(bytes);}};
            Require(digest(novice)!=digest(blink)&&digest(novice)!=digest(idle),"Pixel idle/blink did not change the authored artwork");Require(digest(novice)!=digest(future),"Endgame gear matched the novice");using(var prohibited=PixelArt.Render(new HeroProfile{Role=6},1))Require(digest(novice)==digest(prohibited),"Low level rendered a locked role");
        }passed.Add("Bundled pixel faces animate and low levels cannot render advanced gear");
        Require(Adventure.RoleLevels.Skip(7).Take(4).SequenceEqual(new[]{12,40,65,85}),"New character unlocks drifted");foreach(int role in new[]{7,8,9,10}){var signature=Adventure.Signature(new HeroProfile{Look=1},role);Require(signature.Look==1&&signature.Eyes==3,"Signature look removed Girl or focused mood");Require(Adventure.Gear(role,Adventure.RoleLevels[role]).Atlas==2&&!Adventure.Gear(role,Adventure.RoleLevels[role]).Ornate,"New class granted mastery outfit immediately");}passed.Add("Anime and serious characters retain free looks and earned mastery gates");
        for(int role=0;role<Adventure.Roles.Length;role++)foreach(int look in new[]{0,1}){
            var p=Adventure.Signature(new HeroProfile{Hair=5,Look=look},role);p.Hair=5;int level=Adventure.RoleLevels[role];
            using(var rest=PixelArt.Render(p,level))for(int frame=0;frame<80;frame+=4){int pose=PixelMotion.At(frame*120),bob=(pose&1)!=0?-1:0,sway=PixelMotion.Direction(pose,5);using(var current=PixelArt.Render(p,level,pose)){
                for(int y=43+bob;y<=46+bob;y++)Require(current.GetPixel(40+sway,y).A>0,"Head/neck detached in a motion pose");
                for(int y=62;y<=70;y++)for(int x=32;x<=47;x++)Require(rest.GetPixel(x,y)==current.GetPixel(x,y),"Idle motion moved the planted feet");
            }}
        }passed.Add("All class/Look neck connections remain opaque and feet stay planted across idle poses");
        Require(!PixelMotion.Allowed(true,false,false,false)&&PixelMotion.Allowed(true,false,false,true)&&!PixelMotion.Allowed(false,false,true,true)&&!PixelMotion.Allowed(true,true,true,true),"App motion opt-in ignored a user or accessibility setting");passed.Add("Explicit app-only idle opt-in works with Windows reduced motion and preserves Off/high-contrast guards");
        for(int role=8;role<Adventure.Roles.Length;role++)foreach(int level in new[]{Adventure.RoleLevels[role],100}){
            var personal=new HeroProfile{Role=role};var alternate=new HeroProfile{Role=role,Look=1,Hair=5,Skin=4,HairColour=4,Shirt=2,Eyes=1,Element=5};
            using(var first=PixelArt.Render(personal,level,PixelMotion.At(4800)))using(var second=PixelArt.Render(alternate,level,PixelMotion.At(4800))){
                for(int y=0;y<80;y++)for(int x=0;x<80;x++)Require(first.GetPixel(x,y)==second.GetPixel(x,y),"Personal appearance altered a fixed character");
            }
        }passed.Add("Every special character ignores all personal appearance fields at unlock and mastery");
        Require(Adventure.Roles[11]=="Yugi-inspired"&&!Adventure.CanEquip(11,49)&&Adventure.CanEquip(11,50)&&!Adventure.Gear(11,94).Ornate&&Adventure.Gear(11,95).Ornate,"Yugi unlock/mastery drifted");var yugiPoses=new HashSet<int>(Enumerable.Range(0,80).Select(frame=>PixelArt.SpecialPose(11,PixelMotion.At(frame*120))));yugiPoses.Add(PixelArt.SpecialPose(11,0,3));Require(yugiPoses.SetEquals(Enumerable.Range(0,8)),"Yugi card/cheer poses were not reachable");passed.Add("Yugi preserves old unlocks and has eight reachable fixed character poses");
        Require(Adventure.Roles.Length==24&&Adventure.RoleLevels.Take(12).SequenceEqual(new[]{1,8,18,35,55,75,95,12,40,65,85,50}),"Existing character IDs/gates changed");
        for(int role=12;role<24;role++){int first=Adventure.RoleLevels[role],master=Adventure.FavouriteMasteries[role-12];Require(master>first&&!Adventure.CanEquip(role,first-1)&&Adventure.CanEquip(role,first),"Collection unlock gate drifted");Require(!Adventure.Gear(role,master-1).Ornate&&Adventure.Gear(role,master).Ornate&&Adventure.SpecialRow(role,first)==0&&Adventure.SpecialRow(role,master)==3,"Collection mastery was granted early or used the wrong outfit");}passed.Add("Twelve appended characters retain every previous unlock and have separate hard-earned mastery outfits");
        for(int role=12;role<24;role++){
            var poses=new HashSet<int>(Enumerable.Range(0,80).Select(frame=>PixelArt.SpecialPose(role,PixelMotion.At(frame*120))));Require(poses.SetEquals(Enumerable.Range(0,11))&&PixelArt.SpecialPose(role,0,2)==11,"A dedicated idle or cheer cel is unreachable");
            var p=new HeroProfile{Role=role};using(var starter=PixelArt.Render(p,100,0,0,Adventure.RoleLevels[role]))using(var master=PixelArt.Render(p,100,0,0,100)){int changed=0;for(int y=8;y<72;y++)for(int x=8;x<72;x++)if(starter.GetPixel(x,y)!=master.GetPixel(x,y))changed++;Require(changed>100,"Mastery reused the starter artwork");}
            foreach(int frame in Enumerable.Range(0,80))using(var image=PixelArt.Render(p,100,PixelMotion.At(frame*120)))Require(image.Width==80,"Solo frame did not render");
        }passed.Add("Every one-character sheet renders twelve starter/mastery cels with timed in-betweens and tap-only cheers");
        File.WriteAllText(Path.Combine(folder,"result.json"),Wire.Encode(new Dictionary<string,object>{{"passed",passed.Count},{"checks",passed}}));Console.WriteLine(string.Join(Environment.NewLine,passed));return 0;
    }catch(Exception error){Console.Error.WriteLine(error);return 1;}}
}
