package com.rowell.shareme

import android.app.*
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.pm.PackageManager
import android.net.*
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.*
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.io.File

class TransferService:Service(){
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main)
    private var transport:Transport?=null
    private var installer:InstallerServer?=null
    private var hotspot:WifiManager.LocalOnlyHotspotReservation?=null
    private var networkCallback:ConnectivityManager.NetworkCallback?=null
    private var joinedNetwork:Network?=null
    private var joinedSsid:String?=null
    private var serverStart:Job?=null
    private lateinit var wake:PowerManager.WakeLock
    @Volatile private var live=true
    private var preparation:Job?=null
    @Volatile private var preparationVersion=0L
    override fun onCreate(){
        super.onCreate()
        val manager=getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("sharing","File transfers",NotificationManager.IMPORTANCE_LOW))
        startForeground(41,notification("Ready to share nearby"),ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        Model.set{it.copy(active=true)}
        wake=(getSystemService(POWER_SERVICE) as PowerManager).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"ShareMe:transfer").apply{setReferenceCounted(false)}
        scope.launch{while(isActive){val state=Model.value;if(state.busy&&!wake.isHeld)wake.acquire(60*60*1000L);if(!state.busy&&wake.isHeld)wake.release();manager.notify(41,notification(state.status));if(!state.busy&&state.pairing!=null&&addresses()!=state.pairing.hosts)startServer(state.pairing.mode);delay(1500)}}
    }
    override fun onBind(intent:Intent?)=null
    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int{
        if(intent==null){stopSelf();return START_NOT_STICKY}
        when(intent.action){
            "stop"->stopSelf()
            "disconnect"->{if(Model.value.connection.phase.active)disconnectCurrent("Disconnected. Scan again to resume an unfinished file.")}
            "cancel_preparation"->{if(Model.value.preparing){preparationVersion++;Model.epochs.next();preparation?.cancel();preparation=null;Model.set{it.copy(preparing=false,busy=false,current="",currentUri="",done=0,total=0,fileIndex=0,fileCount=0,status="Preparation cancelled. Your existing selection is kept.")}}}
            "clear_selection"->{if(!Model.value.busy){resetTransport();Model.set{it.copy(files=emptyList(),status="Selection cleared. Your original files stay on this phone.")}}}
            "remove_file"->{if(!Model.value.busy){val id=intent.getStringExtra("id");resetTransport();Model.set{it.copy(files=it.files.filterNot{file->file.meta.id==id},status="Removed from selection. The original file stays on this phone.")}}}
            "prepare"->{
                if(Model.value.busy)return START_NOT_STICKY
                val existing=Model.value.files
                val uris=intent.getStringArrayListExtra("uris").orEmpty().map(Uri::parse).distinct().filter{uri->existing.none{it.uri==uri}}
                if(uris.isEmpty()){Model.update(status="Those files are already selected.");return START_NOT_STICKY}
                if(existing.size+uris.size>16){Model.update(status="Choose up to 16 files per batch.");return START_NOT_STICKY}
                resetTransport();val version=++preparationVersion;val epoch=Model.epochs.next();Model.set{it.copy(busy=true,preparing=true,status="Preparing files. Nothing sent yet.",mode="send")}
                preparation=scope.launch{try{val files=withContext(Dispatchers.IO){uris.mapIndexed{index,uri->ReceiveStore.prepare(this@TransferService,uri,checkCancelled={ensureActive()},progress={name,done,total->Model.epochs.update(epoch){if(live)Model.set{it.copy(current=name,currentUri=uri.toString(),done=done,total=total.coerceAtLeast(0),fileIndex=index+1,fileCount=uris.size,status="Preparing file ${index+1} of ${uris.size}. Nothing sent yet.")}}})}};if(preparationVersion!=version)return@launch;preparation=null;Model.set{it.copy(files=existing+files,busy=false,preparing=false,current="",currentUri="",done=0,total=0,fileIndex=0,fileCount=0,status="Files ready. Scan the receiver when you're ready to send.")}}
                catch(e:CancellationException){throw e}
                catch(e:Exception){if(preparationVersion==version){preparation=null;Model.set{it.copy(busy=false,preparing=false,status=e.message?:"Could not read these files. Choose them again.")}}}}
            }
            "send"->startServer("send")
            "receive"->startServer("receive")
            "connect"->{if(!Model.value.busy){resetTransport();runCatching{Pairing.parse(intent.getStringExtra("pairing").orEmpty())}.onSuccess(::connect).onFailure{Model.update(status=it.message?:"Invalid pairing code.")}}}
            "hotspot"->createHotspot(intent.getStringExtra("mode")?:"receive")
            "installer"->{scope.launch{try{val hosts=addresses();check(hosts.isNotEmpty()){ "Connect to Wi-Fi or create a hotspot first." };installer?.close();installer=withContext(Dispatchers.IO){InstallerServer(File(applicationInfo.sourceDir))};Model.set{it.copy(appUrl=installer!!.url(hosts.first()),status="Installer link ready. The other phone needs this Wi-Fi/hotspot.")}}catch(e:Exception){Model.update(status=e.message?:"Could not share the installer.")}}}
        }
        return START_NOT_STICKY
    }
    private fun addresses()=LocalRouting.advertised(LocalNetworks.addresses(this,hotspot!=null),hotspot!=null)
    private fun releaseJoinedNetwork(){networkCallback?.let{runCatching{getSystemService(ConnectivityManager::class.java).unregisterNetworkCallback(it)}};networkCallback=null;joinedNetwork=null;joinedSsid=null}
    private fun resetTransport(){serverStart?.cancel();serverStart=null;Model.epochs.next();transport?.close();transport=null;Model.set{it.copy(pairing=null,approval=null,busy=false,current="",currentUri="",done=0,total=0,fileIndex=0,fileCount=0,connection=PeerConnection())}}
    private fun disconnectCurrent(message:String,failed:Boolean=false){val peer=Model.value.connection;resetTransport();releaseJoinedNetwork();Model.set{it.copy(connection=if(failed)peer.advance(ConnectionPhase.FAILED)else peer.disconnect(),status=message)}}
    private fun startServer(mode:String){
        if(Model.value.busy)return
        resetTransport()
        Model.set{it.copy(busy=true,status="Starting sharing…")}
        serverStart=scope.launch{try{val hosts=addresses();check(hosts.isNotEmpty()){ "Connect to Wi-Fi or create a hotspot first." };val worker=Transport(this@TransferService);transport=worker;withContext(Dispatchers.IO){worker.start(mode,if(mode=="send")Model.value.files else emptyList(),hosts)}}
        catch(e:CancellationException){throw e}
        catch(e:Exception){transport?.close();transport=null;Model.set{it.copy(busy=false,pairing=null,status=e.message?:"Could not start sharing.")}}}
    }
    private fun connect(pair:Pairing){
        if(pair.mode=="receive"&&Model.value.files.isEmpty()){Model.update(status="Choose files in Send first, then scan the receiver.");return}
        val worker=Transport(this);transport=worker
        Model.set{it.copy(busy=true,connection=PeerConnection.begin(pair.name),status="Connecting to ${pair.name}…")}
        val connectivity=getSystemService(ConnectivityManager::class.java)
        fun begin(network:Network?,properties:LinkProperties?=null){
            if(!live||transport!==worker)return
            val addresses=if(properties!=null)properties.linkAddresses.mapNotNull{address->
                (address.address as? java.net.Inet4Address)?.hostAddress?.let{LocalAddress(it,address.prefixLength,LocalAddress.Kind.WIFI)}
            }else LocalNetworks.addresses(this,hotspot!=null)
            val hosts=LocalRouting.orderedHosts(pair.hosts,addresses)
            val sameSubnet=addresses.takeIf{it.isNotEmpty()}?.any{local->hosts.any{local.reaches(it)}}
            val localHost=if(network==null)addresses.firstOrNull{it.kind==LocalAddress.Kind.HOTSPOT&&hosts.any(it::reaches)}?.host else null
            worker.connect(pair.copy(hosts=hosts),if(pair.mode=="receive")Model.value.files else emptyList(),network,sameSubnet,localHost)
        }
        val joined=joinedNetwork?.let{network->connectivity.getLinkProperties(network)?.takeIf{LocalNetworks.ready(it)}?.let{network to it}}
        if(!LocalRouting.needsHotspotRequest(pair.ssid,joinedSsid,joined!=null)){
            if(pair.ssid.isNotEmpty()&&joined!=null){begin(joined.first,joined.second);return}
            val localHotspot=LocalNetworks.addresses(this,hotspot!=null).any{local->local.kind==LocalAddress.Kind.HOTSPOT&&pair.hosts.any(local::reaches)}
            val wifi=if(localHotspot)null else wifiForHosts(this,pair.hosts)?:connectivity.allNetworks.firstOrNull{network->connectivity.getNetworkCapabilities(network)?.let{it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)&&!it.hasTransport(NetworkCapabilities.TRANSPORT_VPN)}==true}
            if(wifi==null&&!localHotspot){disconnectCurrent("Join the same Wi-Fi or the receiver's hotspot, then scan a fresh code.",failed=true);return}
            begin(wifi,wifi?.let(connectivity::getLinkProperties))
        }else{
            Model.set{it.copy(busy=true,status="Approve joining ${pair.ssid} in Android's prompt…")}
            try{
                val specifier=WifiNetworkSpecifier.Builder().setSsid(pair.ssid).apply{if(pair.password.isNotEmpty())setWpa2Passphrase(pair.password)}.build()
                val request=NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).setNetworkSpecifier(specifier).build()
                releaseJoinedNetwork()
                val callback=object:ConnectivityManager.NetworkCallback(){
                    private var started=false
                    override fun onAvailable(network:Network){if(live&&transport===worker)Model.update(status="Joined hotspot. Waiting for its local address…")}
                    override fun onLinkPropertiesChanged(network:Network,properties:LinkProperties){
                        if(!live||networkCallback!==this||transport!==worker||started||!LocalNetworks.ready(properties))return
                        joinedNetwork=network;joinedSsid=pair.ssid;started=true;begin(network,properties)
                    }
                    override fun onUnavailable(){if(live&&networkCallback===this&&transport===worker)disconnectCurrent("Could not join the hotspot. Open Wi-Fi settings, join ${pair.ssid}, then scan again.",failed=true)}
                    override fun onLost(network:Network){if(live&&networkCallback===this){joinedNetwork=null;joinedSsid=null;if(transport===worker&&Model.value.connection.phase.active)disconnectCurrent("Hotspot disconnected. Reconnect and scan again to resume.",failed=true)else releaseJoinedNetwork()}}
                }
                networkCallback=callback
                connectivity.requestNetwork(request,callback,Handler(Looper.getMainLooper()),60000)
            }catch(e:Exception){disconnectCurrent("Could not join the hotspot. Allow Nearby Wi-Fi access; on Android 10–12, also turn on Location. You can join in Wi-Fi settings and scan again.",failed=true)}
        }
    }
    @Suppress("DEPRECATION")
    private fun createHotspot(mode:String){
        if(Model.value.busy)return
        if(hotspot!=null){startServer(mode);return}
        val allowed=if(Build.VERSION.SDK_INT>=33)checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES)==PackageManager.PERMISSION_GRANTED else checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
        if(!allowed){Model.update(status="Allow nearby Wi-Fi access before creating a hotspot.");return}
        resetTransport();Model.set{it.copy(busy=true,status="Creating an offline hotspot…")}
        try{applicationContext.getSystemService(WifiManager::class.java).startLocalOnlyHotspot(object:WifiManager.LocalOnlyHotspotCallback(){
            override fun onStarted(reservation:WifiManager.LocalOnlyHotspotReservation){
                if(!live){reservation.close();return}
                hotspot=reservation
                val ssid=if(Build.VERSION.SDK_INT>=30)reservation.softApConfiguration.ssid.orEmpty() else reservation.wifiConfiguration?.SSID.orEmpty().trim('"')
                val password=if(Build.VERSION.SDK_INT>=30)reservation.softApConfiguration.passphrase.orEmpty() else reservation.wifiConfiguration?.preSharedKey.orEmpty().trim('"')
                Model.set{it.copy(ssid=ssid,password=password,status="Hotspot ready. Other devices can join without mobile data.")}
                scope.launch{
                    repeat(40){
                        if(!live||hotspot!==reservation)return@launch
                        if(addresses().isNotEmpty()){Model.set{it.copy(busy=false)};startServer(mode);return@launch}
                        delay(250)
                    }
                    Model.set{it.copy(busy=false,status="Hotspot is on but has no local address yet. Wait a moment, then refresh the code.")}
                }
            }
            override fun onFailed(reason:Int){if(live)Model.set{it.copy(busy=false,status="Android couldn't start a hotspot ($reason). Use your phone's Hotspot settings, then try again.")}}
            override fun onStopped(){hotspot=null;if(live){disconnectCurrent("Hotspot stopped. Reconnect both devices and show a fresh code.",failed=true);Model.set{it.copy(ssid="",password="")}}}
        },Handler(Looper.getMainLooper()))}catch(e:SecurityException){Model.set{it.copy(busy=false,status="Allow nearby Wi-Fi access. On older Android versions, Location must also be enabled.")}}catch(e:Exception){Model.set{it.copy(busy=false,status="Hotspot unavailable. Try your phone's Hotspot settings.")}}
    }
    private fun notification(text:String):Notification{
        val open=PendingIntent.getActivity(this,0,Intent(this,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop=PendingIntent.getService(this,1,Intent(this,TransferService::class.java).setAction("stop"),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this,"sharing").setSmallIcon(com.rowell.shareme.R.drawable.ic_stat_shareme).setContentTitle("Share Mode").setContentText(text).setContentIntent(open).setOngoing(true).addAction(0,"Stop",stop).build()
    }
    override fun onDestroy(){
        live=false;preparationVersion++;Model.epochs.next()
        transport?.close();installer?.close();hotspot?.close();networkCallback?.let{runCatching{getSystemService(ConnectivityManager::class.java).unregisterNetworkCallback(it)}}
        scope.cancel();if(wake.isHeld)wake.release();Model.set{it.copy(mode="",pairing=null,busy=false,preparing=false,approval=null,appUrl="",ssid="",password="",active=false,status="Sharing stopped. Partial files can resume when you scan again.",connection=it.connection.disconnect())};super.onDestroy()
    }
    companion object{
        fun wifiForHosts(context:Context,hosts:List<String>):Network?{
            return LocalNetworks.wifiForHosts(context,hosts)
        }
        fun command(context:Context,action:String,extra:(Intent)->Unit={}){val intent=Intent(context,TransferService::class.java).setAction(action);extra(intent);context.startForegroundService(intent)}
    }
}
