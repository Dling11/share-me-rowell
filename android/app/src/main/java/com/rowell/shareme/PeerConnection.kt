package com.rowell.shareme

enum class ConnectionPhase(val label:String,val active:Boolean=false,val terminal:Boolean=false){
    IDLE("No device connected"),
    CONNECTING("Connecting",true),
    APPROVAL("Waiting for your approval",true),
    WAITING("Waiting for the other device",true),
    SENDING("Connected · sending",true),
    RECEIVING("Connected · receiving",true),
    VERIFYING("Connected · verifying",true),
    FINISHED("Finished · disconnected",terminal=true),
    DECLINED("Declined · disconnected",terminal=true),
    DISCONNECTED("Disconnected",terminal=true),
    FAILED("Connection ended",terminal=true)
}

/** Terminal outcomes survive delayed socket errors/completion callbacks. A new scan starts a new card. */
data class PeerConnection(val name:String="",val phase:ConnectionPhase=ConnectionPhase.IDLE){
    val visible get()=name.isNotBlank()
    fun advance(next:ConnectionPhase)=if(phase.terminal)this else copy(phase=next)
    fun disconnect()=if(phase.active)advance(ConnectionPhase.DISCONNECTED)else this
    companion object{
        fun begin(name:String,phase:ConnectionPhase=ConnectionPhase.CONNECTING)=PeerConnection(
            name.filterNot{it.isISOControl()||it in '\u202a'..'\u202e'||it in '\u2066'..'\u2069'}.trim().take(64).ifEmpty{"Nearby device"},phase)
    }
}
