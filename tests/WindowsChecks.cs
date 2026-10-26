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
        File.WriteAllText(Path.Combine(folder,"result.json"),Wire.Encode(new Dictionary<string,object>{{"passed",passed.Count},{"checks",passed}}));Console.WriteLine(string.Join(Environment.NewLine,passed));return 0;
    }catch(Exception error){Console.Error.WriteLine(error);return 1;}}
}
