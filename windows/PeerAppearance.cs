using System;
using System.Collections.Generic;
using System.Globalization;

namespace ShareMeRowell {
    // Optional cosmetic metadata only. Invalid/old headers never prevent a transfer.
    public sealed class PeerAppearance {
        public readonly string Style;public readonly int Level;
        PeerAppearance(string style,int level){Style=style;Level=level;}
        public static string Encode(HeroProfile p){return string.Join(".",new[]{p.Hair,p.Skin,p.HairColour,p.Shirt,p.Eyes,p.Look,p.Role,p.Element});}
        public HeroProfile Profile(){var n=Array.ConvertAll(Style.Split('.'),s=>int.Parse(s,CultureInfo.InvariantCulture));return new HeroProfile{Hair=n[0],Skin=n[1],HairColour=n[2],Shirt=n[3],Eyes=n[4],Look=n[5],Role=n[6],Element=n[7]};}
        public static PeerAppearance Parse(Dictionary<string,object> header){
            object raw;if(!header.TryGetValue("appearance",out raw)||!(raw is string)||((string)raw).Length>80)return null;
            var parts=((string)raw).Split('.');if(parts.Length!=8)return null;var limits=new[]{5,4,5,4,3,1,Adventure.Roles.Length-1,Adventure.Elements.Length-1};
            for(int i=0;i<8;i++){int value;if(!int.TryParse(parts[i],NumberStyles.None,CultureInfo.InvariantCulture,out value)||value<0||value>limits[i])return null;}
            int level=1;object claimed;if(header.TryGetValue("level",out claimed)){int value;if(int.TryParse(Convert.ToString(claimed,CultureInfo.InvariantCulture),out value)&&value>=1&&value<=100)level=value;}
            return new PeerAppearance((string)raw,level);
        }
        public static Dictionary<string,object> Attach(Dictionary<string,object> message,HeroProfile profile,int level){message["appearance"]=Encode(profile);message["level"]=Math.Max(1,Math.Min(100,level));return message;}
    }
}
