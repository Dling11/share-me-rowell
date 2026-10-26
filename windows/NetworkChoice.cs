using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.NetworkInformation;
using System.Net.Sockets;

namespace ShareMeRowell {
    public sealed class NetworkChoice {
        public readonly string Address,Name;
        public readonly bool Virtual,Automatic;
        public NetworkChoice(string address,string name,bool virtualAdapter=false,bool automatic=false){Address=address;Name=name;Virtual=virtualAdapter;Automatic=automatic;}
        public override string ToString(){return Automatic?"Automatic (recommended)":Name+" · "+Address+(Virtual?" · virtual adapter":"");}
        public static bool IsVirtualAdapter(string name,string description){string text=(name+" "+description).ToLowerInvariant();return text.Contains("wsl")||name.StartsWith("vEthernet",StringComparison.OrdinalIgnoreCase)||text.Contains("docker")||text.Contains("vmnet")||text.Contains("virtualbox host")||text.Contains("vmware network adapter");}
        public static List<NetworkChoice> Read(){
            var result=new List<NetworkChoice>();
            foreach(var adapter in NetworkInterface.GetAllNetworkInterfaces().Where(n=>n.OperationalStatus==OperationalStatus.Up&&n.NetworkInterfaceType!=NetworkInterfaceType.Loopback)){
                var properties=adapter.GetIPProperties();bool virtualAdapter=IsVirtualAdapter(adapter.Name,adapter.Description);
                foreach(var ip in properties.UnicastAddresses)if(ip.Address.AddressFamily==AddressFamily.InterNetwork&&Wire.Private(ip.Address)&&!IPAddress.IsLoopback(ip.Address))result.Add(new NetworkChoice(ip.Address.ToString(),adapter.Name,virtualAdapter));
            }
            return result.OrderBy(n=>n.Virtual?1:0).ThenBy(n=>n.Name,StringComparer.OrdinalIgnoreCase).ToList();
        }
        public static List<string> LocalAddresses(IEnumerable<NetworkChoice> candidates){var values=candidates.Where(n=>!n.Virtual).Select(n=>n.Address).Distinct().Take(8).ToList();if(values.Count==0)values.Add("127.0.0.1");return values;}
    }
}
