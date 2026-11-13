using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Web.Script.Serialization;

namespace ShareMeRowell {
    public sealed class HeroGear {
        public readonly int Atlas,Pair,Level;
        public readonly string Title;
        public readonly bool Ornate;
        public HeroGear(int atlas,int pair,string title,int level,bool ornate=false){Atlas=atlas;Pair=pair;Title=title;Level=level;Ornate=ornate;}
    }
    public static class Adventure {
        public static readonly string[] Roles={"Courier","Rune Mage","Forest Ranger","Shadow Reaper","Demon Warden","Heavenly Seraph","Astral Sovereign","Silent Ronin","Gojo-inspired","Saitama-inspired","Naruto-inspired","Yugi-inspired","Luffy-inspired","Zoro-inspired","Boa Hancock-inspired","Shanks-inspired","Eren-inspired","Levi-inspired","Mikasa-inspired","Sukuna-inspired","Itachi-inspired","Rock Lee-inspired","Might Guy-inspired","Madara-inspired"};
        public static readonly int[] RoleLevels={1,8,18,35,55,75,95,12,40,65,85,50,20,28,32,48,36,58,46,88,68,24,78,96};
        public static readonly int[] FavouriteMasteries={80,82,84,90,86,92,88,99,94,76,98,100};
        public static readonly string[] FavouriteTitles={"Straw Hat Captain","Three-Sword Wanderer","Pirate Empress","Red-Haired Captain","Beyond the Walls","Scout Captain","Crimson Scarf","Cursed Sovereign","Crimson Crow","Youthful Fighter","Radiant Mentor","Crimson Legend"};
        public static readonly string[] FavouriteMasterTitles={"Liberated Captain","Emerald Swordsman","Serpent Empress","Emperor of the Sea","Freedom Seeker","Humanity's Strongest","Unbroken Protector","King of Curses","Scarlet Illusionist","Lotus Champion","Night Mentor","Six-Paths Sovereign"};
        public static HeroProfile Signature(HeroProfile source,int role){var p=source.Copy();p.Role=role;switch(role){case 7:p.Hair=0;p.HairColour=0;p.Eyes=3;p.Shirt=1;break;case 8:p.Hair=2;p.HairColour=5;p.Eyes=3;p.Element=2;break;case 9:p.Hair=5;p.Eyes=3;p.Shirt=3;p.Element=5;break;case 10:p.Hair=2;p.HairColour=2;p.Eyes=3;p.Shirt=3;p.Element=3;break;}return p;}
        public static readonly string[] Ranks={"Novice","Adventurer","Vanguard","Elemental","Shadow","Demonic","Heavenly","God-tier"};
        public static readonly int[] RankLevels={1,8,18,30,45,60,80,100};
        public static readonly int[] GearLevels={1,10,25,45,60,75,85,95,100};
        public static readonly int[] GearRoles={0,0,0,0,3,4,5,6,6};
        public static readonly string[] Elements={"Aether","Fire","Water","Wind","Shadow","Light"};
        public static long Threshold(int level){long n=Math.Max(1,Math.Min(100,level))-1;return 40*n*n+3*n*n*n;}
        public static int Level(long xp){int level=1;while(level<100&&xp>=Threshold(level+1))level++;return level;}
        public static int RankIndex(int level){int rank=0;while(rank<7&&level>=RankLevels[rank+1])rank++;return rank;}
        public static int FileXp(long bytes){return (int)Math.Min(50,Math.Max(0,bytes)/262144);}
        public static bool CanEquip(int role,int level){return role>=0&&role<Roles.Length&&level>=RoleLevels[role];}
        public static bool IsSpecial(int role){return role>=8&&role<Roles.Length;}
        public static bool IsFixedDesign(int role){return role==5||role==6||IsSpecial(role);}
        public static int MasteryLevel(int role){if(role>=12)return FavouriteMasteries[role-12];return new[]{45,45,45,60,75,85,100,55,80,90,100,95}[role];}
        public static int SpecialSheet(int role){if(!IsSpecial(role))throw new ArgumentOutOfRangeException("role");return role<11?0:role==11?1:role-10;}
        public static int SpecialCels(int role){return role>=12?12:role==11?8:4;}
        public static int SpecialRow(int role,int level){if(!IsSpecial(role))throw new ArgumentOutOfRangeException("role");return role>=12?(level>=FavouriteMasteries[role-12]?3:0):role==11?(level>=95?2:0):(role-8)*2+(level>=new[]{80,90,100}[role-8]?1:0);}
        public static HeroGear Gear(int role,int level){
            level=Math.Max(1,Math.Min(100,level));if(!CanEquip(role,level))role=0;
            switch(role){
                case 0: return level<10?new HeroGear(1,0,"Novice Courier",1):level<25?new HeroGear(1,1,"Courier Trainee",10):level<45?new HeroGear(1,2,"Iron Courier",25):new HeroGear(0,0,"Courier Knight",45,true);
                case 1: return level<45?new HeroGear(1,3,"Apprentice Mage",8):new HeroGear(0,1,"Rune Mage",45,true);
                case 2: return level<45?new HeroGear(1,4,"Forest Scout",18):new HeroGear(0,2,"Forest Ranger",45,true);
                case 3: return level<60?new HeroGear(1,5,"Shadow Initiate",35):new HeroGear(0,3,"Shadow Reaper",60,true);
                case 4: return level<75?new HeroGear(1,6,"Warden Recruit",55):new HeroGear(0,4,"Demon Warden",75,true);
                case 5: return level<85?new HeroGear(1,2,"Seraph Aspirant",75):new HeroGear(0,5,"Heavenly Seraph",85,true);
                case 6:return level<100?new HeroGear(0,6,"Astral Herald",95):new HeroGear(1,7,"Astral Sovereign",100,true);
                case 7:return level<55?new HeroGear(2,0,"Ronin Trainee",12):new HeroGear(2,1,"Silent Ronin",55,true);
                case 8:return level<80?new HeroGear(2,2,"Silver Academy",40):new HeroGear(2,3,"Limitless Teacher",80,true);
                case 9:return level<90?new HeroGear(2,4,"Everyday Training",65):new HeroGear(2,5,"One-Punch Hero",90,true);
                case 10:return level<100?new HeroGear(2,6,"Leaf Ninja",85):new HeroGear(2,7,"Sage of Dawn",100,true);
                case 11:return level<95?new HeroGear(3,0,"Card Duelist",50):new HeroGear(3,1,"Pharaoh Duelist",95,true);
                default:int index=role-12;bool master=level>=FavouriteMasteries[index];return new HeroGear(4+index,master?1:0,master?FavouriteMasterTitles[index]:FavouriteTitles[index],master?FavouriteMasteries[index]:RoleLevels[role],master);
            }
        }
    }
    public sealed class HeroProfile {
        public string Name=Environment.MachineName;
        public int Hair=0,Skin=0,HairColour=0,Shirt=0,Eyes=0,Look=0,Role=0,Element=0;
        public bool Animations=true;
        public bool MotionOverride=false;
        public bool PixelPreview=false;
        public bool AutoSkills=true;
        public HeroProfile Copy(){return (HeroProfile)MemberwiseClone();}
        public void KeepAppearance(HeroProfile personal){Hair=personal.Hair;Skin=personal.Skin;HairColour=personal.HairColour;Shirt=personal.Shirt;Eyes=personal.Eyes;Look=personal.Look;Element=personal.Element;}
        public static string CleanName(string raw){string value=new string((raw??"").Where(c=>!char.IsControl(c)&&!(c>='\u202a'&&c<='\u202e')&&!(c>='\u2066'&&c<='\u2069')).ToArray()).Trim();return value.Length==0?"Happy Sprout":value.Substring(0,Math.Min(24,value.Length));}
        public void Clean(int level){Name=CleanName(Name);Hair=Bound(Hair,5);Skin=Bound(Skin,4);HairColour=Bound(HairColour,5);Shirt=Bound(Shirt,4);Eyes=Bound(Eyes,3);Look=Bound(Look,1);Element=Bound(Element,5);if(!Adventure.CanEquip(Role,level))Role=0;}
        static int Bound(int value,int max){return Math.Max(0,Math.Min(max,value));}
    }
    // Fixed-size, checksummed receipts: digest, size and award only; no paths or peer names.
    // A flushed receipt is the source of both totals and deduplication after restart.
    public sealed class JourneyStore:IDisposable {
        readonly object gate=new object();
        readonly HashSet<string> credited=new HashSet<string>();
        readonly string directory;
        FileStream ledger;
        public long Xp{get;private set;}
        public long Files{get;private set;}
        public long Bytes{get;private set;}
        public int Level{get{return Adventure.Level(Xp);}}
        public string Error{get;private set;}
        public HeroProfile Profile{get;private set;}
        static readonly byte[] Header=Encoding.ASCII.GetBytes("SMXP001\n");
        const int RecordBytes=76;
        public JourneyStore(string folder){
            directory=folder;Directory.CreateDirectory(directory);
            try{OpenLedger();}catch(Exception error){Error="Progress is unavailable: "+error.Message;if(ledger!=null){ledger.Dispose();ledger=null;}}
            Profile=LoadProfile();
        }
        void OpenLedger(){
            string path=Path.Combine(directory,"adventurer.receipts");ledger=new FileStream(path,FileMode.OpenOrCreate,FileAccess.ReadWrite,FileShare.Read);
            if(ledger.Length==0){ledger.Write(Header,0,Header.Length);ledger.Flush(true);}
            byte[] header=new byte[Header.Length];ledger.Position=0;if(ledger.Read(header,0,header.Length)!=header.Length||!header.SequenceEqual(Header))throw new IOException("The progress ledger header is damaged. The original file is kept.");
            long good=Header.Length;
            while(ledger.Length-ledger.Position>=RecordBytes){
                byte[] record=new byte[RecordBytes];if(ledger.Read(record,0,record.Length)!=record.Length)throw new IOException("Could not read progress.");
                byte[] payload=record.Take(44).ToArray();byte[] digest;using(var hash=SHA256.Create())digest=hash.ComputeHash(payload);
                long size=BitConverter.ToInt64(payload,32);int points=BitConverter.ToInt32(payload,40);
                if(!digest.SequenceEqual(record.Skip(44))||size<=0||size>1099511627776L||points!=Adventure.FileXp(size))throw new IOException("A progress receipt is damaged. The original file is kept.");
                string key=Wire.Hex(payload.Take(32).ToArray())+":"+size;
                if(credited.Add(key))Credit(size,points);
                good=ledger.Position;
            }
            if(ledger.Position<ledger.Length){
                byte[] tail=new byte[ledger.Length-good];ledger.Read(tail,0,tail.Length);
                File.WriteAllBytes(path+".partial-"+Guid.NewGuid().ToString("N"),tail);
                ledger.SetLength(good);ledger.Flush(true);
            }
            ledger.Position=ledger.Length;
        }
        void Credit(long size,int points){Xp=Math.Min(Adventure.Threshold(100),Xp+points);Files++;Bytes+=Math.Min(size,long.MaxValue-Bytes);}
        public int AwardVerified(FileMeta file){
            if(file==null||file.size<=0||file.size>1099511627776L||!System.Text.RegularExpressions.Regex.IsMatch(file.sha256??"","^[a-f0-9]{64}$"))return 0;
            lock(gate){
                if(ledger==null)return 0;string key=file.sha256+":"+file.size;if(credited.Contains(key))return 0;
                try{
                    int points=Adventure.FileXp(file.size);byte[] payload=new byte[44];for(int i=0;i<32;i++)payload[i]=Convert.ToByte(file.sha256.Substring(i*2,2),16);
                    Array.Copy(BitConverter.GetBytes(file.size),0,payload,32,8);Array.Copy(BitConverter.GetBytes(points),0,payload,40,4);
                    byte[] checksum;using(var hash=SHA256.Create())checksum=hash.ComputeHash(payload);
                    ledger.Write(payload,0,payload.Length);ledger.Write(checksum,0,checksum.Length);ledger.Flush(true);
                    credited.Add(key);long before=Xp;Credit(file.size,points);return (int)(Xp-before);
                }catch(Exception error){Error="Could not save progress: "+error.Message;ledger.Dispose();ledger=null;return 0;}
            }
        }
        HeroProfile LoadProfile(){
            var serializer=new JavaScriptSerializer();foreach(string suffix in new[]{"",".bak"})try{string file=Path.Combine(directory,"adventurer.json"+suffix);if(new FileInfo(file).Length>8192)continue;var profile=serializer.Deserialize<HeroProfile>(File.ReadAllText(file));if(profile!=null){profile.Clean(Level);return profile;}}catch{}
            var first=new HeroProfile();first.Clean(Level);return first;
        }
        public bool SaveProfile(HeroProfile profile){lock(gate){
            var clean=profile.Copy();clean.Clean(Level);string file=Path.Combine(directory,"adventurer.json"),temporary=file+".new";
            if(Adventure.IsFixedDesign(clean.Role))clean.KeepAppearance(Profile);
            try{
                byte[] bytes=new UTF8Encoding(false).GetBytes(new JavaScriptSerializer().Serialize(clean));
                using(var stream=new FileStream(temporary,FileMode.Create,FileAccess.Write,FileShare.None)){stream.Write(bytes,0,bytes.Length);stream.Flush(true);}
                if(File.Exists(file))File.Replace(temporary,file,file+".bak");else File.Move(temporary,file);
                Profile=clean;return true;
            }catch(Exception error){Error="Could not save appearance: "+error.Message;return false;}
        }}
        public void Dispose(){lock(gate){if(ledger!=null){ledger.Dispose();ledger=null;}}}
    }
}
