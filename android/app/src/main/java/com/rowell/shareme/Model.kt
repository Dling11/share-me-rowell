package com.rowell.shareme

import android.app.Application
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CompletableFuture

data class Approval(val name:String,val files:List<FileMeta>,val sending:Boolean,val answer:CompletableFuture<Boolean> = CompletableFuture())
data class ScreenState(val status:String="Ready to share",val mode:String="",val pairing:Pairing?=null,val files:List<Prepared> = emptyList(),val busy:Boolean=false,val current:String="",val done:Long=0,val total:Long=0,val approval:Approval?=null,val history:List<History> = emptyList(),val appUrl:String="",val ssid:String="",val password:String="",val active:Boolean=false,val connection:PeerConnection=PeerConnection(),val preparing:Boolean=false,val fileIndex:Int=0,val fileCount:Int=0)
object Model {
    val epochs=SessionEpoch()
    private val mutable=MutableStateFlow(ScreenState())
    val state=mutable.asStateFlow()
    val value get()=mutable.value
    var deviceName="${Build.MANUFACTURER} ${Build.MODEL}".take(40)
    @Synchronized fun set(transform:(ScreenState)->ScreenState){mutable.value=transform(mutable.value)}
    fun update(status:String?=null,history:List<History>?=null){set{it.copy(status=status?:it.status,history=history?:it.history)}}
}
class ShareApp:Application(){override fun onCreate(){super.onCreate();Model.update(history=ReceiveStore(this).history())}}
