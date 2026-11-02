package com.rowell.shareme

data class NearbyPeer(val id:String,val name:String,val avatar:AvatarStyle,val host:String,val port:Int,val fingerprint:String) {
    fun pairing()=Pairing(listOf(host),port,fingerprint,"", "receive",name)
}

// Discovery is public metadata, never the QR secret or a Wi-Fi password.
object NearbyProtocol {
    const val SERVICE_TYPE="_sharemode._tcp."
    fun attributes(profile:ShareProfile,pair:Pairing):Map<String,String> = mapOf(
        "v" to "1", "id" to profile.id, "name" to profile.name,
        "avatar" to profile.avatar.legacyEncode(), "fp" to pair.fingerprint
    )+if(profile.avatar.look==0)emptyMap() else mapOf("look" to profile.avatar.look.toString())
    // Keep the five-part avatar for older 1.1.0 peers; they ignore the optional look.
    fun parseAvatar(value:String,look:String?=null):AvatarStyle {
        val avatar=AvatarStyle.parse(value)
        if(look==null)return avatar
        val selected=look.toIntOrNull()
        require(selected!=null && selected in 0..1){"Invalid avatar style."}
        return avatar.copy(look=selected)
    }
    fun parse(host:String,port:Int,attributes:Map<String,String>):NearbyPeer {
        require(attributes["v"]=="1" && privateHost(host) && port in 1024..65535)
        val id=attributes["id"].orEmpty();val name=attributes["name"].orEmpty();val fp=attributes["fp"].orEmpty()
        require(id.matches(Regex("[a-zA-Z0-9-]{8,64}")) && name.isNotBlank() && name.length<=24 && name==profileName(name))
        require(fp.matches(Regex("[a-f0-9]{64}")))
        return NearbyPeer(id,name,parseAvatar(attributes["avatar"].orEmpty(),attributes["look"]),host,port,fp)
    }
    // Compare on both devices: a LAN-discovered name is not an authenticated identity.
    fun transferCode(fingerprint:String,nonce:String):String {
        require(fingerprint.matches(Regex("[a-f0-9]{64}")) && nonce.matches(Regex("[a-f0-9]{32}")))
        val digest=java.security.MessageDigest.getInstance("SHA-256").digest("ShareMode-nearby-v1:$fingerprint:$nonce".toByteArray(Charsets.UTF_8))
        return hex(digest).take(16).uppercase().chunked(4).joinToString(" ")
    }
    fun allowsRequest(nearby:Boolean,visible:Boolean,receiveMode:Boolean,unexpired:Boolean,qrMatches:Boolean):Boolean =
        unexpired && if(nearby)visible && receiveMode else qrMatches
}

object SharingLifetime {
    const val IDLE_TIMEOUT_MS=2*60*1000L
    fun shouldStop(visible:Boolean,busy:Boolean,connectionActive:Boolean,backgroundIdleMillis:Long)=
        !visible && !busy && !connectionActive && backgroundIdleMillis>=IDLE_TIMEOUT_MS
}
