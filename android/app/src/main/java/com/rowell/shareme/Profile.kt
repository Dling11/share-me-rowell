package com.rowell.shareme

import android.content.Context
import java.security.SecureRandom
import java.util.UUID

data class AvatarStyle(val hair:Int=0,val skin:Int=0,val hairColour:Int=0,val shirt:Int=0,val eyes:Int=0) {
    fun encode()="$hair.$skin.$hairColour.$shirt.$eyes"
    companion object {
        fun parse(value:String):AvatarStyle {
            val values=value.split('.').map{it.toIntOrNull()}
            require(values.size==5 && values.all{it!=null}) { "Invalid avatar." }
            val v=values.filterNotNull()
            require(v[0] in 0..5 && v[1] in 0..4 && v[2] in 0..4 && v[3] in 0..4 && v[4] in 0..2) { "Invalid avatar." }
            return AvatarStyle(v[0],v[1],v[2],v[3],v[4])
        }
        fun random():AvatarStyle=SecureRandom().let{AvatarStyle(it.nextInt(6),it.nextInt(5),it.nextInt(5),it.nextInt(5),it.nextInt(3))}
    }
}

fun profileName(raw:String):String=raw.filterNot{it.isISOControl() || it in '\u202a'..'\u202e' || it in '\u2066'..'\u2069'}.trim().take(24).ifBlank{"Happy Sprout"}
data class ShareProfile(val id:String="",val name:String="Happy Sprout",val avatar:AvatarStyle=AvatarStyle())

object ProfileStore {
    fun load(context:Context):ShareProfile {
        val preferences=context.getSharedPreferences("nearby-profile",Context.MODE_PRIVATE)
        val id=preferences.getString("id",null)?:UUID.randomUUID().toString().also{preferences.edit().putString("id",it).apply()}
        val style=preferences.getString("avatar",null)?.let{runCatching{AvatarStyle.parse(it)}.getOrNull()}?:AvatarStyle.random().also{preferences.edit().putString("avatar",it.encode()).apply()}
        val name=preferences.getString("name",null)?:SecureRandom().let{random->
            "${listOf("Happy","Cozy","Sunny","Tiny","Peachy","Sleepy")[random.nextInt(6)]} ${listOf("Sprout","Mochi","Bean","Panda","Bun","Cloud")[random.nextInt(6)]} ${random.nextInt(90)+10}"
        }.also{preferences.edit().putString("name",it).apply()}
        return ShareProfile(id,profileName(name),style)
    }
    fun save(context:Context,profile:ShareProfile) {
        val clean=profile.copy(name=profileName(profile.name))
        context.getSharedPreferences("nearby-profile",Context.MODE_PRIVATE).edit().putString("name",clean.name).putString("avatar",clean.avatar.encode()).apply()
        Model.set{it.copy(profile=clean)}
    }
}
