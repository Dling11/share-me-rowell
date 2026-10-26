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
    private lateinit var wake:PowerManager.WakeLock
    @Volatile private var live=true
    override fun onCreate(){
        super.onCreate()
        val manager=getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("sharing","File transfers",NotificationManager.IMPORTANCE_LOW))
        startForeground(41,notification("Ready to share nearby"),ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        Model.set{it.copy(active=true)}
        wake=(getSystemService(POWER_SERVICE) as PowerManager).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"ShareMe:transfer").apply{setReferenceCounted(false)}
        scope.launch{while(isActive){val state=Model.value;if(state.busy&&!wake.isHeld)wake.acquire(60*60*1000L);if(!state.busy&&wake.isHeld)wake.release();manager.notify(41,notification(state.status));delay(1500)}}
    }
    override fun onBind(intent:Intent?)=null
    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int{
        if(intent==null){stopSelf();return START_NOT_STICKY}
        when(intent.action){
            "stop"->stopSelf()
            "prepare"->{
                if(Model.value.busy)return START_NOT_STICKY
                val uris=intent.getStringArrayListExtra("uris").orEmpty().map(Uri::parse)
                require(uris.size in 1..16)
                resetTransport();Model.set{it.copy(busy=true,status="Preparing files…",mode="send")}
                scope.launch{try{val files=withContext(Dispatchers.IO){uris.map{ReceiveStore.prepare(this@TransferService,it){ensureActive()}}};Model.set{it.copy(files=files,busy=false,status="Files ready.")};if(Transport.addresses().any{it!="127.0.0.1"})startServer("send") else Model.update(status="Files ready. Connect to Wi-Fi, create a hotspot or use Android sharing.")}
                catch(e:CancellationException){throw e}
                catch(e:Exception){Model.set{it.copy(busy=false,status=e.message?:"Could not prepare those files.")}}}
            }
            "send"->startServer("send")
            "receive"->startServer("receive")
            "connect"->{if(!Model.value.busy){resetTransport();runCatching{Pairing.parse(intent.getStringExtra("pairing").orEmpty())}.onSuccess(::connect).onFailure{Model.update(status=it.message?:"Invalid pairing code.")}}}
            "hotspot"->createHotspot(intent.getStringExtra("mode")?:"receive")
            "installer"->{scope.launch{try{val hosts=Transport.addresses();check(hosts.first()!="127.0.0.1"){"Connect to Wi-Fi or create a hotspot first."};installer?.close();installer=withContext(Dispatchers.IO){InstallerServer(File(applicationInfo.sourceDir))};Model.set{it.copy(appUrl=installer!!.url(hosts.first()),status="Installer link ready. The other phone needs this Wi-Fi/hotspot.")}}catch(e:Exception){Model.update(status=e.message?:"Could not share the installer.")}}}
        }
        return START_NOT_STICKY
    }
    private fun resetTransport(){Model.epochs.next();transport?.close();transport=null;Model.set{it.copy(pairing=null,approval=null,busy=false)}}
    private fun startServer(mode:String){
        if(Model.value.busy)return
        resetTransport()
        Model.set{it.copy(busy=true,status="Starting sharing…")}
        scope.launch{try{check(Transport.addresses().first()!="127.0.0.1"){"Connect to Wi-Fi or create a hotspot first."};val worker=Transport(this@TransferService);transport=worker;withContext(Dispatchers.IO){worker.start(mode,if(mode=="send")Model.value.files else emptyList())}}
        catch(e:CancellationException){throw e}
        catch(e:Exception){resetTransport();Model.update(status=e.message?:"Could not start sharing.")}}
    }
    private fun connect(pair:Pairing){
        if(pair.mode=="receive"&&Model.value.files.isEmpty()){Model.update(status="Choose files in Send first, then scan the receiver.");return}
        val worker=Transport(this);transport=worker
        val connectivity=getSystemService(ConnectivityManager::class.java)
        fun begin(network:Network?){if(live&&transport===worker)worker.connect(pair,if(pair.mode=="receive")Model.value.files else emptyList(),network)}
        val existing=wifiForHosts(this,pair.hosts)
        if(pair.ssid.isEmpty()||existing!=null){
            val wifi=existing?:connectivity.allNetworks.firstOrNull{connectivity.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)==true}
            begin(wifi)
        }else{
            Model.set{it.copy(busy=true,status="Approve joining ${pair.ssid} in Android's prompt…")}
            val specifier=WifiNetworkSpecifier.Builder().setSsid(pair.ssid).apply{if(pair.password.isNotEmpty())setWpa2Passphrase(pair.password)}.build()
            val request=NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).setNetworkSpecifier(specifier).build()
            networkCallback?.let{runCatching{connectivity.unregisterNetworkCallback(it)}}
            val callback=object:ConnectivityManager.NetworkCallback(){
                override fun onAvailable(network:Network){begin(network)}
                override fun onUnavailable(){if(live&&transport===worker)Model.set{it.copy(busy=false,status="Could not join the hotspot. Connect manually and scan again.")}}
                override fun onLost(network:Network){if(live&&transport===worker)Model.update(status="Hotspot disconnected. Reconnect and scan again to resume.")}
            }
            networkCallback=callback
            try{connectivity.requestNetwork(request,callback,30000)}catch(e:Exception){Model.set{it.copy(busy=false,status="Allow nearby Wi-Fi access, then scan again.")}}
        }
    }
    @Suppress("DEPRECATION")
    private fun createHotspot(mode:String){
        if(Model.value.busy)return
        if(hotspot!=null){startServer(mode);return}
        Model.update(status="Creating an offline hotspot…")
        val allowed=if(Build.VERSION.SDK_INT>=33)checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES)==PackageManager.PERMISSION_GRANTED else checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
        if(!allowed){Model.update(status="Allow nearby Wi-Fi access before creating a hotspot.");return}
        try{applicationContext.getSystemService(WifiManager::class.java).startLocalOnlyHotspot(object:WifiManager.LocalOnlyHotspotCallback(){
            override fun onStarted(reservation:WifiManager.LocalOnlyHotspotReservation){
                if(!live){reservation.close();return}
                hotspot=reservation
                val ssid=if(Build.VERSION.SDK_INT>=30)reservation.softApConfiguration.ssid.orEmpty() else reservation.wifiConfiguration?.SSID.orEmpty().trim('"')
                val password=if(Build.VERSION.SDK_INT>=30)reservation.softApConfiguration.passphrase.orEmpty() else reservation.wifiConfiguration?.preSharedKey.orEmpty().trim('"')
                Model.set{it.copy(ssid=ssid,password=password,status="Hotspot ready. Other devices can join without mobile data.")}
                scope.launch{delay(1000);startServer(mode)}
            }
            override fun onFailed(reason:Int){if(live)Model.update(status="Android couldn't start a hotspot ($reason). Use your phone's Hotspot settings, then try again.")}
            override fun onStopped(){hotspot=null;if(live)Model.set{it.copy(ssid="",password="")}}
        },Handler(Looper.getMainLooper()))}catch(e:SecurityException){Model.update(status="Allow nearby Wi-Fi access. On older Android versions, Location must also be enabled.")}catch(e:Exception){Model.update(status="Hotspot unavailable. Try your phone's Hotspot settings.")}
    }
    private fun notification(text:String):Notification{
        val open=PendingIntent.getActivity(this,0,Intent(this,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop=PendingIntent.getService(this,1,Intent(this,TransferService::class.java).setAction("stop"),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this,"sharing").setSmallIcon(com.rowell.shareme.R.drawable.ic_stat_shareme).setContentTitle("Share-Me-Rowell").setContentText(text).setContentIntent(open).setOngoing(true).addAction(0,"Stop",stop).build()
    }
    override fun onDestroy(){
        live=false;Model.epochs.next()
        transport?.close();installer?.close();hotspot?.close();networkCallback?.let{runCatching{getSystemService(ConnectivityManager::class.java).unregisterNetworkCallback(it)}}
        scope.cancel();if(wake.isHeld)wake.release();Model.set{it.copy(mode="",pairing=null,busy=false,approval=null,appUrl="",ssid="",password="",active=false,status="Sharing stopped. Partial files can resume when you scan again.")};super.onDestroy()
    }
    companion object{
        fun wifiForHosts(context:Context,hosts:List<String>):Network?{
            val manager=context.getSystemService(ConnectivityManager::class.java)
            return manager.allNetworks.firstOrNull{network->manager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)==true&&manager.getLinkProperties(network)?.routes.orEmpty().any{route->!route.isDefaultRoute&&hosts.any{host->runCatching{route.destination.contains(java.net.InetAddress.getByName(host))}.getOrDefault(false)}}}
        }
        fun command(context:Context,action:String,extra:(Intent)->Unit={}){val intent=Intent(context,TransferService::class.java).setAction(action);extra(intent);context.startForegroundService(intent)}
    }
}
