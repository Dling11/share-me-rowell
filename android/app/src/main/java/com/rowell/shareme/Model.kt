package com.rowell.shareme

import android.app.Application
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CompletableFuture

data class Approval(val name:String,val files:List<FileMeta>,val sending:Boolean,val answer:CompletableFuture<Boolean> = CompletableFuture(),val deviceCode:String="",val avatar:AvatarStyle?=null,val level:Int=1)
data class ScreenState(val status:String="Ready to share",val mode:String="",val pairing:Pairing?=null,val files:List<Prepared> = emptyList(),val busy:Boolean=false,val current:String="",val currentUri:String="",val done:Long=0,val total:Long=0,val approval:Approval?=null,val history:List<History> = emptyList(),val appUrl:String="",val ssid:String="",val password:String="",val active:Boolean=false,val connection:PeerConnection=PeerConnection(),val preparing:Boolean=false,val fileIndex:Int=0,val fileCount:Int=0,val profile:ShareProfile=ShareProfile(),val uiVisible:Boolean=false,val uiPage:String="send",val nearbyVisible:Boolean=false,val nearbyPeers:List<NearbyPeer> = emptyList(),val nearbyStatus:String="Open Receive on the other phone. Use the same Wi-Fi or hotspot.",val verificationCode:String="",val resumedFrom:Long=0,val journey:AdventurerJourney=AdventurerJourney(),val earnedXp:Int=0,val avatarAnimations:Boolean=true,val peerAppearance:PeerAppearance?=null)
object Model {
    val epochs=SessionEpoch()
    private val mutable=MutableStateFlow(ScreenState())
    val state=mutable.asStateFlow()
    val value get()=mutable.value
    val deviceName get()=value.profile.name
    @Synchronized fun set(transform:(ScreenState)->ScreenState){mutable.value=transform(mutable.value)}
    fun update(status:String?=null,history:List<History>?=null){set{it.copy(status=status?:it.status,history=history?:it.history)}}
}
class ShareApp:Application(){override fun onCreate(){super.onCreate();Model.set{it.copy(profile=ProfileStore.load(this),history=ReceiveStore(this).history(),journey=JourneyStore.load(this),avatarAnimations=ProfileStore.animations(this))}}}
