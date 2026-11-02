package com.rowell.shareme

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import java.io.Closeable

/** Platform DNS-SD only. Never scans address ranges or advertises pairing secrets. */
@Suppress("DEPRECATION")
class NearbyDiscovery(context:Context):Closeable {
    private val manager=context.getSystemService(NsdManager::class.java)
    private val main=Handler(Looper.getMainLooper())
    private var closed=false
    private var registration:NsdManager.RegistrationListener?=null
    private var advertisedKey=""
    private var advertisingReady=false
    private var discovery:NsdManager.DiscoveryListener?=null
    private var generation=0
    private val found=linkedMapOf<String,NsdServiceInfo>()
    private val peers=linkedMapOf<String,Pair<NearbyPeer,Long>>()
    private val resolving=ArrayDeque<NsdServiceInfo>()
    private var resolvingNow=false
    private var retryAfter=0L

    fun update(state:ScreenState) {
        if(closed)return
        val pair=state.pairing
        val visible=state.uiVisible && state.uiPage=="receive" && !state.busy && pair?.mode=="receive"
        val key=if(visible)"${pair!!.code}:${state.profile.name}:${state.profile.avatar.encode()}" else ""
        if(key!=advertisedKey){stopAdvertising();if(visible)advertise(pair!!,state.profile,key)}
        val actuallyVisible=visible&&advertisingReady
        Model.set{if(it.nearbyVisible==actuallyVisible)it else it.copy(nearbyVisible=actuallyVisible)}
        val browse=state.uiVisible && state.uiPage=="send" && state.files.isNotEmpty() && !state.busy
        if(browse && discovery==null && SystemClock.elapsedRealtime()>=retryAfter)startBrowsing()
        if(!browse && discovery!=null)stopBrowsing()
        val expired=peers.filterValues{SystemClock.elapsedRealtime()-it.second>45000}.keys.toList()
        if(expired.isNotEmpty()){
            expired.forEach{key->peers.remove(key);found[key]?.let{if(resolving.none{item->item.serviceName==key})resolving.addLast(it)}}
            publish();resolveNext(generation)
        }
    }
    private fun advertise(pair:Pairing,profile:ShareProfile,key:String) {
        val info=NsdServiceInfo().apply {
            serviceName="ShareMode-${profile.id.take(8)}";serviceType=NearbyProtocol.SERVICE_TYPE;port=pair.port
            NearbyProtocol.attributes(profile,pair).forEach{(name,value)->setAttribute(name,value)}
        }
        val listener=object:NsdManager.RegistrationListener {
            override fun onServiceRegistered(info:NsdServiceInfo){main.post{if(registration===this){advertisingReady=true;Model.set{it.copy(nearbyVisible=it.uiVisible&&it.uiPage=="receive"&&!it.busy)}}}}
            override fun onServiceUnregistered(info:NsdServiceInfo){}
            override fun onUnregistrationFailed(info:NsdServiceInfo,error:Int){}
            override fun onRegistrationFailed(info:NsdServiceInfo,error:Int){main.post{if(registration===this){advertisingReady=false;registration=null;Model.set{it.copy(nearbyVisible=false,nearbyStatus="Nearby visibility unavailable. Use the QR code.")}}}}
        }
        advertisedKey=key;registration=listener
        runCatching{manager.registerService(info,NsdManager.PROTOCOL_DNS_SD,listener)}.onFailure{registration=null;Model.set{it.copy(nearbyVisible=false,nearbyStatus="Nearby visibility unavailable. Use the QR code.")}}
    }
    private fun stopAdvertising(){registration?.let{runCatching{manager.unregisterService(it)}};registration=null;advertisingReady=false;advertisedKey=""}
    private fun startBrowsing() {
        val token=++generation
        val listener=object:NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(type:String){}
            override fun onDiscoveryStopped(type:String){}
            override fun onStartDiscoveryFailed(type:String,error:Int){main.post{if(discovery===this){retryAfter=SystemClock.elapsedRealtime()+30000;stopBrowsing();Model.set{it.copy(nearbyStatus="Nearby search unavailable. Scan a QR code instead.")}}}}
            override fun onStopDiscoveryFailed(type:String,error:Int){}
            override fun onServiceFound(info:NsdServiceInfo){main.post {
                if(closed || generation!=token || discovery!==this || found.size>=32 || !info.serviceType.startsWith("_sharemode._tcp"))return@post
                found[info.serviceName]=info
                if(resolving.none{it.serviceName==info.serviceName}){resolving.addLast(info);resolveNext(token)}
            }}
            override fun onServiceLost(info:NsdServiceInfo){main.post{if(generation==token){found.remove(info.serviceName);peers.remove(info.serviceName);publish()}}}
        }
        discovery=listener;Model.set{it.copy(nearbyStatus="Looking for receivers on this Wi-Fi or hotspot…")}
        runCatching{manager.discoverServices(NearbyProtocol.SERVICE_TYPE,NsdManager.PROTOCOL_DNS_SD,listener)}.onFailure{retryAfter=SystemClock.elapsedRealtime()+30000;stopBrowsing();Model.set{it.copy(nearbyStatus="Nearby search unavailable. Scan a QR code instead.")}}
    }
    private fun resolveNext(token:Int) {
        if(closed || generation!=token || resolvingNow || resolving.isEmpty())return
        val info=resolving.removeFirst();resolvingNow=true
        runCatching{manager.resolveService(info,object:NsdManager.ResolveListener {
            override fun onResolveFailed(service:NsdServiceInfo,error:Int){main.post{if(generation==token){resolvingNow=false;resolveNext(token)}}}
            override fun onServiceResolved(service:NsdServiceInfo){main.post {
                if(closed || generation!=token)return@post
                resolvingNow=false
                if(found.containsKey(info.serviceName))runCatching {
                    val attributes=service.attributes.mapValues{(_,bytes)->require(bytes.size<=128);bytes.toString(Charsets.UTF_8)}
                    NearbyProtocol.parse(service.host?.hostAddress.orEmpty(),service.port,attributes)
                }.getOrNull()?.takeIf{it.id!=Model.value.profile.id}?.let{peers[info.serviceName]=it to SystemClock.elapsedRealtime();publish()}
                resolveNext(token)
            }}
        })}.onFailure{resolvingNow=false;resolveNext(token)}
    }
    private fun publish(){Model.set{it.copy(nearbyPeers=peers.values.map{entry->entry.first}.distinctBy{peer->peer.id}.sortedBy{peer->peer.name.lowercase()}.take(16))}}
    private fun stopBrowsing(){generation++;discovery?.let{runCatching{manager.stopServiceDiscovery(it)}};discovery=null;found.clear();peers.clear();resolving.clear();resolvingNow=false;publish()}
    override fun close(){closed=true;stopAdvertising();stopBrowsing();Model.set{it.copy(nearbyVisible=false)}}
}
