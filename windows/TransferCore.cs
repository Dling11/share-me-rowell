using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.NetworkInformation;
using System.Net.Security;
using System.Net.Sockets;
using System.Security.Authentication;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading;
using System.Web.Script.Serialization;

namespace ShareMeRowell {
    public sealed class FileMeta {
        public string id, name, sha256;
        public long size;
        public Dictionary<string, object> Json() { return new Dictionary<string, object> { {"id",id},{"name",name},{"size",size},{"sha256",sha256} }; }
        public static FileMeta Parse(Dictionary<string, object> o) {
            var m = new FileMeta { id = Convert.ToString(o["id"]), name = Wire.SafeName(Convert.ToString(o["name"])), size = Convert.ToInt64(o["size"], CultureInfo.InvariantCulture), sha256 = Convert.ToString(o["sha256"]) };
            if (!System.Text.RegularExpressions.Regex.IsMatch(m.id, "^[a-zA-Z0-9_-]{1,64}$") || m.size < 0 || m.size > 1099511627776L || !System.Text.RegularExpressions.Regex.IsMatch(m.sha256, "^[a-f0-9]{64}$")) throw new IOException("Invalid file details.");
            return m;
        }
    }
    public sealed class Source {
        public string Path;
        public DateTime Modified;
        public FileMeta Meta;
        public static Source Prepare(string path) {
            var info = new FileInfo(path);
            if (!info.Exists || (info.Attributes & FileAttributes.ReparsePoint) != 0) throw new IOException("Choose regular files, rather than folders or links.");
            var meta = new FileMeta { id = Guid.NewGuid().ToString("N"), name = Wire.SafeName(info.Name), size = info.Length };
            if (meta.size > 1099511627776L) throw new IOException("This file is too large.");
            using (var input = new FileStream(path, FileMode.Open, FileAccess.Read, FileShare.Read)) meta.sha256 = Wire.Hash(input);
            return new Source { Path = info.FullName, Modified = info.LastWriteTimeUtc, Meta = meta };
        }
        public Stream Open(long offset) {
            var info = new FileInfo(Path);
            if (!info.Exists || info.Length != Meta.size || info.LastWriteTimeUtc != Modified) throw new IOException("A selected file changed. Choose it again.");
            var stream = new FileStream(Path, FileMode.Open, FileAccess.Read, FileShare.Read);
            stream.Position = offset; return stream;
        }
    }
    public static class Wire {
        public static readonly UTF8Encoding Utf8 = new UTF8Encoding(false, true);
        public static string Encode(object o) { return new JavaScriptSerializer { MaxJsonLength = 65536 }.Serialize(o); }
        public static Dictionary<string, object> Decode(string text) { return new JavaScriptSerializer { MaxJsonLength = 65536, RecursionLimit = 12 }.Deserialize<Dictionary<string, object>>(text); }
        public static Dictionary<string, object> Read(Stream stream) {
            using (var bytes = new MemoryStream()) {
                while (bytes.Length <= 65536) {
                    int next = stream.ReadByte();
                    if (next < 0) throw new EndOfStreamException("The other device disconnected. Scan again to resume.");
                    if (next == 10) return Decode(Utf8.GetString(bytes.ToArray()));
                    bytes.WriteByte((byte)next);
                }
            }
            throw new IOException("Connection header is too large.");
        }
        public static void Write(Stream stream, object o) { byte[] data = Utf8.GetBytes(Encode(o)); if (data.Length > 65536) throw new IOException("Too many file details."); stream.Write(data,0,data.Length); stream.WriteByte(10); stream.Flush(); }
        public static bool Ok(Dictionary<string, object> o) { return o.ContainsKey("ok") && Convert.ToBoolean(o["ok"]); }
        public static List<FileMeta> Files(Dictionary<string, object> o) {
            var list = ((System.Collections.IEnumerable)o["files"]).Cast<object>().Select(x => FileMeta.Parse((Dictionary<string, object>)x)).ToList();
            if (list.Count < 1 || list.Count > 16 || list.Select(x=>x.id).Distinct().Count() != list.Count) throw new IOException("Choose 1 to 16 files.");
            return list;
        }
        public static string Hex(byte[] bytes) { return BitConverter.ToString(bytes).Replace("-", "").ToLowerInvariant(); }
        public static string Hash(Stream input,CancellationToken cancellation=default(CancellationToken)) {using(var hash=SHA256.Create()){var buffer=new byte[262144];int count;while(true){cancellation.ThrowIfCancellationRequested();count=input.Read(buffer,0,buffer.Length);if(count==0)break;hash.TransformBlock(buffer,0,count,buffer,0);}hash.TransformFinalBlock(new byte[0],0,0);return Hex(hash.Hash);}}
        public static string Secret() { byte[] bytes = new byte[32]; using (var rng = RandomNumberGenerator.Create()) rng.GetBytes(bytes); return Convert.ToBase64String(bytes).TrimEnd('=').Replace('+','-').Replace('/','_'); }
        public static bool Equal(string a, string b) { if (a == null || b == null || a.Length != b.Length) return false; int difference=0; for(int i=0;i<a.Length;i++) difference |= a[i]^b[i]; return difference==0; }
        public static string SafeName(string raw) {
            var output = new StringBuilder();
            foreach(char c in (raw ?? "Received file").Normalize(NormalizationForm.FormC)) output.Append(c < 32 || "/\\:*?\"<>|".Contains(c) || (c >= '\u202a' && c <= '\u202e') || (c >= '\u2066' && c <= '\u2069') ? '_' : c);
            string name = output.ToString().Trim().TrimEnd('.'); if(name.Length>180) name=name.Substring(0,180); if(name.Length==0) name="Received file";
            if (System.Text.RegularExpressions.Regex.IsMatch(name.Split('.')[0], "^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$", System.Text.RegularExpressions.RegexOptions.IgnoreCase)) name="_"+name;
            return name;
        }
        public static bool Private(IPAddress ip) { byte[] b=ip.GetAddressBytes(); return b.Length==4 && (b[0]==10 || b[0]==127 || (b[0]==192 && b[1]==168) || (b[0]==172 && b[1]>=16 && b[1]<=31) || (b[0]==169 && b[1]==254)); }
        public static List<string> Addresses() {
            var list = new List<string>();
            foreach(var adapter in NetworkInterface.GetAllNetworkInterfaces().Where(n=>n.OperationalStatus==OperationalStatus.Up && n.NetworkInterfaceType!=NetworkInterfaceType.Loopback).OrderBy(n=>n.Description.Contains("Hyper-V") || n.Name.Contains("WSL") ? 1 : 0)) {
                foreach(var item in adapter.GetIPProperties().UnicastAddresses) if(item.Address.AddressFamily==AddressFamily.InterNetwork && Private(item.Address) && !IPAddress.IsLoopback(item.Address)) list.Add(item.Address.ToString());
            }
            if(list.Count==0) list.Add("127.0.0.1"); return list.Distinct().Take(8).ToList();
        }
        public static void Copy(Stream input, Stream output, long length, Action<long> progress) {
            byte[] buffer=new byte[262144]; long done=0;
            while(done<length) { int read=input.Read(buffer,0,(int)Math.Min(buffer.Length,length-done)); if(read<=0) throw new EndOfStreamException("Transfer interrupted. Scan again to resume."); output.Write(buffer,0,read); done+=read; if(progress!=null) progress(done); }
            output.Flush();
        }
    }
    public enum ConnectionPhase {Idle,Approval,Waiting,Sending,Receiving,Verifying,Finished,Declined,Disconnected,Failed}
    public sealed class ConnectionSnapshot {
        public readonly string Peer;
        public readonly ConnectionPhase Phase;
        public ConnectionSnapshot(string peer,ConnectionPhase phase){Peer=peer;Phase=phase;}
        public bool Active {get{return Phase==ConnectionPhase.Approval||Phase==ConnectionPhase.Waiting||Phase==ConnectionPhase.Sending||Phase==ConnectionPhase.Receiving||Phase==ConnectionPhase.Verifying;}}
        public bool Terminal {get{return Phase==ConnectionPhase.Finished||Phase==ConnectionPhase.Declined||Phase==ConnectionPhase.Disconnected||Phase==ConnectionPhase.Failed;}}
        public string Label {get{switch(Phase){case ConnectionPhase.Approval:return "Waiting for your approval";case ConnectionPhase.Waiting:return "Waiting for the other device";case ConnectionPhase.Sending:return "Connected · sending";case ConnectionPhase.Receiving:return "Connected · receiving";case ConnectionPhase.Verifying:return "Connected · verifying";case ConnectionPhase.Finished:return "Finished · disconnected";case ConnectionPhase.Declined:return "Declined · disconnected";case ConnectionPhase.Disconnected:return "Disconnected";case ConnectionPhase.Failed:return "Connection ended";default:return "No device connected";}}}
        public Dictionary<string,object> Json(){return new Dictionary<string,object>{{"peer",Peer},{"phase",Phase.ToString()},{"active",Active},{"label",Label}};}
    }
    public sealed class TransferServer : IDisposable {
        public Func<string,List<FileMeta>,CancellationToken,bool> Approve;
        public Action<string,long,long> Progress;
        public Action<string> Status;
        public Action<string,string,long,string> Completed;
        public string Name = Environment.MachineName;
        public string Mode = "receive";
        public List<Source> Sources = new List<Source>();
        public readonly string Destination;
        public readonly X509Certificate2 Certificate;
        public int Port { get; private set; }
        public string Code { get; private set; }
        public DateTime Expires { get; private set; }
        public bool Busy { get { return busy!=0; } }
        private int busy;
        private volatile bool stopped;
        private TcpListener listener;
        private readonly object gate = new object();
        private readonly HashSet<TcpClient> clients=new HashSet<TcpClient>();
        private ConnectionSnapshot connection=new ConnectionSnapshot("",ConnectionPhase.Idle);
        private CancellationTokenSource cancellation;
        public ConnectionSnapshot Connection {get{lock(gate)return connection;}}
        private void Phase(ConnectionPhase phase){lock(gate){if(!connection.Terminal)connection=new ConnectionSnapshot(connection.Peer,phase);}}
        public TransferServer(string destination, string stateDirectory) {
            Destination=System.IO.Path.GetFullPath(destination); Directory.CreateDirectory(Destination); Directory.CreateDirectory(stateDirectory);
            string certPath=System.IO.Path.Combine(stateDirectory,"identity.dpapi");
            if(File.Exists(certPath)) Certificate=new X509Certificate2(ProtectedData.Unprotect(File.ReadAllBytes(certPath),null,DataProtectionScope.CurrentUser),"rowell-local",X509KeyStorageFlags.UserKeySet|X509KeyStorageFlags.Exportable);
            else {
                using(var key=RSA.Create()) {
                    key.KeySize=2048;
                    var request=new CertificateRequest("CN=Share-Me-Rowell",key,HashAlgorithmName.SHA256,RSASignaturePadding.Pkcs1);
                    using(var generated=request.CreateSelfSigned(DateTimeOffset.UtcNow.AddDays(-1),DateTimeOffset.UtcNow.AddYears(5))) {
                        byte[] pfx=generated.Export(X509ContentType.Pfx,"rowell-local");
                        Certificate=new X509Certificate2(pfx,"rowell-local",X509KeyStorageFlags.UserKeySet|X509KeyStorageFlags.Exportable);
                        File.WriteAllBytes(certPath,ProtectedData.Protect(pfx,null,DataProtectionScope.CurrentUser));
                    }
                }
            }
            Refresh();
        }
        public string Fingerprint { get { using(var hash=SHA256.Create()) return Wire.Hex(hash.ComputeHash(Certificate.RawData)); } }
        public void Refresh() { Code=Wire.Secret(); Expires=DateTime.UtcNow.AddMinutes(10); }
        public void CancelActive(){lock(gate){if(connection.Active)connection=new ConnectionSnapshot(connection.Peer,ConnectionPhase.Disconnected);if(cancellation!=null)cancellation.Cancel();foreach(var client in clients.ToArray())client.Close();}if(Status!=null)Status("Disconnected. Scan again to resume an unfinished file.");}
        public string Pairing(string address) {
            var json=new Dictionary<string,object>{{"v",1},{"hosts",new[]{address}},{"port",Port},{"fp",Fingerprint},{"code",Code},{"mode",Mode},{"name",Name}};
            return "shareme-rowell://pair/"+Convert.ToBase64String(Wire.Utf8.GetBytes(Wire.Encode(json))).TrimEnd('=').Replace('+','-').Replace('/','_');
        }
        public void Start(IPAddress bind, int port) {
            listener=new TcpListener(bind,port); listener.Start(4); Port=((IPEndPoint)listener.LocalEndpoint).Port;
            new Thread(()=>{while(!stopped) { try {var client=listener.AcceptTcpClient(); lock(gate) {if(stopped||clients.Count>=3) {client.Close();continue;} clients.Add(client);} ThreadPool.QueueUserWorkItem(_=>Handle(client)); } catch { if(!stopped && Status!=null) Status("Connection listener stopped."); } }} ){IsBackground=true}.Start();
        }
        private void Reject(Stream stream,string message) { Wire.Write(stream,new Dictionary<string,object>{{"ok",false},{"error",message}}); }
        private void Handle(TcpClient client) {
            bool claimed=false;CancellationTokenSource sessionCancellation=null;
            try {
                if(!Wire.Private(((IPEndPoint)client.Client.RemoteEndPoint).Address)) return;
                client.ReceiveTimeout=15000; client.SendTimeout=30000; client.NoDelay=true;
                using(var ssl=new SslStream(client.GetStream(),false)) {
                    ssl.AuthenticateAsServer(Certificate,false,SslProtocols.Tls12,false);
                    var request=Wire.Read(ssl);
                    if(!request.ContainsKey("v") || Convert.ToInt32(request["v"])!=1 || DateTime.UtcNow>Expires || !Wire.Equal(Convert.ToString(request.ContainsKey("code")?request["code"]:null),Code)) {Reject(ssl,"Pairing expired. Scan the current QR code.");return;}
                    string peer=Wire.SafeName(Convert.ToString(request.ContainsKey("name")?request["name"]:"Nearby device"));
                    if(peer.Length>64)peer=peer.Substring(0,64);
                    lock(gate){if(busy==0){busy=1;claimed=true;sessionCancellation=new CancellationTokenSource();cancellation=sessionCancellation;connection=new ConnectionSnapshot(peer,ConnectionPhase.Approval);}}
                    if(!claimed){Reject(ssl,"This device is busy with another transfer.");return;}
                    string op=Convert.ToString(request["op"]);
                    List<Source> sources; string mode;
                    lock(gate) {sources=new List<Source>(Sources);mode=Mode;}
                    List<FileMeta> files;
                    if(op=="push" && mode=="receive") files=Wire.Files(request);
                    else if(op=="pull" && mode=="send" && sources.Count>0) files=sources.Select(s=>s.Meta).ToList();
                    else {Phase(ConnectionPhase.Failed);Reject(ssl,"Choose Send on one device and Receive on the other.");return;}
                    client.ReceiveTimeout=120000;
                    sessionCancellation.Token.ThrowIfCancellationRequested();
                    bool allowed=Approve!=null&&Approve(peer,files,sessionCancellation.Token);
                    sessionCancellation.Token.ThrowIfCancellationRequested();
                    if(!allowed){Phase(ConnectionPhase.Declined);if(Status!=null)Status("Transfer declined.");Reject(ssl,"Transfer declined on the other device.");return;}
                    client.ReceiveTimeout=600000;
                    if(op=="push") Receive(ssl,files,sessionCancellation.Token);
                    else {
                        Wire.Write(ssl,new Dictionary<string,object>{{"ok",true},{"sender",Name},{"files",files.Select(f=>f.Json()).ToArray()}});
                        Phase(ConnectionPhase.Waiting);var accepted=Wire.Read(ssl); if(!Wire.Ok(accepted)){Phase(ConnectionPhase.Declined);if(Status!=null)Status("Transfer declined on the other device.");return;}
                        var offsets=((System.Collections.IEnumerable)accepted["offsets"]).Cast<object>().Select(Convert.ToInt64).ToArray();
                        if(offsets.Length!=files.Count) throw new IOException("Invalid resume response.");
                        for(int i=0;i<sources.Count;i++) {
                            Phase(ConnectionPhase.Sending);
                            int index=i; long offset=offsets[i]; if(offset<0 || offset>files[i].size) throw new IOException("Invalid resume position.");
                            using(var input=sources[i].Open(offset)) Wire.Copy(input,ssl,files[i].size-offset,done=>{if(Progress!=null) Progress(files[index].name,offset+done,files[index].size);});
                            Phase(ConnectionPhase.Verifying);var ack=Wire.Read(ssl); if(!Wire.Ok(ack)) throw new IOException(Convert.ToString(ack.ContainsKey("error")?ack["error"]:"Verification failed."));
                            if(Completed!=null) Completed("Sent",files[i].name,files[i].size,sources[i].Path);
                        }
                    }
                    Phase(ConnectionPhase.Finished);if(Status!=null) Status("Transfer complete. Files verified.");
                }
            } catch(Exception error) { if(claimed&&!stopped&&!Connection.Terminal){Phase(ConnectionPhase.Failed);if(Status!=null&&!(sessionCancellation!=null&&sessionCancellation.IsCancellationRequested))Status(error is IOException ? error.Message : "Connection interrupted. Scan again to resume.");} }
            finally {client.Close();lock(gate){clients.Remove(client);if(claimed){busy=0;cancellation=null;}}if(sessionCancellation!=null)sessionCancellation.Dispose();}
        }
        private void Receive(Stream stream,List<FileMeta> files,CancellationToken cancellation) {
            string parts=System.IO.Path.Combine(Destination,".shareme-parts"); Directory.CreateDirectory(parts);
            var seen=new HashSet<string>();
            long[] offsets=files.Select(f=>{string key=f.sha256+"-"+f.size;if(!seen.Add(key))return 0L;string path=System.IO.Path.Combine(parts,key+".part");long length=File.Exists(path)?new FileInfo(path).Length:0;if(length>f.size){File.Delete(path);length=0;}return length;}).ToArray();
            long remaining=files.Select((f,i)=>f.size-offsets[i]).Sum();
            if(new DriveInfo(System.IO.Path.GetPathRoot(Destination)).AvailableFreeSpace<remaining+16777216) throw new IOException("Not enough free space in the receive folder.");
            Wire.Write(stream,new Dictionary<string,object>{{"ok",true},{"offsets",offsets}});
            for(int i=0;i<files.Count;i++) {
                Phase(ConnectionPhase.Receiving);
                var file=files[i]; long offset=offsets[i]; string partial=System.IO.Path.Combine(parts,file.sha256+"-"+file.size+".part");
                using(var output=new FileStream(partial,FileMode.OpenOrCreate,FileAccess.Write,FileShare.None)) {output.SetLength(offset);output.Position=offset; Wire.Copy(stream,output,file.size-offset,done=>{if(Progress!=null) Progress(file.name,offset+done,file.size);}); output.Flush(true);}
                Phase(ConnectionPhase.Verifying);if(Status!=null) Status("Verifying "+file.name+"…");
                string actual; using(var input=File.OpenRead(partial)) actual=Wire.Hash(input,cancellation);
                if(!Wire.Equal(actual,file.sha256)) {File.Delete(partial); Reject(stream,"File verification failed. Choose the source file again.");throw new IOException("Verification failed; the incomplete file was removed.");}
                string destination=System.IO.Path.Combine(Destination,file.name); int suffix=2;
                while(File.Exists(destination)||Directory.Exists(destination)) {destination=System.IO.Path.Combine(Destination,System.IO.Path.GetFileNameWithoutExtension(file.name)+" ("+(suffix++)+")"+System.IO.Path.GetExtension(file.name));}
                lock(gate){cancellation.ThrowIfCancellationRequested();File.Move(partial,destination);}
                if(Completed!=null) Completed("Received",System.IO.Path.GetFileName(destination),file.size,destination);
                Wire.Write(stream,new Dictionary<string,object>{{"ok",true},{"index",i}});
            }
        }
        public void Dispose() {stopped=true;if(listener!=null)listener.Stop();CancelActive();lock(gate)clients.Clear();Certificate.Dispose();}
    }

    public sealed class InstallerServer : IDisposable {
        private readonly TcpListener listener;
        private readonly object gate=new object();
        private readonly HashSet<TcpClient> clients=new HashSet<TcpClient>();
        private readonly string apk;
        public readonly string Token=Wire.Secret();
        private volatile bool stopped;
        public int Port {get;private set;}
        private readonly DateTime expires=DateTime.UtcNow.AddMinutes(30);
        public InstallerServer(string apkPath,IPAddress bind,int port) {
            apk=apkPath;listener=new TcpListener(bind,port);listener.Start(3);Port=((IPEndPoint)listener.LocalEndpoint).Port;
            new Thread(()=>{while(!stopped){try{var client=listener.AcceptTcpClient();lock(gate){if(stopped||clients.Count>=3){client.Close();continue;}clients.Add(client);}ThreadPool.QueueUserWorkItem(_=>Handle(client));}catch{}}}){IsBackground=true}.Start();
        }
        public string Url(string host){return "http://"+host+":"+Port+"/"+Token;}
        private void Handle(TcpClient client){try{
            using(client){if(!Wire.Private(((IPEndPoint)client.Client.RemoteEndPoint).Address)||DateTime.UtcNow>expires)return;client.ReceiveTimeout=5000;client.SendTimeout=30000;using(var stream=client.GetStream()){
                var line=new StringBuilder();while(line.Length<2048){int c=stream.ReadByte();if(c<0)return;if(c==10)break;if(c!=13)line.Append((char)c);}
                int headerLength=0,headerLine=0;bool headersComplete=false;while(headerLength++<16384){int c=stream.ReadByte();if(c<0)return;if(c==10){if(headerLine==0){headersComplete=true;break;}headerLine=0;}else if(c!=13)headerLine++;}if(!headersComplete)return;
                string request=line.ToString();bool download=request=="GET /"+Token+"/app.apk HTTP/1.1" || request=="GET /"+Token+"/app.apk HTTP/1.0";
                bool page=request=="GET /"+Token+" HTTP/1.1" || request=="GET /"+Token+" HTTP/1.0";
                if(!download&&!page){byte[] no=Encoding.ASCII.GetBytes("HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n");stream.Write(no,0,no.Length);return;}
                if(!File.Exists(apk))return;
                if(download){var info=new FileInfo(apk);byte[] header=Encoding.ASCII.GetBytes("HTTP/1.1 200 OK\r\nContent-Type: application/vnd.android.package-archive\r\nContent-Disposition: attachment; filename=Share-Me-Rowell.apk\r\nContent-Length: "+info.Length+"\r\nX-Content-Type-Options: nosniff\r\nConnection: close\r\n\r\n");stream.Write(header,0,header.Length);using(var input=File.OpenRead(apk))Wire.Copy(input,stream,info.Length,null);}
                else {string body="<!doctype html><meta name=viewport content='width=device-width,initial-scale=1'><title>Share-Me-Rowell</title><body style='background:#0c131e;color:white;font:18px system-ui;padding:32px'><h1>Share-Me-Rowell</h1><p>Install the Android app, then scan the Send or Receive code on the PC.</p><p><a style='display:inline-block;background:#70e2c2;color:#0c131e;padding:18px;border-radius:12px;text-decoration:none' href='/"+Token+"/app.apk'>Download Android APK</a></p><p>After download, tap the APK and follow Android's installation prompt. This connection works on your local Wi-Fi.</p></body>";byte[] bytes=Encoding.UTF8.GetBytes(body);byte[] header=Encoding.ASCII.GetBytes("HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: "+bytes.Length+"\r\nCache-Control: no-store\r\nContent-Security-Policy: default-src 'none'; style-src 'unsafe-inline'\r\nConnection: close\r\n\r\n");stream.Write(header,0,header.Length);stream.Write(bytes,0,bytes.Length);}
            }}
        }catch{client.Close();}finally{lock(gate)clients.Remove(client);}}
        public void Dispose(){stopped=true;listener.Stop();lock(gate){foreach(var client in clients.ToArray())client.Close();clients.Clear();}}
    }
}
