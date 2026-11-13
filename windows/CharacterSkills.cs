using System;
using System.Drawing;

namespace ShareMeRowell {
    public static class CharacterSkills {
        public static bool Has(int role){return role==13||role==19||role==23;}
        public static string Name(int role){return role==13?"Zoro":role==19?"Sukuna":"Madara";}
        public static string Title(int role){return role==13?"Three-Sword Tempest":role==19?"Malevolent Shrine":"Crimson Awakening";}
        public static string Prefix(int role){if(!Has(role))throw new ArgumentOutOfRangeException("role");return role==13?"zoro":role==19?"sukuna":"madara";}
        public static int Duration(int role){return role==13?ZoroSequence.Duration:role==19?SukunaSequence.Duration:MadaraSequence.Duration;}
        public static Color Energy(int role){return role==13?Color.FromArgb(115,244,160):role==19?Color.FromArgb(234,78,91):Color.FromArgb(178,126,250);}
        public static int UnlockLevel(int role){return role==13?Adventure.MasteryLevel(role):Adventure.RoleLevels[role];}
        public static bool HasForOutfit(int role,int outfitLevel){return Has(role)&&(role!=13||outfitLevel>=Adventure.MasteryLevel(role));}
        public static bool CanAutoUse(HeroProfile profile,int role,int level){return profile.AutoSkills&&Has(role)&&Adventure.CanEquip(role,level)&&level>=UnlockLevel(role);}
        public static MadaraMoment At(int role,long time){return role==13?ZoroSequence.At(time):role==19?SukunaSequence.At(time):MadaraSequence.At(time);}
    }
    public static class SukunaSequence {
        public const int Duration=4800;
        static float Progress(long time,int start,int length){return Math.Max(0,Math.Min(1,(time-start)/(float)length));}
        public static MadaraMoment At(long time){
            time=Math.Max(0,time);var m=new MadaraMoment{Body=0,Guardian=-1,Eye=-1,ActorScale=.54f,Caption="Malevolent Shrine"};
            if(time<180)return m;
            if(time<1200){m.Body=Math.Min(11,(int)(Progress(time,180,1020)*12));m.Caption="Domain hand sign";return m;}
            if(time<1850){float p=Progress(time,1200,650);m.Body=12+Math.Min(3,(int)(p*4));m.Eye=Math.Min(15,(int)(p*16));m.Closeup=Math.Min(Progress(time,1200,100),1-Progress(time,1730,120));m.Caption="Domain Expansion";return m;}
            if(time<3000){float p=Progress(time,1850,1150);m.Body=16+Math.Min(3,(int)(p*4));m.Guardian=Math.Min(15,(int)(p*16));m.Aura=p;m.ActorScale=.54f-.14f*p;m.Caption="Malevolent Shrine · Manifest";return m;}
            if(time<4200){m.Body=19;m.Guardian=16+Math.Min(7,(int)(Progress(time,3000,1200)*8));m.Aura=1;m.ActorScale=.4f;m.Caption="Malevolent Shrine · Unleashed";return m;}
            if(time<Duration){float p=Progress(time,4200,600);m.Body=20+Math.Min(3,(int)(p*4));m.Guardian=23-Math.Min(23,(int)(p*24));m.Aura=1-p;m.ActorScale=.4f+.14f*p;m.Caption="Domain dissolves";return m;}
            m.Body=23;m.Complete=true;return m;
        }
    }
}
