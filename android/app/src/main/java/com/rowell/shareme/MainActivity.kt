package com.rowell.shareme

import android.Manifest
import android.app.DownloadManager
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat
import java.util.Date

class QrActivity:CaptureActivity()
class MainActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);enableEdgeToEdge(statusBarStyle=SystemBarStyle.dark(Bg.toArgb()),navigationBarStyle=SystemBarStyle.dark(Bg.toArgb()));setContent{ShareTheme{AppScreen()}}}
    private fun start(action:String,extra:(Intent)->Unit={})=runCatching{TransferService.command(this,action,extra)}.onFailure{Model.update(status=it.message?:"Could not start sharing.")}
    @Composable private fun AppScreen(){
        val state by Model.state.collectAsState()
        var page by rememberSaveable{mutableStateOf("home")}
        var pendingHotspot by rememberSaveable{mutableStateOf("")}
        var scanned by rememberSaveable{mutableStateOf("")}
        var paste by remember{mutableStateOf(false)}
        var pasted by remember{mutableStateOf("")}
        var stop by remember{mutableStateOf(false)}
        var disconnect by remember{mutableStateOf(false)}
        var clear by remember{mutableStateOf(false)}
        var appTools by rememberSaveable{mutableStateOf(false)}
        val permissions=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){granted->
            val wifi=if(Build.VERSION.SDK_INT>=33)Manifest.permission.NEARBY_WIFI_DEVICES else Manifest.permission.ACCESS_FINE_LOCATION
            if(granted[wifi]==true){if(scanned.isNotEmpty()){val value=scanned;scanned="";start("connect"){it.putExtra("pairing",value)}}else if(pendingHotspot.isNotEmpty()){start("hotspot"){it.putExtra("mode",pendingHotspot)};pendingHotspot=""}}
            else Model.update(status="Nearby Wi-Fi access is needed for hotspot pairing. You can also join the hotspot in Settings and use a code without hotspot details.")
        }
        fun connect(text:String){runCatching{Pairing.parse(text)}.onSuccess{pair->
            if(pair.mode=="receive"&&state.files.isEmpty()){page="send";Model.update(status="Choose files first, then scan the receiver.");return@onSuccess}
            if(pair.ssid.isNotEmpty()&&TransferService.wifiForHosts(this@MainActivity,pair.hosts)==null){scanned=text;permissions.launch(wifiPermissions())}
            else start("connect"){it.putExtra("pairing",text)}
        }.onFailure{Model.update(status=it.message?:"Scan a Share-Me-Rowell pairing code.")}}
        val scan=rememberLauncherForActivityResult(ScanContract()){result->result.contents?.let(::connect)}
        val notify=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){}
        fun begin(action:String){if(Build.VERSION.SDK_INT>=33)notify.launch(Manifest.permission.POST_NOTIFICATIONS);start(action)}
        val pick=rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()){uris->
            if(uris.isNotEmpty()){
                if((state.files.map{it.uri}+uris).distinct().size>16)Model.update(status="Choose up to 16 files per batch.")
                else{uris.forEach{runCatching{contentResolver.takePersistableUriPermission(it,Intent.FLAG_GRANT_READ_URI_PERMISSION)}};if(Build.VERSION.SDK_INT>=33)notify.launch(Manifest.permission.POST_NOTIFICATIONS);start("prepare"){it.putStringArrayListExtra("uris",ArrayList(uris.map(Uri::toString)))}}
            }
        }
        fun scanCode(){scan.launch(ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE).setPrompt("Scan the other device's Share-Me-Rowell code").setBeepEnabled(false).setOrientationLocked(false).setCaptureActivity(QrActivity::class.java))}
        Scaffold(containerColor=Bg,topBar={Row(Modifier.fillMaxWidth().statusBarsPadding().padding(20.dp,12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
            Image(painterResource(R.drawable.ic_brand),"Share-Me-Rowell",Modifier.size(46.dp))
            Column{Text("Share-Me",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text("ROWELL",style=MaterialTheme.typography.labelSmall,color=Mint,fontWeight=FontWeight.Bold)}
            Spacer(Modifier.weight(1f));Surface(color=Mint.copy(alpha=.10f),shape=RoundedCornerShape(30.dp)){Text("Local & offline",Modifier.padding(12.dp,8.dp),style=MaterialTheme.typography.labelSmall,color=Mint)}
        }},bottomBar={NavigationBar(containerColor=Bg,tonalElevation=0.dp){listOf(Triple("home","Home",Icons.Rounded.Home),Triple("send","Send",Icons.AutoMirrored.Rounded.Send),Triple("receive","Receive",Icons.Rounded.Download),Triple("history","Recent",Icons.Rounded.History)).forEach{(id,label,icon)->NavigationBarItem(selected=page==id,onClick={page=id},icon={Icon(icon,null)},label={Text(label)},colors=NavigationBarItemDefaults.colors(selectedIconColor=Mint,selectedTextColor=Mint,indicatorColor=Mint.copy(alpha=.12f),unselectedIconColor=Muted,unselectedTextColor=Muted))}}}){insets->
            Column(Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
                if(state.connection.visible)DeviceCard(state,onDisconnect={disconnect=true},onRetry={scanCode()})
                if(state.active&&!state.connection.visible)CardBox{
                    Text(state.status,fontWeight=FontWeight.SemiBold)
                    if(state.preparing&&state.current.isNotEmpty())Text(state.current,color=Muted,maxLines=2,overflow=TextOverflow.Ellipsis)
                    if(state.busy&&state.total>0){LinearProgressIndicator(progress={(state.done.toDouble()/state.total).toFloat().coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth());Text("${size(state.done)} / ${size(state.total)}",color=Muted)}
                    else if(state.busy)LinearProgressIndicator(Modifier.fillMaxWidth())
                    if(state.preparing)OutlinedButton(onClick={start("cancel_preparation")}){Text("Cancel preparation")}
                    else OutlinedButton(onClick={stop=true}){Text("Stop sharing")}
                }else if(!state.connection.visible&&state.status!="Ready to share")CardBox{Text(state.status)}
                if(state.active&&state.connection.visible)TextButton(onClick={stop=true}){Text("Stop sharing & hotspot",color=Muted)}
                when(page){
                    "home"->{
                        Spacer(Modifier.height(4.dp))
                        Text("Share something\ngood.",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold)
                        Text("Big files. Little effort. Right nearby.",color=Muted)
                        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
                            ActionCard("Send","Choose your files",Icons.AutoMirrored.Rounded.Send,true,Modifier.weight(1f)){page="send"}
                            ActionCard("Receive","Scan & accept",Icons.Rounded.Download,false,Modifier.weight(1f)){page="receive"}
                        }
                        CardBox{
                            FeatureRow(Icons.Rounded.Wifi,"Stay nearby","Use the same Wi-Fi or a hotspot. No internet needed.")
                            HorizontalDivider(color=Muted.copy(alpha=.12f))
                            FeatureRow(Icons.AutoMirrored.Rounded.InsertDriveFile,"Make room for anything","Photos, videos, APKs, ZIPs and even large ISOs.")
                        }
                        CardBox{FeatureRow(Icons.Rounded.Computer,"Sharing with a PC?","Open Send or Receive on Windows, then scan its code here.")}
                        TextButton(onClick={appTools=!appTools},modifier=Modifier.fillMaxWidth()){Icon(Icons.Rounded.Share,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text(if(appTools)"Hide app sharing" else "Share the app")}
                        if(appTools)CardBox{Text("Bring another phone along",fontWeight=FontWeight.Bold);Text("Send this installer, or let the other phone download it on your local network.",color=Muted)
                            Button(onClick={shareInstaller()},enabled=!state.busy){Text("Share APK · Bluetooth / other apps")}
                            OutlinedButton(onClick={begin("installer")},enabled=!state.busy){Text("Show local download QR")}
                            if(state.appUrl.isNotEmpty())QrCard(state.appUrl,"Scan with the other phone's normal camera. It must join this Wi-Fi/hotspot first.")
                        }
                        TextButton(onClick={clear=true},enabled=!state.busy,modifier=Modifier.fillMaxWidth()){Text("Remove unfinished downloads",color=Muted)}
                    }
                    "send"->{
                        Text("Send files",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
                        Text("1 · Add files, then review",fontWeight=FontWeight.Bold)
                        Text("Photos, videos, APKs, ZIPs, ISOs and documents.",color=Muted,style=MaterialTheme.typography.bodySmall)
                        Button(onClick={pick.launch(arrayOf("*/*"))},enabled=!state.busy,modifier=Modifier.fillMaxWidth()){Icon(Icons.Rounded.Add,null);Spacer(Modifier.width(8.dp));Text(if(state.files.isEmpty())"Add files" else "Add more files")}
                        if(state.files.isNotEmpty()){
                            CardBox{
                                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Your selection",fontWeight=FontWeight.Bold);Text("${state.files.size} files · ${size(state.files.sumOf{it.meta.size})}",color=Mint,style=MaterialTheme.typography.bodySmall)};TextButton(onClick={start("clear_selection")},enabled=!state.busy){Text("Clear")}}
                                Column(Modifier.heightIn(max=280.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
                                    state.files.forEach{file->Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
                                        Icon(Icons.AutoMirrored.Rounded.InsertDriveFile,null,Modifier.size(26.dp),tint=Mint)
                                        Column(Modifier.weight(1f)){Text(file.meta.name,maxLines=2,overflow=TextOverflow.Ellipsis,style=MaterialTheme.typography.bodyMedium);Text(size(file.meta.size),color=Muted,style=MaterialTheme.typography.bodySmall)}
                                        IconButton(onClick={start("remove_file"){it.putExtra("id",file.meta.id)}},enabled=!state.busy){Icon(Icons.Rounded.Close,"Remove ${file.meta.name} from selection")}
                                    }}
                                }
                                if(!state.connection.phase.active)Text(if(!state.connection.visible)"Nothing sent yet. Removing a file here keeps the original." else "Review the files before sharing again. Your originals stay on this phone.",color=Muted,style=MaterialTheme.typography.bodySmall)
                            }
                            Text("2 · Connect & send",fontWeight=FontWeight.Bold)
                            Text("Sending starts after the other device approves.",color=Muted,style=MaterialTheme.typography.bodySmall)
                            Button(onClick={scanCode()},enabled=!state.busy,modifier=Modifier.fillMaxWidth()){Icon(Icons.Rounded.QrCodeScanner,null);Spacer(Modifier.width(8.dp));Text("Scan receiver's code")}
                            OutlinedButton(onClick={begin("send")},enabled=!state.busy,modifier=Modifier.fillMaxWidth()){Text("Show my sender code")}
                            if(state.pairing?.mode=="send")QrCard(state.pairing!!.text(),"On the other phone: Receive → Scan sender's code.")
                            TextButton(onClick={shareUris(state.files.map{it.uri})},enabled=!state.busy,modifier=Modifier.fillMaxWidth()){Text("More ways · Bluetooth / Android sharing")}
                        }
                        else if(!state.preparing)Text("Add files to start a transfer.",color=Muted)
                        HotspotBox("send",state){mode->pendingHotspot=mode;permissions.launch(wifiPermissions())}
                    }
                    "receive"->{
                        Text("Receive files",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
                        Text("Incoming files ask for your approval and are saved in Downloads/SHARE-ME-ROWELL.",color=Muted)
                        Button(onClick={scanCode()},enabled=!state.busy,modifier=Modifier.fillMaxWidth()){Icon(Icons.Rounded.QrCodeScanner,null);Spacer(Modifier.width(8.dp));Text("Scan sender's code")}
                        OutlinedButton(onClick={begin("receive")},enabled=!state.busy,modifier=Modifier.fillMaxWidth()){Text("Show my receiver code")}
                        if(state.pairing?.mode=="receive")QrCard(state.pairing!!.text(),"The sender chooses files, then scans this code. Codes expire after 10 minutes; show a fresh code if needed.")
                        HotspotBox("receive",state){mode->pendingHotspot=mode;permissions.launch(wifiPermissions())}
                        OutlinedButton(onClick={runCatching{startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))}}){Text("Open Downloads")}
                    }
                    "history"->{
                        Text("Recent transfers",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
                        if(state.history.isEmpty())Text("Completed transfers appear here.",color=Muted)
                        state.history.forEach{item->CardBox{
                            Text(item.name,fontWeight=FontWeight.SemiBold);Text("${item.direction} · ${size(item.size)}",color=Mint)
                            Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT).format(Date(item.time)),color=Muted,style=MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                                Button(onClick={open(item)}){Text(if(item.name.endsWith(".apk",true)&&item.direction=="Received")"Install APK" else "Open")}
                                OutlinedButton(onClick={shareUris(listOf(Uri.parse(item.uri)))}){Text("Share")}
                            }
                        }}
                    }
                }
                if(page!="home"&&!state.busy)TextButton(onClick={paste=true}){Text("Paste a pairing link instead")}
                Text("Share-Me-Rowell 1.0.2 · By Rowell",style=MaterialTheme.typography.bodySmall,color=Muted)
            }
        }
        state.approval?.let{request->AlertDialog(onDismissRequest={request.answer.complete(false)},title={Text(if(request.sending)"Allow this device to receive?" else "Accept these files?")},text={Column(Modifier.heightIn(max=300.dp).verticalScroll(rememberScrollState())){Text(request.name,fontWeight=FontWeight.Bold);Spacer(Modifier.height(10.dp));request.files.forEach{Text("${it.name} · ${size(it.size)}")};Spacer(Modifier.height(10.dp));Text("Total: ${size(request.files.sumOf{it.size})}")}},confirmButton={TextButton(onClick={request.answer.complete(true)}){Text("Accept")}},dismissButton={TextButton(onClick={request.answer.complete(false)}){Text("Decline")}})}
        if(paste)AlertDialog(onDismissRequest={paste=false},title={Text("Pairing link")},text={OutlinedTextField(pasted,{pasted=it.take(8192)},label={Text("Paste Share-Me-Rowell link")})},confirmButton={TextButton(onClick={paste=false;connect(pasted)}){Text("Connect")}},dismissButton={TextButton(onClick={paste=false}){Text("Cancel")}})
        if(stop)AlertDialog(onDismissRequest={stop=false},title={Text("Stop sharing?")},text={Text("An interrupted file can resume when you connect and send it again. A hotspot created here will also close.")},confirmButton={TextButton(onClick={stop=false;start("stop")}){Text("Stop")}},dismissButton={TextButton(onClick={stop=false}){Text("Keep sharing")}})
        if(disconnect)AlertDialog(onDismissRequest={disconnect=false},title={Text("Cancel this transfer?")},text={Text("Completed files stay saved. Scan again to resume unfinished files. Your hotspot stays on.")},confirmButton={TextButton(onClick={disconnect=false;if(Model.value.connection.phase.active)start("disconnect")}){Text("Cancel transfer")}},dismissButton={TextButton(onClick={disconnect=false}){Text("Keep sharing")}})
        if(clear)AlertDialog(onDismissRequest={clear=false},title={Text("Remove unfinished downloads?")},text={Text("Frees storage used by interrupted transfers. Completed files stay in Downloads.")},confirmButton={TextButton(onClick={clear=false;if(!Model.value.busy)lifecycleScope.launch{withContext(Dispatchers.IO){ReceiveStore(this@MainActivity).clearIncomplete()};Model.update(status="Unfinished downloads removed.")}}){Text("Remove")}},dismissButton={TextButton(onClick={clear=false}){Text("Cancel")}})
    }
    private fun shareInstaller(){lifecycleScope.launch{try{val uri=withContext(Dispatchers.IO){val destination=File(cacheDir,"installer/Share-Me-Rowell.apk");destination.parentFile!!.mkdirs();File(applicationInfo.sourceDir).copyTo(destination,true);FileProvider.getUriForFile(this@MainActivity,"$packageName.files",destination)};shareUris(listOf(uri))}catch(e:Exception){toast("Could not prepare the installer.")}}}
    private fun shareUris(uris:List<Uri>){if(uris.isEmpty())return;try{val intent=Intent(if(uris.size==1)Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE).apply{type="*/*";if(uris.size==1)putExtra(Intent.EXTRA_STREAM,uris[0])else putParcelableArrayListExtra(Intent.EXTRA_STREAM,ArrayList(uris));addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);clipData=ClipData.newRawUri("Shared files",uris[0]).also{clip->uris.drop(1).forEach{clip.addItem(ClipData.Item(it))}}};startActivity(Intent.createChooser(intent,"Share files · choose Bluetooth for offline sharing"))}catch(e:Exception){toast("Android could not open its sharing screen.")}}
    private fun open(item:History){
        val apk=item.name.endsWith(".apk",true)
        if(apk&&!packageManager.canRequestPackageInstalls()){toast("Allow installation from Share-Me-Rowell, then tap Install APK again.");startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:$packageName")));return}
        try{startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(item.uri),if(apk)"application/vnd.android.package-archive" else contentResolver.getType(Uri.parse(item.uri))?:"application/octet-stream").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))}catch(e:Exception){toast("No app could open this file. It is available in Downloads/SHARE-ME-ROWELL.")}
    }
    private fun toast(message:String)=Toast.makeText(this,message,Toast.LENGTH_LONG).show()
}
val Bg=Color(0xFF0C131E);val Panel=Color(0xFF162232);val Mint=Color(0xFF70E2C2);val Muted=Color(0xFF9CAFC3)
@Composable fun ShareTheme(content: @Composable () -> Unit){MaterialTheme(colorScheme=darkColorScheme(primary=Mint,onPrimary=Bg,background=Bg,surface=Panel,onSurface=Color(0xFFF5F8FF),secondary=Mint,surfaceVariant=Color(0xFF1D2D3F),onSurfaceVariant=Muted,outline=Color(0xFF3C5268)),shapes=Shapes(small=RoundedCornerShape(12.dp),medium=RoundedCornerShape(18.dp),large=RoundedCornerShape(24.dp)),content=content)}
@Composable fun CardBox(content:@Composable ColumnScope.()->Unit){Surface(color=Panel,shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)}}
@Composable fun ActionCard(title:String,hint:String,icon:ImageVector,primary:Boolean,modifier:Modifier,onClick:()->Unit){
    Surface(onClick=onClick,color=if(primary)Mint else Panel,contentColor=if(primary)Bg else Color(0xFFF5F8FF),shape=RoundedCornerShape(26.dp),modifier=modifier){Column(Modifier.padding(20.dp).heightIn(min=114.dp),verticalArrangement=Arrangement.SpaceBetween){Icon(icon,null,Modifier.size(32.dp));Column{Text(title,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text(hint,style=MaterialTheme.typography.bodySmall)}}}
}
@Composable fun FeatureRow(icon:ImageVector,title:String,hint:String){Row(horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.CenterVertically){Surface(color=Mint.copy(alpha=.10f),shape=RoundedCornerShape(14.dp)){Icon(icon,null,Modifier.padding(12.dp).size(23.dp),tint=Mint)};Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)){Text(title,fontWeight=FontWeight.SemiBold);Text(hint,color=Muted,style=MaterialTheme.typography.bodySmall)}}}
@Composable fun DeviceCard(state:ScreenState,onDisconnect:()->Unit,onRetry:()->Unit){
    val peer=state.connection
    CardBox{
        FeatureRow(Icons.Rounded.Devices,peer.name,"Sharing device")
        val tint=when(peer.phase){ConnectionPhase.CONNECTING,ConnectionPhase.APPROVAL,ConnectionPhase.WAITING->Color(0xFFFFD18A);ConnectionPhase.SENDING,ConnectionPhase.RECEIVING,ConnectionPhase.VERIFYING->Mint;else->Muted}
        Surface(color=tint.copy(alpha=.10f),shape=RoundedCornerShape(12.dp)){Text("●  ${peer.phase.label}",Modifier.padding(12.dp,8.dp),color=tint,style=MaterialTheme.typography.labelLarge)}
        Text(state.status,color=Muted,style=MaterialTheme.typography.bodySmall)
        if(peer.phase.active&&state.busy){
            if(state.total>0){if(state.fileCount>0)Text("File ${state.fileIndex} of ${state.fileCount}",color=Mint,style=MaterialTheme.typography.labelLarge);Text(state.current,maxLines=2,overflow=TextOverflow.Ellipsis);LinearProgressIndicator(progress={(state.done.toDouble()/state.total).toFloat().coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth());Text("${size(state.done)} / ${size(state.total)}",color=Muted,style=MaterialTheme.typography.bodySmall)}
            else LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        if(peer.phase.active)OutlinedButton(onClick=onDisconnect,modifier=Modifier.fillMaxWidth()){Icon(Icons.Rounded.LinkOff,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text(if(peer.phase in listOf(ConnectionPhase.SENDING,ConnectionPhase.RECEIVING,ConnectionPhase.VERIFYING))"Cancel transfer" else "Disconnect")}
        else{Text("Scan a fresh pairing code to share again.",color=Muted,style=MaterialTheme.typography.bodySmall);if(peer.phase!=ConnectionPhase.FINISHED)OutlinedButton(onClick=onRetry,modifier=Modifier.fillMaxWidth()){Icon(Icons.Rounded.QrCodeScanner,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("Scan a new code")}}
    }
}
@Composable fun QrCard(text:String,hint:String){
    val bitmap=remember(text){val matrix=QRCodeWriter().encode(text,BarcodeFormat.QR_CODE,800,800,mapOf(EncodeHintType.MARGIN to 4));Bitmap.createBitmap(800,800,Bitmap.Config.ARGB_8888).also{image->val pixels=IntArray(800*800){index->if(matrix[index%800,index/800])android.graphics.Color.BLACK else android.graphics.Color.WHITE};image.setPixels(pixels,0,800,0,0,800,800)}}
    CardBox{Image(bitmap.asImageBitmap(),"Pairing QR code",Modifier.fillMaxWidth().aspectRatio(1f).background(Color.White));Text(hint,color=Muted)}
}
@Composable fun HotspotBox(mode:String,state:ScreenState,start:(String)->Unit){CardBox{
    Text("No Wi-Fi router?",fontWeight=FontWeight.Bold);Text("Create a local hotspot. Other devices can join it without mobile data.",color=Muted)
    OutlinedButton(onClick={start(mode)},enabled=!state.busy&&(mode!="send"||state.files.isNotEmpty())){Text(if(state.ssid.isEmpty())"Create offline hotspot" else "Use this hotspot")}
    if(state.ssid.isNotEmpty()){Text("Network: ${state.ssid}");Text("Password: ${state.password}",color=Muted);Text("You can also join from Wi-Fi settings.",color=Muted)}
}}
fun size(bytes:Long):String{var value=bytes.toDouble();val units=arrayOf("B","KB","MB","GB","TB");var index=0;while(value>=1024&&index<4){value/=1024;index++};return if(index==0)"$bytes B" else "%.1f %s".format(value,units[index])}
fun wifiPermissions():Array<String> = if(Build.VERSION.SDK_INT>=33)arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES)else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
