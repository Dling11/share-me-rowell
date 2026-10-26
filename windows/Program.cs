using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Net;
using System.Runtime.InteropServices;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;
using QRCoder;

namespace ShareMeRowell {
    static class Program {
        [STAThread] static int Main(string[] args) {
            if(args.Length>1 && args[0]=="--render-icon"){Brand.Export(args[1]);return 0;}
            if(args.Length>0 && args[0]=="--test-server")return TestServer(args);
            Application.EnableVisualStyles();Application.SetCompatibleTextRenderingDefault(false);
            if(args.Length>1 && args[0]=="--capture") {
                string folder=Path.GetFullPath(args[1]);Directory.CreateDirectory(folder);
                using(var form=new MainForm(folder)){form.ShowInTaskbar=false;form.StartPosition=FormStartPosition.Manual;form.Location=new Point(-20000,-20000);form.Shown+=(s,e)=>form.BeginInvoke(new Action(()=>{form.CapturePreview(folder);form.Close();}));Application.Run(form);}return 0;
            }
            try{Application.Run(new MainForm());return 0;}catch(Exception error){MessageBox.Show(error.Message,"Share-Me-Rowell",MessageBoxButtons.OK,MessageBoxIcon.Error);return 1;}
        }
        static int TestServer(string[] args) {
            try {
                string root=Path.GetFullPath(args[1]);Directory.CreateDirectory(root);
                using(var server=new TransferServer(Path.Combine(root,"received"),Path.Combine(root,"state"))){
                    server.Approve=(peer,files)=>true;
                    if(args.Length>2&&args[2]=="send"){server.Mode="send";server.Sources=args.Skip(3).Select(Source.Prepare).ToList();}
                    server.Start(IPAddress.Loopback,0);
                    using(var generator=new QRCodeGenerator())using(var data=generator.CreateQrCode(server.Pairing("127.0.0.1"),QRCodeGenerator.ECCLevel.M))using(var code=new QRCode(data))using(var image=code.GetGraphic(6))image.Save(Path.Combine(root,"pairing.png"));
                    var info=new Dictionary<string,object>{{"port",server.Port},{"code",server.Code},{"fp",server.Fingerprint},{"pairing",server.Pairing("127.0.0.1")}};
                    InstallerServer installer=null;if(File.Exists(Path.Combine(root,"test-installer.apk"))){installer=new InstallerServer(Path.Combine(root,"test-installer.apk"),IPAddress.Loopback,0);info["installerUrl"]=installer.Url("127.0.0.1");}
                    File.WriteAllText(Path.Combine(root,"server.json"),Wire.Encode(info));File.WriteAllText(Path.Combine(root,"ready"),"ready");
                    while(!File.Exists(Path.Combine(root,"stop")))Thread.Sleep(100);
                    if(installer!=null)installer.Dispose();
                }return 0;
            }catch(Exception error){File.WriteAllText(Path.Combine(args[1],"error.txt"),error.ToString());return 1;}
        }
    }
    public sealed class MainForm:Form {
        readonly TransferServer server;
        InstallerServer installer;
        readonly string stateDirectory,receiveDirectory;
        readonly List<Source> sources=new List<Source>();
        readonly ListView files=new ListView(),history=new ListView();
        readonly PictureBox qr=new PictureBox();
        readonly Label title=new Label(),subtitle=new Label(),qrTitle=new Label(),qrHint=new Label(),expiry=new Label(),status=new Label(),summary=new Label(),qrEmpty=new Label();
        readonly ComboBox addresses=new ComboBox();
        readonly TransferBar bar=new TransferBar();
        readonly Panel pages=new Panel(),sendPage=new Panel(),receivePage=new Panel(),historyPage=new Panel();
        readonly SoftCard rightCard=new SoftCard();
        SoftCard codeCanvas;
        readonly TableLayoutPanel cards=new TableLayoutPanel();
        readonly SoftButton choose=new SoftButton(),clear=new SoftButton(),refresh=new SoftButton(),cancel=new SoftButton();
        readonly Dictionary<string,SoftButton> navigation=new Dictionary<string,SoftButton>();
        string view="send",direction="send";
        bool preparing,closing;
        DateTime lastProgress=DateTime.MinValue;
        readonly System.Windows.Forms.Timer timer=new System.Windows.Forms.Timer{Interval=400};
        [DllImport("dwmapi.dll")] static extern int DwmSetWindowAttribute(IntPtr handle,int attribute,ref int value,int size);
        protected override void OnHandleCreated(EventArgs e){base.OnHandleCreated(e);int enabled=1;try{DwmSetWindowAttribute(Handle,20,ref enabled,4);}catch{}}
        public MainForm(string captureRoot=null) {
            Text="Share-Me-Rowell";ClientSize=new Size(1160,780);MinimumSize=new Size(1080,730);BackColor=Palette.Page;ForeColor=Palette.Text;Font=Palette.Font(10);AutoScaleMode=AutoScaleMode.Dpi;
            Icon=Icon.ExtractAssociatedIcon(Application.ExecutablePath);
            stateDirectory=captureRoot==null?Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),"Share-Me-Rowell"):Path.Combine(captureRoot,"state");
            receiveDirectory=captureRoot==null?Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile),"Downloads","SHARE-ME-ROWELL"):Path.Combine(captureRoot,"received");
            server=new TransferServer(receiveDirectory,stateDirectory);server.Start(captureRoot==null?IPAddress.Any:IPAddress.Loopback,captureRoot==null?48290:0);
            server.Approve=Approve;server.Progress=UpdateProgress;server.Status=message=>Ui(()=>status.Text=message);server.Completed=AddHistory;
            var root=new TableLayoutPanel{Dock=DockStyle.Fill,ColumnCount=2,RowCount=1,Margin=new Padding(0)};root.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute,214));root.ColumnStyles.Add(new ColumnStyle(SizeType.Percent,100));Controls.Add(root);
            var side=new Panel{Dock=DockStyle.Fill,BackColor=Palette.Sidebar,Padding=new Padding(20)};root.Controls.Add(side,0,0);
            var logo=new PictureBox{Image=Brand.Bitmap(100),SizeMode=PictureBoxSizeMode.Zoom,Bounds=new Rectangle(20,27,54,54)};side.Controls.Add(logo);
            side.Controls.Add(Label("Share-Me",16,true,Palette.Text,new Rectangle(81,29,125,29),Palette.Sidebar));side.Controls.Add(Label("ROWELL",9,true,Palette.Mint,new Rectangle(83,59,110,20),Palette.Sidebar));
            int y=146;foreach(string item in new[]{"send","receive","history"}){string id=item;var button=ActionButton(id=="send"?"↗   Send files":id=="receive"?"↓   Receive files":"◷   Recent transfers",false);button.SetBounds(18,y,178,49);button.BackColor=Palette.Sidebar;button.Ghost=true;button.Click+=(s,e)=>Navigate(id);side.Controls.Add(button);navigation[id]=button;y+=60;}
            var sideBottom=new Panel{Dock=DockStyle.Bottom,Height=166,BackColor=Palette.Sidebar};side.Controls.Add(sideBottom);
            var install=ActionButton("Get the phone app",false);install.Dock=DockStyle.Top;install.Click+=(s,e)=>ShowInstaller();sideBottom.Controls.Add(install);
            var local=Label("●  Local & offline\n\nVersion 1.0.0 · By Rowell",9,false,Palette.Muted,new Rectangle(4,72,176,80),Palette.Sidebar);sideBottom.Controls.Add(local);
            var main=new TableLayoutPanel{Dock=DockStyle.Fill,ColumnCount=1,RowCount=3,Padding=new Padding(24,16,24,18)};main.RowStyles.Add(new RowStyle(SizeType.Absolute,92));main.RowStyles.Add(new RowStyle(SizeType.Percent,100));main.RowStyles.Add(new RowStyle(SizeType.Absolute,102));root.Controls.Add(main,1,0);
            var header=new Panel{Dock=DockStyle.Fill,BackColor=Palette.Page};title.SetBounds(0,9,700,44);title.Font=Palette.Font(25,true);title.ForeColor=Palette.Text;subtitle.SetBounds(1,57,720,28);subtitle.ForeColor=Palette.Muted;header.Controls.AddRange(new Control[]{title,subtitle});main.Controls.Add(header,0,0);
            cards.Dock=DockStyle.Fill;cards.ColumnCount=2;cards.ColumnStyles.Add(new ColumnStyle(SizeType.Percent,100));cards.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute,328));main.Controls.Add(cards,0,1);
            var left=new SoftCard{Dock=DockStyle.Fill,Margin=new Padding(0,0,18,0)};pages.Dock=DockStyle.Fill;pages.BackColor=Palette.Card;left.Controls.Add(pages);cards.Controls.Add(left,0,0);
            BuildSend();BuildReceive();BuildHistory();pages.Controls.AddRange(new Control[]{sendPage,receivePage,historyPage});
            rightCard.Dock=DockStyle.Fill;rightCard.Margin=new Padding(0);rightCard.Padding=new Padding(20);cards.Controls.Add(rightCard,1,0);BuildQr();
            var footer=new SoftCard{Dock=DockStyle.Fill,Margin=new Padding(0,18,0,0),Padding=new Padding(18,15,18,15)};main.Controls.Add(footer,0,2);
            var footerLayout=new TableLayoutPanel{Dock=DockStyle.Fill,ColumnCount=2,RowCount=1,BackColor=Palette.Card,Margin=new Padding(0)};footerLayout.RowStyles.Add(new RowStyle(SizeType.Percent,100));footerLayout.ColumnStyles.Add(new ColumnStyle(SizeType.Percent,100));footerLayout.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute,114));footer.Controls.Add(footerLayout);
            var progressPanel=new Panel{Dock=DockStyle.Fill,BackColor=Palette.Card,Padding=new Padding(0,0,18,0)};status.Dock=DockStyle.Top;status.Height=32;status.Text="Ready when you are. Connect both devices to the same network.";status.ForeColor=Palette.Text;status.BackColor=Palette.Card;bar.Dock=DockStyle.Bottom;bar.BackColor=Palette.Card;progressPanel.Controls.Add(status);progressPanel.Controls.Add(bar);footerLayout.Controls.Add(progressPanel,0,0);
            cancel.Text="Stop transfer";cancel.Dock=DockStyle.Fill;cancel.Enabled=false;cancel.Click+=(s,e)=>{if(MessageBox.Show("Stop this transfer? You can reconnect to resume the partial file.",Text,MessageBoxButtons.YesNo)==DialogResult.Yes)server.CancelActive();};footerLayout.Controls.Add(cancel,1,0);
            timer.Tick+=(s,e)=>{bool ready=!preparing&&!server.Busy;choose.Enabled=refresh.Enabled=ready;clear.Enabled=ready&&sources.Count>0;cancel.Enabled=server.Busy;foreach(var button in navigation.Values)button.Enabled=ready;expiry.Text=DateTime.UtcNow>server.Expires?"Code expired · refresh to reconnect":"Pairing code lasts 10 minutes";};timer.Start();
            FormClosing+=(s,e)=>{if(server.Busy&&MessageBox.Show("Stop the transfer and close? Partial files can resume later.",Text,MessageBoxButtons.YesNo)!=DialogResult.Yes){e.Cancel=true;return;}closing=true;timer.Stop();server.Dispose();if(installer!=null)installer.Dispose();if(qr.Image!=null)qr.Image.Dispose();logo.Image.Dispose();};
            LoadHistory();RefreshAddresses();Navigate("send");
        }
        static Label Label(string text,float size,bool bold,Color color,Rectangle bounds,Color background){return new Label{Text=text,Font=Palette.Font(size,bold),ForeColor=color,BackColor=background,Bounds=bounds};}
        static SoftButton ActionButton(string text,bool primary){return new SoftButton{Text=text,Primary=primary,BackColor=Palette.Card,Width=180,Height=46};}
        void BuildSend(){
            sendPage.Dock=DockStyle.Fill;sendPage.BackColor=Palette.Card;
            var layout=new TableLayoutPanel{Dock=DockStyle.Fill,ColumnCount=1,RowCount=6,Margin=new Padding(0)};foreach(float h in new[]{34f,30f,180f,40f})layout.RowStyles.Add(new RowStyle(SizeType.Absolute,h));layout.RowStyles.Add(new RowStyle(SizeType.Percent,100));layout.RowStyles.Add(new RowStyle(SizeType.Absolute,45));sendPage.Controls.Add(layout);
            var heading=Label("Choose your files",16,true,Palette.Text,Rectangle.Empty,Palette.Card);heading.Dock=DockStyle.Fill;layout.Controls.Add(heading);
            var caption=Label("Send directly to a phone or another device.",9,false,Palette.Muted,Rectangle.Empty,Palette.Card);caption.Dock=DockStyle.Fill;layout.Controls.Add(caption);
            var drop=new SoftCard{Dock=DockStyle.Fill,Fill=Palette.Raised,BackColor=Palette.Card,Dashed=true,Padding=new Padding(12)};
            var dropLayout=new TableLayoutPanel{Dock=DockStyle.Fill,ColumnCount=1,RowCount=3,BackColor=Palette.Raised};dropLayout.RowStyles.Add(new RowStyle(SizeType.Percent,40));dropLayout.RowStyles.Add(new RowStyle(SizeType.Percent,25));dropLayout.RowStyles.Add(new RowStyle(SizeType.Percent,35));drop.Controls.Add(dropLayout);
            var dropTitle=Label("↓  Drop files here",19,true,Palette.Text,Rectangle.Empty,Palette.Raised);dropTitle.Dock=DockStyle.Fill;dropTitle.TextAlign=ContentAlignment.MiddleCenter;dropLayout.Controls.Add(dropTitle);
            var dropHint=Label("Any file type · up to 16 files",9,false,Palette.Muted,Rectangle.Empty,Palette.Raised);dropHint.Dock=DockStyle.Fill;dropHint.TextAlign=ContentAlignment.MiddleCenter;dropLayout.Controls.Add(dropHint);
            choose.Text="Choose files";choose.Primary=true;choose.BackColor=Palette.Raised;choose.Anchor=AnchorStyles.None;choose.Width=180;choose.Click+=(s,e)=>ChooseFiles();dropLayout.Controls.Add(choose);layout.Controls.Add(drop);
            summary.Text="No files selected yet";summary.Dock=DockStyle.Fill;summary.TextAlign=ContentAlignment.MiddleLeft;summary.ForeColor=Palette.Muted;summary.Font=Palette.Font(9);layout.Controls.Add(summary);
            StyleList(files);files.Columns.Add("File",280);files.Columns.Add("Size",84);files.Resize+=(s,e)=>{files.Columns[0].Width=Math.Max(100,files.ClientSize.Width-95);};layout.Controls.Add(files);
            clear.Text="Clear selection";clear.Width=150;clear.Anchor=AnchorStyles.Left;clear.Click+=(s,e)=>{sources.Clear();files.Items.Clear();Configure();};layout.Controls.Add(clear);
            foreach(Control control in new Control[]{drop,dropLayout,dropTitle,dropHint,files,sendPage}){control.AllowDrop=true;control.DragEnter+=(s,e)=>{if(e.Data.GetDataPresent(DataFormats.FileDrop)&&!preparing&&!server.Busy)e.Effect=DragDropEffects.Copy;};control.DragDrop+=(s,e)=>Prepare((string[])e.Data.GetData(DataFormats.FileDrop));}
        }
        void BuildReceive(){
            receivePage.Dock=DockStyle.Fill;receivePage.BackColor=Palette.Card;
            var layout=new TableLayoutPanel{Dock=DockStyle.Fill,ColumnCount=1,RowCount=7};layout.RowStyles.Add(new RowStyle(SizeType.Absolute,54));layout.RowStyles.Add(new RowStyle(SizeType.Absolute,48));layout.RowStyles.Add(new RowStyle(SizeType.Absolute,150));layout.RowStyles.Add(new RowStyle(SizeType.Absolute,32));layout.RowStyles.Add(new RowStyle(SizeType.Absolute,70));layout.RowStyles.Add(new RowStyle(SizeType.Absolute,50));layout.RowStyles.Add(new RowStyle(SizeType.Percent,100));receivePage.Controls.Add(layout);
            var heading=Label("Ready for your files",19,true,Palette.Text,Rectangle.Empty,Palette.Card);heading.Dock=DockStyle.Fill;layout.Controls.Add(heading);
            var caption=Label("Send from your phone in three simple steps.",10,false,Palette.Muted,Rectangle.Empty,Palette.Card);caption.Dock=DockStyle.Fill;layout.Controls.Add(caption);
            var steps=Label("1   Choose files in Send on your phone.\n\n2   Scan this PC's receiver code.\n\n3   Approve the request here.",11,false,Palette.Text,Rectangle.Empty,Palette.Card);steps.Dock=DockStyle.Fill;layout.Controls.Add(steps);
            var folderTitle=Label("SAVED TO",9,true,Palette.Muted,Rectangle.Empty,Palette.Card);folderTitle.Dock=DockStyle.Fill;layout.Controls.Add(folderTitle);
            var folder=Label(receiveDirectory,10,false,Palette.Text,Rectangle.Empty,Palette.Card);folder.Dock=DockStyle.Fill;layout.Controls.Add(folder);
            var open=ActionButton("Open receive folder",true);open.Anchor=AnchorStyles.Left;open.Width=190;open.Click+=(s,e)=>Process.Start(new ProcessStartInfo("explorer.exe",receiveDirectory){UseShellExecute=true});layout.Controls.Add(open);
        }
        void BuildHistory(){historyPage.Dock=DockStyle.Fill;historyPage.BackColor=Palette.Card;StyleList(history);history.Columns.Add("Time",145);history.Columns.Add("Action",90);history.Columns.Add("File",300);historyPage.Controls.Add(history);history.Resize+=(s,e)=>history.Columns[2].Width=Math.Max(140,history.ClientSize.Width-245);}
        void StyleList(ListView list){list.Dock=DockStyle.Fill;list.View=View.Details;list.FullRowSelect=true;list.HeaderStyle=ColumnHeaderStyle.None;list.BackColor=Palette.Card;list.ForeColor=Palette.Text;list.BorderStyle=BorderStyle.None;list.Font=Palette.Font(10);list.ShowItemToolTips=true;var rows=new ImageList{ImageSize=new Size(1,34)};list.SmallImageList=rows;}
        void BuildQr(){
            var layout=new TableLayoutPanel{Dock=DockStyle.Fill,ColumnCount=1,RowCount=7,BackColor=Palette.Card};layout.RowStyles.Add(new RowStyle(SizeType.Absolute,34));layout.RowStyles.Add(new RowStyle(SizeType.Absolute,72));layout.RowStyles.Add(new RowStyle(SizeType.Percent,100));layout.RowStyles.Add(new RowStyle(SizeType.Absolute,34));layout.RowStyles.Add(new RowStyle(SizeType.Absolute,32));layout.RowStyles.Add(new RowStyle(SizeType.Absolute,26));layout.RowStyles.Add(new RowStyle(SizeType.Absolute,46));rightCard.Controls.Add(layout);
            qrTitle.Dock=DockStyle.Fill;qrTitle.ForeColor=Palette.Text;qrTitle.Font=Palette.Font(16,true);layout.Controls.Add(qrTitle);qrHint.Dock=DockStyle.Fill;qrHint.ForeColor=Palette.Muted;qrHint.Font=Palette.Font(9);layout.Controls.Add(qrHint);
            codeCanvas=new SoftCard{Dock=DockStyle.Fill,Fill=Palette.Raised,BackColor=Palette.Card,Padding=new Padding(10),Margin=new Padding(0,0,0,12)};qr.Dock=DockStyle.Fill;qr.SizeMode=PictureBoxSizeMode.Zoom;qr.BackColor=Color.White;qrEmpty.Dock=DockStyle.Fill;qrEmpty.TextAlign=ContentAlignment.MiddleCenter;qrEmpty.ForeColor=Palette.Muted;qrEmpty.BackColor=Palette.Raised;qrEmpty.Text="Choose files to show\nyour sharing code";codeCanvas.Controls.Add(qr);codeCanvas.Controls.Add(qrEmpty);layout.Controls.Add(codeCanvas);
            var network=Label("CONNECTION ADDRESS",8,true,Palette.Muted,Rectangle.Empty,Palette.Card);network.Dock=DockStyle.Fill;network.TextAlign=ContentAlignment.BottomLeft;layout.Controls.Add(network);
            addresses.Dock=DockStyle.Fill;addresses.DropDownStyle=ComboBoxStyle.DropDownList;addresses.FlatStyle=FlatStyle.Flat;addresses.BackColor=Palette.Raised;addresses.ForeColor=Palette.Text;addresses.Font=Palette.Font(9);addresses.DrawMode=DrawMode.OwnerDrawFixed;addresses.DrawItem+=(s,e)=>{using(var brush=new SolidBrush(Palette.Raised))e.Graphics.FillRectangle(brush,e.Bounds);if(e.Index>=0)TextRenderer.DrawText(e.Graphics,addresses.Items[e.Index].ToString(),addresses.Font,e.Bounds,Palette.Text,TextFormatFlags.VerticalCenter);};addresses.SelectedIndexChanged+=(s,e)=>RenderCode();layout.Controls.Add(addresses);
            expiry.Dock=DockStyle.Fill;expiry.ForeColor=Palette.Muted;expiry.Font=Palette.Font(8);expiry.TextAlign=ContentAlignment.MiddleLeft;layout.Controls.Add(expiry);refresh.Text="Refresh code";refresh.Dock=DockStyle.Fill;refresh.Click+=(s,e)=>{RefreshAddresses();server.Refresh();RenderCode();};layout.Controls.Add(refresh);
        }
        void RefreshAddresses(){string selected=addresses.SelectedItem as string;addresses.Items.Clear();foreach(string ip in Wire.Addresses())addresses.Items.Add(ip);addresses.SelectedItem=selected;if(addresses.SelectedIndex<0)addresses.SelectedIndex=0;}
        void Navigate(string page){if(server.Busy||preparing)return;view=page;if(page!="history")direction=page;foreach(var entry in navigation){entry.Value.Selected=entry.Key==page;entry.Value.Invalidate();}sendPage.Visible=page=="send";receivePage.Visible=page=="receive";historyPage.Visible=page=="history";rightCard.Visible=page!="history";cards.ColumnStyles[1].Width=page=="history"?0:328;title.Text=page=="send"?"Send something good.":page=="receive"?"Files, coming your way.":"Your recent transfers.";subtitle.Text=page=="history"?"Verified transfers, all in one place.":"Direct sharing. No account. No cloud.";if(page!="history")Configure();}
        void Configure(){if(server.Busy)return;server.Mode=direction;server.Sources=new List<Source>(sources);server.Refresh();summary.Text=sources.Count==0?"No files selected yet":sources.Count+" file"+(sources.Count==1?"":"s")+" selected · "+Size(sources.Sum(s=>s.Meta.size));RenderCode();}
        void RenderCode(){if(addresses.SelectedItem==null)return;bool sending=direction=="send";qrTitle.Text=sending?"Scan to receive":"Scan to send";qrHint.Text=sending?"On your phone, open Receive and scan this sender code. Approve the request here.":"On your phone, choose Send, select files and scan this receiver code.";bool ready=(!sending||sources.Count>0)&&addresses.SelectedItem.ToString()!="127.0.0.1";var previous=qr.Image;qr.Image=ready?Qr(server.Pairing(addresses.SelectedItem.ToString())):null;qrEmpty.Visible=!ready;codeCanvas.Fill=ready?Color.White:Palette.Raised;codeCanvas.Invalidate();expiry.Text="Pairing code lasts 10 minutes";if(!ready)qrEmpty.Text=addresses.SelectedItem.ToString()=="127.0.0.1"?"Connect to Wi-Fi\nor a phone hotspot":"Choose files to show\nyour sharing code";if(previous!=null)previous.Dispose();}
        static Bitmap Qr(string value){using(var generator=new QRCodeGenerator())using(var data=generator.CreateQrCode(value,QRCodeGenerator.ECCLevel.M))using(var code=new QRCode(data))return code.GetGraphic(8);}
        void ChooseFiles(){using(var dialog=new OpenFileDialog{Multiselect=true,Title="Choose up to 16 files",Filter="All files|*.*"})if(dialog.ShowDialog()==DialogResult.OK)Prepare(dialog.FileNames);}
        void Prepare(string[] paths){if(server.Busy||preparing)return;if(sources.Count+paths.Length>16){MessageBox.Show("Choose up to 16 files per batch.",Text);return;}preparing=true;status.Text="Preparing your files…";ThreadPool.QueueUserWorkItem(_=>{try{var ready=paths.Select(Source.Prepare).ToList();Ui(()=>{sources.AddRange(ready);foreach(var source in ready)files.Items.Add(new ListViewItem(new[]{source.Meta.name,Size(source.Meta.size)}){ToolTipText=source.Meta.name});preparing=false;Configure();status.Text="Files ready. Scan the code from Receive on your phone.";});}catch(Exception error){Ui(()=>{preparing=false;status.Text=error.Message;});}});}
        bool Approve(string peer,List<FileMeta> batch){var decision=new TaskCompletionSource<bool>();Ui(()=>{using(var dialog=Dialog("Allow this transfer?",peer+" wants to "+(server.Mode=="receive"?"send":"receive")+" "+batch.Count+" file(s).",new Size(560,480))){var table=(TableLayoutPanel)dialog.Controls[0];var list=new ListBox{Dock=DockStyle.Fill,BackColor=Palette.Raised,ForeColor=Palette.Text,BorderStyle=BorderStyle.None,Font=Palette.Font(10)};foreach(var file in batch)list.Items.Add(file.name+"  ·  "+Size(file.size));table.Controls.Add(list,0,2);var footer=new FlowLayoutPanel{Dock=DockStyle.Fill,FlowDirection=FlowDirection.RightToLeft,BackColor=Palette.Card};var yes=ActionButton("Allow transfer",true);var no=ActionButton("Decline",false);yes.DialogResult=DialogResult.Yes;no.DialogResult=DialogResult.No;footer.Controls.Add(yes);footer.Controls.Add(no);table.Controls.Add(footer,0,3);dialog.AcceptButton=yes;dialog.CancelButton=no;decision.TrySetResult(dialog.ShowDialog(this)==DialogResult.Yes);}});return decision.Task.Wait(TimeSpan.FromSeconds(90))&&decision.Task.Result;}
        static Form Dialog(string heading,string description,Size size){var form=new Form{Text="Share-Me-Rowell",ClientSize=size,BackColor=Palette.Card,ForeColor=Palette.Text,StartPosition=FormStartPosition.CenterParent,Font=Palette.Font(10),MinimizeBox=false,MaximizeBox=false};var table=new TableLayoutPanel{Dock=DockStyle.Fill,ColumnCount=1,RowCount=4,Padding=new Padding(24)};table.RowStyles.Add(new RowStyle(SizeType.Absolute,55));table.RowStyles.Add(new RowStyle(SizeType.Absolute,66));table.RowStyles.Add(new RowStyle(SizeType.Percent,100));table.RowStyles.Add(new RowStyle(SizeType.Absolute,60));form.Controls.Add(table);var title=Label(heading,23,true,Palette.Text,Rectangle.Empty,Palette.Card);title.Dock=DockStyle.Fill;table.Controls.Add(title,0,0);var hint=Label(description,10,false,Palette.Muted,Rectangle.Empty,Palette.Card);hint.Dock=DockStyle.Fill;table.Controls.Add(hint,0,1);return form;}
        void ShowInstaller(){string apk=Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Android","Share-Me-Rowell.apk");if(!File.Exists(apk)){MessageBox.Show("Use the Windows ZIP that includes the Android installer, or download the standalone APK from the release.",Text);return;}if(installer!=null)installer.Dispose();installer=new InstallerServer(apk,IPAddress.Any,48291);using(var dialog=Dialog("Get it on your phone.","Use your phone's normal camera to scan this code. Both devices need the same Wi-Fi or hotspot.",new Size(470,660))){string url=installer.Url(addresses.SelectedItem.ToString());var table=(TableLayoutPanel)dialog.Controls[0];var picture=new PictureBox{Dock=DockStyle.Fill,BackColor=Color.White,SizeMode=PictureBoxSizeMode.Zoom,Image=Qr(url)};table.Controls.Add(picture,0,2);var address=new TextBox{Dock=DockStyle.Fill,Multiline=true,ReadOnly=true,Text=url,BackColor=Palette.Raised,ForeColor=Palette.Text,BorderStyle=BorderStyle.None};table.Controls.Add(address,0,3);dialog.ShowDialog(this);picture.Image.Dispose();}}
        void UpdateProgress(string name,long done,long total){if((DateTime.UtcNow-lastProgress).TotalMilliseconds<150&&done<total)return;lastProgress=DateTime.UtcNow;Ui(()=>{status.Text=name+" · "+Size(done)+" / "+Size(total);bar.Value=total==0?100:(int)Math.Min(100,done*100/total);});}
        void AddHistory(string direction,string name,long size,string path){Ui(()=>{var row=new ListViewItem(new[]{DateTime.Now.ToString("MMM d, h:mm tt"),direction,name}){Tag=path,ToolTipText=name};history.Items.Insert(0,row);while(history.Items.Count>50)history.Items.RemoveAt(50);SaveHistory();});}
        void SaveHistory(){try{File.WriteAllText(Path.Combine(stateDirectory,"history.json"),Wire.Encode(history.Items.Cast<ListViewItem>().Select(item=>new[]{item.SubItems[0].Text,item.SubItems[1].Text,item.SubItems[2].Text,Convert.ToString(item.Tag)}).ToArray()));}catch{}}
        void LoadHistory(){try{var rows=new System.Web.Script.Serialization.JavaScriptSerializer().Deserialize<string[][]>(File.ReadAllText(Path.Combine(stateDirectory,"history.json")));foreach(var row in rows.Take(50))history.Items.Add(new ListViewItem(row.Take(3).ToArray()){Tag=row[3],ToolTipText=row[2]});}catch{}}
        void Ui(Action action){try{if(!closing&&!IsDisposed&&IsHandleCreated)BeginInvoke(action);}catch(InvalidOperationException){}}
        public void CapturePreview(string folder){using(var image=new Bitmap(Width,Height)){DrawToBitmap(image,new Rectangle(0,0,Width,Height));image.Save(Path.Combine(folder,"send.png"));}Navigate("receive");Application.DoEvents();using(var image=new Bitmap(Width,Height)){DrawToBitmap(image,new Rectangle(0,0,Width,Height));image.Save(Path.Combine(folder,"receive.png"));}File.WriteAllText(Path.Combine(folder,"pairing.txt"),server.Pairing(addresses.SelectedItem.ToString()));}
        public new static string Size(long bytes){string[] units={"B","KB","MB","GB","TB"};double value=bytes;int i=0;while(value>=1024&&i<units.Length-1){value/=1024;i++;}return value.ToString(i==0?"0":"0.0",CultureInfo.InvariantCulture)+" "+units[i];}
    }
}
