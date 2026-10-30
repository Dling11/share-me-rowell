package com.rowell.shareme

import android.Manifest
import android.app.DownloadManager
import android.content.ClipData
import android.content.Intent
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat
import java.util.Date

class QrActivity: CaptureActivity()
class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle=SystemBarStyle.dark(Bg.toArgb()), navigationBarStyle=SystemBarStyle.dark(Bg.toArgb()))
        setContent { ShareTheme { AppScreen() } }
    }
    private fun start(action:String, extra:(Intent)->Unit={}) = runCatching {
        TransferService.command(this,action,extra)
    }.onFailure { Model.update(status=it.message?:"Could not start sharing.") }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable private fun AppScreen() {
        val state by Model.state.collectAsState()
        var page by rememberSaveable { mutableStateOf("send") }
        var pendingHotspot by rememberSaveable { mutableStateOf("") }
        var scanned by rememberSaveable { mutableStateOf("") }
        var paste by remember { mutableStateOf(false) }
        var pasted by remember { mutableStateOf("") }
        var stop by remember { mutableStateOf(false) }
        var disconnect by remember { mutableStateOf(false) }
        var clear by remember { mutableStateOf(false) }
        var appTools by remember { mutableStateOf(false) }
        var menu by remember { mutableStateOf(false) }
        var options by rememberSaveable(page) { mutableStateOf(false) }
        var detail by remember { mutableStateOf<Prepared?>(null) }
        var about by remember { mutableStateOf(false) }
        val permissions=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            val wifi=if(Build.VERSION.SDK_INT>=33)Manifest.permission.NEARBY_WIFI_DEVICES else Manifest.permission.ACCESS_FINE_LOCATION
            val pairing=scanned; val mode=pendingHotspot; scanned=""; pendingHotspot=""
            if(granted[wifi]==true) {
                if(pairing.isNotEmpty())start("connect"){it.putExtra("pairing",pairing)}
                else if(mode.isNotEmpty())start("hotspot"){it.putExtra("mode",mode)}
            } else Model.update(status="Allow Nearby Wi-Fi access for hotspot pairing. On Android 10–12, allow Location and turn it on in Settings.")
        }
        fun connect(text:String) {
            runCatching { Pairing.parse(text) }.onSuccess { pair ->
                if(pair.mode=="receive"&&state.files.isEmpty()) { page="send"; Model.update(status="Choose files first, then scan the receiver."); return@onSuccess }
                page=if(pair.mode=="receive")"send" else "receive"
                if(pair.ssid.isNotEmpty()) { scanned=text; permissions.launch(wifiPermissions()) }
                else start("connect"){it.putExtra("pairing",text)}
            }.onFailure { Model.update(status=it.message?:"Scan a Share Mode pairing code.") }
        }
        val scan=rememberLauncherForActivityResult(ScanContract()) { result -> result.contents?.let(::connect) }
        val notify=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
        fun begin(action:String) { if(Build.VERSION.SDK_INT>=33)notify.launch(Manifest.permission.POST_NOTIFICATIONS); start(action) }
        val pick=rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if(uris.isNotEmpty()) {
                if((state.files.map{it.uri}+uris).distinct().size>16)Model.update(status="Choose up to 16 files per batch.")
                else {
                    uris.forEach { runCatching { contentResolver.takePersistableUriPermission(it,Intent.FLAG_GRANT_READ_URI_PERMISSION) } }
                    if(Build.VERSION.SDK_INT>=33)notify.launch(Manifest.permission.POST_NOTIFICATIONS)
                    start("prepare"){it.putStringArrayListExtra("uris",ArrayList(uris.map(Uri::toString)))}
                }
            }
        }
        fun scanCode() { scan.launch(ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE).setPrompt("Scan the code on the other device").setBeepEnabled(false).setOrientationLocked(false).setCaptureActivity(QrActivity::class.java)) }
        fun hotspot() { pendingHotspot=page; permissions.launch(wifiPermissions()) }

        Scaffold(containerColor=Bg, topBar={
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal=20.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                Image(painterResource(R.drawable.ic_brand),null,Modifier.size(38.dp))
                Column(Modifier.weight(1f)) { Text("Share Mode",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold); Text("Nearby. No internet needed.",color=Muted,style=MaterialTheme.typography.labelSmall) }
                Box {
                    IconButton(onClick={menu=true}) { Icon(Icons.Rounded.MoreVert,"More options") }
                    DropdownMenu(expanded=menu,onDismissRequest={menu=false}) {
                        DropdownMenuItem(text={Text("Share the app")},onClick={menu=false;appTools=true})
                        DropdownMenuItem(text={Text("Wi-Fi settings")},onClick={menu=false;startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))})
                        DropdownMenuItem(text={Text("Paste pairing link")},enabled=!state.busy,onClick={menu=false;paste=true})
                        if(state.active)DropdownMenuItem(text={Text("Stop sharing & hotspot")},onClick={menu=false;stop=true})
                        DropdownMenuItem(text={Text("Remove unfinished downloads")},enabled=!state.busy,onClick={menu=false;clear=true})
                        DropdownMenuItem(text={Text("About Share Mode")},onClick={menu=false;about=true})
                    }
                }
            }
        },bottomBar={
            Column {
                if(page=="send"&&state.files.isNotEmpty()&&!state.busy)Surface(color=Panel) {
                    Column(Modifier.fillMaxWidth().padding(16.dp,10.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                        Button(onClick={scanCode()},modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)) { Icon(Icons.Rounded.QrCodeScanner,null,Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Scan receiver to send") }
                        Text("Other phone: open Receive. PC: open Receive files.",color=Muted,style=MaterialTheme.typography.bodySmall,modifier=Modifier.align(Alignment.CenterHorizontally))
                    }
                }
                NavigationBar(containerColor=Bg,tonalElevation=0.dp) {
                    listOf(Triple("send","Send",Icons.AutoMirrored.Rounded.Send),Triple("receive","Receive",Icons.Rounded.Download),Triple("history","Recent",Icons.Rounded.History)).forEach { (id,label,icon) ->
                        NavigationBarItem(selected=page==id,onClick={page=id;if(id=="receive"&&!state.busy&&state.pairing?.mode!="receive")begin("receive")},icon={Icon(icon,null)},label={Text(label)},colors=NavigationBarItemDefaults.colors(selectedIconColor=Mint,selectedTextColor=Mint,indicatorColor=Mint.copy(alpha=.12f),unselectedIconColor=Muted,unselectedTextColor=Muted))
                    }
                }
            }
        }) { insets ->
            Column(Modifier.fillMaxSize().padding(insets)) {
                // Keep the active file and progress above the scrolling list on every tab.
                if(state.busy)TransferBanner(state,onCancel={if(state.preparing)start("cancel_preparation") else if(state.connection.phase.active)disconnect=true else stop=true})
                key(page) {
                    LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(20.dp,14.dp,20.dp,24.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
                        if(state.connection.phase.terminal)item {
                            CardBox {
                                Text(if(state.connection.phase==ConnectionPhase.FINISHED)"Transfer complete" else state.connection.phase.label,fontWeight=FontWeight.Bold,color=if(state.connection.phase==ConnectionPhase.FINISHED)Mint else MaterialTheme.colorScheme.onSurface)
                                Text(state.status,color=Muted,style=MaterialTheme.typography.bodyMedium)
                                if(state.connection.phase==ConnectionPhase.FINISHED)TextButton(onClick={page="history"}){Text("View recent files")}
                                else TextButton(onClick={scanCode()}){Text("Scan a fresh code")}
                            }
                        } else if(!state.busy&&state.status!="Ready to share"&&state.pairing==null&&(!state.status.startsWith("Files ready")||page!="send"))item {
                            Text(state.status,color=Muted,style=MaterialTheme.typography.bodyMedium)
                        }
                        when(page) {
                            "send" -> {
                                item {
                                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) { Text("Send files",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold); Text(if(state.files.isEmpty())"Choose one file or a whole batch." else "${state.files.size} selected · ${size(state.files.sumOf{it.meta.size})}",color=Muted,style=MaterialTheme.typography.bodyMedium) }
                                        if(state.files.isNotEmpty())IconButton(onClick={pick.launch(arrayOf("*/*"))},enabled=!state.busy){Icon(Icons.Rounded.Add,"Add more files",tint=Mint)}
                                    }
                                }
                                if(state.files.isEmpty())item {
                                    CardBox {
                                        Icon(Icons.Rounded.FolderOpen,null,Modifier.size(36.dp),tint=Mint)
                                        Text("What would you like to send?",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
                                        Text("Photos, videos, apps and any other files. Select up to 16 at once.",color=Muted)
                                        Button(onClick={pick.launch(arrayOf("*/*"))},enabled=!state.busy,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)){Icon(Icons.Rounded.Add,null);Spacer(Modifier.width(8.dp));Text("Choose files")}
                                    }
                                } else {
                                    item {
                                        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                                            Text("Selected files",fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f))
                                            TextButton(onClick={start("clear_selection")},enabled=!state.busy){Text("Clear all")}
                                        }
                                    }
                                    items(state.files,key={it.meta.id}) { file ->
                                        FileRow(file.meta.name,file.meta.size,file.uri,onOpen={detail=file},onRemove=if(state.busy)null else ({start("remove_file"){it.putExtra("id",file.meta.id)}}))
                                    }
                                    item { OutlinedButton(onClick={pick.launch(arrayOf("*/*"))},enabled=!state.busy,modifier=Modifier.fillMaxWidth()) { Icon(Icons.Rounded.Add,null);Spacer(Modifier.width(8.dp));Text("Add more files") } }
                                    item { Text("Tap a file to preview its details. Nothing sends until you scan and the receiver approves.",color=Muted,style=MaterialTheme.typography.bodySmall) }
                                }
                                item { TextButton(onClick={options=!options},enabled=!state.busy,modifier=Modifier.fillMaxWidth()){Text(if(options)"Hide connection options" else "More connection options");Icon(if(options)Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,null)} }
                                if(options)item {
                                    CardBox {
                                        Text("Let the other phone scan",fontWeight=FontWeight.SemiBold)
                                        Text("Useful when the receiving phone will scan your code.",color=Muted,style=MaterialTheme.typography.bodySmall)
                                        OutlinedButton(onClick={begin("send")},enabled=state.files.isNotEmpty()&&!state.busy,modifier=Modifier.fillMaxWidth()){Text("Show sender code")}
                                        if(state.pairing?.mode=="send")QrCard(state.pairing!!.text(),"Other phone: Receive → Scan sender code.")
                                        HotspotControls(state,enabled=state.files.isNotEmpty()&&!state.busy,onStart={hotspot()})
                                        if(state.files.isNotEmpty())TextButton(onClick={shareUris(state.files.map{it.uri})},enabled=!state.busy){Text("Android sharing · Bluetooth optional")}
                                    }
                                }
                            }
                            "receive" -> {
                                item { Text("Receive files",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("On the sending phone, choose files and scan your code.",color=Muted) }
                                if(!state.busy) {
                                    item { HotspotControls(state,enabled=true,onStart={hotspot()}) }
                                    item {
                                        CardBox {
                                            if(state.pairing?.mode=="receive") {
                                                QrCard(state.pairing!!.text(),"Scan from Send on the other phone.")
                                                TextButton(onClick={begin("receive")},modifier=Modifier.fillMaxWidth()){Icon(Icons.Rounded.Refresh,null,Modifier.size(18.dp));Spacer(Modifier.width(6.dp));Text("Refresh code")}
                                            } else {
                                                Text("Ready when you are",fontWeight=FontWeight.Bold)
                                                Text("Connect to the same Wi-Fi, or create a hotspot above.",color=Muted)
                                                Button(onClick={begin("receive")},modifier=Modifier.fillMaxWidth()){Text("Show my receiver code")}
                                            }
                                        }
                                    }
                                    item { OutlinedButton(onClick={scanCode()},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Icon(Icons.Rounded.QrCodeScanner,null,Modifier.size(20.dp));Spacer(Modifier.width(8.dp));Text("Scan sender code · phone or PC")} }
                                    item { Text("For PC → phone: open Send files on Windows, then scan its code here. Received files go to Downloads/SHARE-ME-ROWELL.",color=Muted,style=MaterialTheme.typography.bodySmall) }
                                    item { TextButton(onClick={runCatching{startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))}}){Text("Open Downloads")} }
                                }
                            }
                            "history" -> {
                                item { Text("Recent files",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold) }
                                if(state.history.isEmpty())item { Text("Completed transfers appear here.",color=Muted) }
                                items(state.history) { item ->
                                    CardBox {
                                        FileRow(item.name,item.size,Uri.parse(item.uri),onOpen={open(item)})
                                        Text("${item.direction} · ${DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(Date(item.time))}",color=Muted,style=MaterialTheme.typography.bodySmall)
                                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                                            TextButton(onClick={open(item)}){Text(if(item.name.endsWith(".apk",true)&&item.direction=="Received")"Install APK" else "Open")}
                                            TextButton(onClick={shareUris(listOf(Uri.parse(item.uri)))}){Text("Share")}
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        state.approval?.let { request ->
            AlertDialog(onDismissRequest={request.answer.complete(false)},title={Text(if(request.sending)"Send these files?" else "Receive these files?")},text={
                Column(Modifier.heightIn(max=360.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Text(if(request.sending)"To ${request.name}" else "From ${request.name}",fontWeight=FontWeight.Bold)
                    Text("${request.files.size} files · ${size(request.files.sumOf{it.size})}",color=Mint)
                    request.files.forEach { Text("${it.name}\n${size(it.size)}",style=MaterialTheme.typography.bodyMedium) }
                }
            },confirmButton={TextButton(onClick={request.answer.complete(true)}){Text(if(request.sending)"Send" else "Receive")}},dismissButton={TextButton(onClick={request.answer.complete(false)}){Text("Decline")}})
        }
        detail?.let { file -> AlertDialog(onDismissRequest={detail=null},title={Text("File details")},text={Column(Modifier.heightIn(max=360.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)){FileThumb(file.uri,file.meta.name,Modifier.size(160.dp).align(Alignment.CenterHorizontally));SelectionContainer{Text(file.meta.name)};Text("${size(file.meta.size)} · ${fileType(file.meta.name)}",color=Muted)}},confirmButton={TextButton(onClick={open(History("Selected",file.meta.name,file.meta.size,file.uri.toString(),0))}){Text("Open")}},dismissButton={TextButton(onClick={detail=null}){Text("Done")}}) }
        if(appTools)ModalBottomSheet(onDismissRequest={appTools=false},containerColor=Bg) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
                Text("Share the app",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
                Text("Help another phone install Share Mode.",color=Muted)
                Button(onClick={shareInstaller()},enabled=!state.busy,modifier=Modifier.fillMaxWidth()){Text("Share installer via Android")}
                Text("Choose Bluetooth in Android's sharing screen if needed. This is optional.",color=Muted,style=MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick={begin("installer")},enabled=!state.busy,modifier=Modifier.fillMaxWidth()){Text("Show local download QR")}
                if(state.appUrl.isNotEmpty())QrCard(state.appUrl,"Use the other phone's camera after it joins the same Wi-Fi/hotspot.")
                Spacer(Modifier.height(20.dp))
            }
        }
        if(paste)AlertDialog(onDismissRequest={paste=false},title={Text("Pairing link")},text={OutlinedTextField(pasted,{pasted=it.take(8192)},label={Text("Paste pairing link")})},confirmButton={TextButton(onClick={paste=false;connect(pasted)}){Text("Connect")}},dismissButton={TextButton(onClick={paste=false}){Text("Cancel")}})
        if(stop)AlertDialog(onDismissRequest={stop=false},title={Text("Stop sharing?")},text={Text("This also turns off a hotspot created here. Completed files stay saved; unfinished files can resume later.")},confirmButton={TextButton(onClick={stop=false;start("stop")}){Text("Stop")}},dismissButton={TextButton(onClick={stop=false}){Text("Keep sharing")}})
        if(disconnect)AlertDialog(onDismissRequest={disconnect=false},title={Text("Cancel this transfer?")},text={Text("Completed files stay saved. Scan again to resume unfinished files. Your hotspot stays on.")},confirmButton={TextButton(onClick={disconnect=false;start("disconnect")}){Text("Cancel transfer")}},dismissButton={TextButton(onClick={disconnect=false}){Text("Keep sharing")}})
        if(clear)AlertDialog(onDismissRequest={clear=false},title={Text("Remove unfinished downloads?")},text={Text("Frees storage used by interrupted transfers. Completed files stay in Downloads.")},confirmButton={TextButton(onClick={clear=false;if(!Model.value.busy)lifecycleScope.launch{withContext(Dispatchers.IO){ReceiveStore(this@MainActivity).clearIncomplete()};Model.update(status="Unfinished downloads removed.")}}){Text("Remove")}},dismissButton={TextButton(onClick={clear=false}){Text("Cancel")}})
        if(about)AlertDialog(onDismissRequest={about=false},title={Text("Share Mode")},text={Text("Version ${BuildConfig.VERSION_NAME} · By Rowell\n\nLocal file sharing for Android and Windows. Up to 16 files per batch. No account needed.")},confirmButton={TextButton(onClick={about=false}){Text("Done")}})
    }
    private fun shareInstaller() { lifecycleScope.launch { try {
        val uri=withContext(Dispatchers.IO){val destination=File(cacheDir,"installer/Share-Mode.apk");destination.parentFile!!.mkdirs();File(applicationInfo.sourceDir).copyTo(destination,true);FileProvider.getUriForFile(this@MainActivity,"$packageName.files",destination)}
        shareUris(listOf(uri))
    } catch(_:Exception){toast("Could not prepare the installer.")} } }
    private fun shareUris(uris:List<Uri>) { if(uris.isEmpty())return; try {
        val intent=Intent(if(uris.size==1)Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE).apply {
            type="*/*";if(uris.size==1)putExtra(Intent.EXTRA_STREAM,uris[0])else putParcelableArrayListExtra(Intent.EXTRA_STREAM,ArrayList(uris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData=ClipData.newRawUri("Shared files",uris[0]).also{clip->uris.drop(1).forEach{clip.addItem(ClipData.Item(it))}}
        }
        startActivity(Intent.createChooser(intent,"Share files"))
    } catch(_:Exception){toast("Android could not open its sharing screen.")} }
    private fun open(item:History) {
        val apk=item.name.endsWith(".apk",true)
        if(apk&&!packageManager.canRequestPackageInstalls()){toast("Allow installation from Share Mode, then tap Install APK again.");startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:$packageName")));return}
        try { startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(item.uri),if(apk)"application/vnd.android.package-archive" else contentResolver.getType(Uri.parse(item.uri))?.takeUnless{it=="application/octet-stream"}?:mimeType(item.name)).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)) }
        catch(_:Exception){toast("No app could open this file. Received files are in Downloads/SHARE-ME-ROWELL.")}
    }
    private fun toast(message:String)=Toast.makeText(this,message,Toast.LENGTH_LONG).show()
}
fun wifiPermissions():Array<String> = if(Build.VERSION.SDK_INT>=33)arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES)else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
