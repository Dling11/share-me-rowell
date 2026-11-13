package com.rowell.shareme

data class NearbyPeer(val id:String,val name:String,val avatar:AvatarStyle,val host:String,val port:Int,val fingerprint:String,val level:Int=1) {
    fun pairing()=Pairing(listOf(host),port,fingerprint,"", "receive",name)
}

// Discovery is public metadata, never the QR secret or a Wi-Fi password.
object NearbyProtocol {
    const val SERVICE_TYPE="_sharemode._tcp."
    fun attributes(profile:ShareProfile,pair:Pairing,level:Int=1):Map<String,String> = mapOf(
        "v" to "1", "id" to profile.id, "name" to profile.name,
        "avatar" to profile.avatar.legacyEncode(), "fp" to pair.fingerprint
    ) + cosmetics(profile.avatar,level)
    // Discovery and nearby hello must use the same bounded optional fields.
    fun cosmetics(avatar:AvatarStyle,level:Int=1):Map<String,String> = buildMap {
        if(avatar.look!=0)put("look",avatar.look.toString())
        if(avatar.role in 1..6)put("role",avatar.role.toString())
        if(avatar.role in 7..10)put("hero",avatar.role.toString())
        if(avatar.role==11)put("guest","11")
        if(avatar.role>=12)put("collect",avatar.role.toString())
        if(avatar.element!=0)put("element",avatar.element.toString())
        if(avatar.hairColour>4)put("hairTone",avatar.hairColour.toString())
        if(avatar.eyes==3)put("mood","3")
        if(level>1)put("level",level.coerceIn(1,100).toString())
    }
    // Five-part base stays readable by old peers; optional cosmetics are untrusted display metadata.
    fun parseAvatar(value:String,look:String?=null,role:String?=null,element:String?=null,hairTone:String?=null,hero:String?=null,mood:String?=null,guest:String?=null,collect:String?=null):AvatarStyle {
        val avatar=AvatarStyle.parse(value)
        fun part(value:String?,fallback:Int,range:IntRange):Int {if(value==null)return fallback;val v=value.toIntOrNull();require(v!=null&&v in range){"Invalid avatar style."};return v}
        val oldRole=part(role,avatar.role,0..6)
        val knownRole=if(hero==null)oldRole else part(hero,oldRole,7..10)
        val guestRole=part(guest,knownRole,11..11)
        return avatar.copy(look=part(look,avatar.look,0..1),role=part(collect,guestRole,12..23),eyes=part(mood,avatar.eyes,3..3),element=part(element,avatar.element,Adventure.elements.indices),hairColour=part(hairTone,avatar.hairColour,0..5))
    }
    fun parseLevel(value:String?):Int {if(value==null)return 1;val level=value.toIntOrNull();require(level!=null&&level in 1..100){"Invalid level."};return level}
    fun parse(host:String,port:Int,attributes:Map<String,String>):NearbyPeer {
        require(attributes["v"]=="1" && privateHost(host) && port in 1024..65535)
        val id=attributes["id"].orEmpty();val name=attributes["name"].orEmpty();val fp=attributes["fp"].orEmpty()
        require(id.matches(Regex("[a-zA-Z0-9-]{8,64}")) && name.isNotBlank() && name.length<=24 && name==profileName(name))
        require(fp.matches(Regex("[a-f0-9]{64}")))
        return NearbyPeer(id,name,parseAvatar(attributes["avatar"].orEmpty(),attributes["look"],attributes["role"],attributes["element"],attributes["hairTone"],attributes["hero"],attributes["mood"],attributes["guest"],attributes["collect"]),host,port,fp,parseLevel(attributes["level"]))
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
