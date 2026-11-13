package com.rowell.shareme

import org.json.JSONObject

/** Optional cosmetics, never transfer authorization or earned progress. */
data class PeerAppearance(val avatar:AvatarStyle,val level:Int=1) {
    companion object {
        fun parse(message:JSONObject):PeerAppearance? {
            val encoded=message.opt("appearance") as? String ?: return null
            if(encoded.length>80 || encoded.split('.').size!=8)return null
            val avatar=runCatching{AvatarStyle.parse(encoded)}.getOrNull()?:return null
            val level=message.optInt("level",1).takeIf{it in 1..100}?:1
            return PeerAppearance(avatar,level)
        }
    }
}
fun JSONObject.withAppearance(avatar:AvatarStyle=Model.value.profile.avatar,level:Int=Model.value.journey.level):JSONObject=
    put("appearance",avatar.encode()).put("level",level.coerceIn(1,100))
