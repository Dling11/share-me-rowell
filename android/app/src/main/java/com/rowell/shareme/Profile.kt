package com.rowell.shareme

import android.content.Context
import java.security.SecureRandom
import java.util.UUID

data class AvatarStyle(val hair:Int=0,val skin:Int=0,val hairColour:Int=0,val shirt:Int=0,val eyes:Int=0,val look:Int=0,val role:Int=0,val element:Int=0) {
    fun legacyEncode()="$hair.$skin.${if(hairColour<=4)hairColour else 2}.$shirt.${if(eyes<=2)eyes else 0}"
    fun encode()="$hair.$skin.$hairColour.$shirt.$eyes.$look.$role.$element"
    companion object {
        fun parse(value:String):AvatarStyle {
            val values=value.split('.').map{it.toIntOrNull()}
            require((values.size in 5..6 || values.size==8) && values.all{it!=null}) { "Invalid avatar." }
            val v=values.filterNotNull()
            val look=v.getOrElse(5){0}
            val role=v.getOrElse(6){0};val element=v.getOrElse(7){0}
            require(v[0] in 0..5 && v[1] in 0..4 && v[2] in 0..5 && v[3] in 0..4 && v[4] in 0..3 && look in 0..1 && role in Adventure.roles.indices && element in Adventure.elements.indices) { "Invalid avatar." }
            return AvatarStyle(v[0],v[1],v[2],v[3],v[4],look,role,element)
        }
        fun random(look:Int?=null):AvatarStyle=SecureRandom().let{
            val selected=look?:it.nextInt(2)
            require(selected in 0..1)
            AvatarStyle(it.nextInt(6),it.nextInt(5),it.nextInt(6),it.nextInt(5),it.nextInt(4),selected)
        }
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
        val role=profile.avatar.role.takeIf{Adventure.canEquip(it,Model.value.journey.level)}?:0
        val personal=if(Adventure.isSpecial(role))load(context).avatar.copy(role=role)else profile.avatar.copy(role=role)
        val clean=profile.copy(name=profileName(profile.name),avatar=personal)
        context.getSharedPreferences("nearby-profile",Context.MODE_PRIVATE).edit().putString("name",clean.name).putString("avatar",clean.avatar.encode()).apply()
        // The listener secret and socket stay valid; only the name displayed in its QR changes.
        Model.set{it.copy(profile=clean,pairing=it.pairing?.copy(name=clean.name))}
    }
    fun animations(context:Context)=context.getSharedPreferences("nearby-profile",Context.MODE_PRIVATE).getBoolean("animations",true)
    fun animations(context:Context,enabled:Boolean){context.getSharedPreferences("nearby-profile",Context.MODE_PRIVATE).edit().putBoolean("animations",enabled).apply();Model.set{it.copy(avatarAnimations=enabled)}}
}
