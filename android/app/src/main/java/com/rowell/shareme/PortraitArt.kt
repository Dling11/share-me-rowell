package com.rowell.shareme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import java.util.ArrayDeque
import kotlin.math.max
import kotlin.math.min

/** Stable wire IDs stay readable; retired class artwork is no longer offered or rendered. */
object CharacterRoster {
    val roles=listOf(0,5,6)+(8..23)
    fun current(role:Int)=if(role in roles)role else 0
    fun fixed(role:Int)=role==5||role==6||Adventure.isSpecial(role)
    fun mastery(role:Int)=if(role>=12)Adventure.favouriteMasteries[role-12] else listOf(45,45,45,60,75,85,100,55,80,90,100,95)[role]
}

/** One approved cel at a time, shared by HD and Pixel. No full-atlas/video allocation. */
object PortraitArt {
    private val names=listOf("luffy","zoro","boa","shanks","eren","levi","mikasa","sukuna","itachi","rock_lee","might_guy","madara")
    private val images=object:LruCache<String,Bitmap>(8*1024*1024){override fun sizeOf(key:String,value:Bitmap)=value.allocationByteCount}
    fun asset(role:Int,look:Int=0)=when(val current=CharacterRoster.current(role)){0->if(look==1)"portrait_custom_girl" else "portrait_custom_boy";5->"portrait_seraph";6->"portrait_sovereign";in 8..10->"portrait_special";11->"portrait_yugi";else->"portrait_"+names[current-12]}
    fun frame(role:Int,level:Int,pose:Int,hop:Int=0):Int {
        if(role==0)return 0
        if(role==5||role==6)return (if(level>=CharacterRoster.mastery(role))12 else 0)+(if(hop>0)11 else (pose ushr 12) and 15).coerceAtMost(11)
        val turn=PixelIdle.direction(pose,5)
        val index=if(role>=12)if(hop>0)11 else ((pose ushr 12) and 15).coerceAtMost(11)
            else if(role==11&&hop>0)6 else if(pose and 2!=0)1 else if(turn!=0)if(turn<0)2 else 3
            else if(role==11)when((pose shr 2) and 7){1->4;3->5;5->7;else->0}else 0
        return Adventure.specialRow(role,level)*4+index
    }
    @Synchronized fun cel(context:Context,name:String,index:Int,pixel:Boolean=false):Bitmap {
        val key="$name/$index/$pixel";images.get(key)?.let{return it}
        val source=checkNotNull(context.assets.open("avatars/$name/$index.webp").use{BitmapFactory.decodeStream(it)}){"Missing approved character cel"}
        require(source.width==256&&source.height==256)
        val result=if(pixel)Bitmap.createScaledBitmap(source,128,128,false).also{source.recycle()}else source
        images.put(key,result);return result
    }
    private data class Mask(val pixels:IntArray,val groups:ByteArray,val shades:FloatArray)
    private val masks=object:LruCache<String,Mask>(4){}
    private fun mask(context:Context,style:AvatarStyle,eyes:Int):Mask {
        val key="${style.look}/${style.hair}/$eyes";masks.get(key)?.let{return it}
        val bitmap=cel(context,asset(0,style.look),style.hair*4+eyes);val size=bitmap.width
        val pixels=IntArray(size*size);bitmap.getPixels(pixels,0,size,0,0,size,size)
        val groups=ByteArray(pixels.size);val shades=FloatArray(pixels.size);val possible=BooleanArray(pixels.size);val skin=BooleanArray(pixels.size)
        val skinSeeds=ArrayDeque<Int>();val queue=ArrayDeque<Int>();val hsv=FloatArray(3)
        for(i in pixels.indices){val c=pixels[i];if(c ushr 24<16)continue;AndroidColor.colorToHSV(c,hsv);val h=hsv[0];val value=hsv[2];val low=min(AndroidColor.red(c),min(AndroidColor.green(c),AndroidColor.blue(c)))/255f
            val saturation=if(value==low)0f else (value-low)/(if(value+low>1)2-value-low else value+low)
            val x=i%size*96f/size;val y=i/size*96f/size
            if(y<73&&h in 5f..42f&&saturation>.14f&&value>.32f&&(y<54||x<36||x>60)){skin[i]=true;if(saturation>.25f&&y>=40&&y<53&&x>33&&x<64||saturation>.18f&&y>=55&&y<72&&(x<36||x>60)){groups[i]=2;skinSeeds.add(i)}}
            if(y>=43&&h>=135&&h<195&&saturation>.12f){groups[i]=3;shades[i]=value/.84f}
            else if(y>=48&&y<75&&h in 195f..250f&&saturation>.25f){groups[i]=4;shades[i]=value/.86f}
            else if(style.hair!=5&&y<72&&saturation<.32f&&value>.18f)possible[i]=true
        }
        fun flood(q:ArrayDeque<Int>,allowed:BooleanArray,group:Byte){while(q.isNotEmpty()){val i=q.removeFirst();val x=i%size;val y=i/size;for(n in intArrayOf(if(x>0)i-1 else -1,if(x+1<size)i+1 else -1,if(y>0)i-size else -1,if(y+1<size)i+size else -1))if(n>=0&&allowed[n]&&groups[n].toInt()==0){groups[n]=group;q.add(n)}}}
        flood(skinSeeds,skin,2)
        for(i in pixels.indices){if(groups[i].toInt()==2)shades[i]=max(AndroidColor.red(pixels[i]),max(AndroidColor.green(pixels[i]),AndroidColor.blue(pixels[i])))/255f/.98f
            if(possible[i]&&groups[i].toInt()==0){val x=i%size*96f/size;val y=i/size*96f/size;if(y<26||x<27||x>69){groups[i]=1;queue.add(i)}}}
        flood(queue,possible,1)
        for(i in pixels.indices)if(groups[i].toInt()==1)shades[i]=max(AndroidColor.red(pixels[i]),max(AndroidColor.green(pixels[i]),AndroidColor.blue(pixels[i])))/255f/.86f
        return Mask(pixels,groups,shades).also{masks.put(key,it)}
    }
    @Synchronized fun portrait(context:Context,style:AvatarStyle,level:Int,pose:Int,hop:Int,pixel:Boolean):ImageBitmap {
        val role=CharacterRoster.current(style.role)
        if(role!=0)return cel(context,asset(role,style.look),frame(role,level,pose,hop),pixel).asImageBitmap()
        val eyes=if(pose and 2!=0||hop>0)1 else style.eyes
        val key="custom:${style.look}:${style.hair}:$eyes:${style.skin}:${style.hairColour}:${style.shirt}:${style.element}:$pixel"
        images.get(key)?.let{return it.asImageBitmap()}
        val mask=mask(context,style,eyes);val pixels=mask.pixels.copyOf()
        val palette=intArrayOf(0,hairTones[style.hairColour].toArgb(),skinTones[style.skin].toArgb(),shirtTones[style.shirt].toArgb(),elementTones[style.element].toArgb())
        for(i in pixels.indices){val group=mask.groups[i].toInt();if(group==0)continue;val c=palette[group];val shade=mask.shades[i];pixels[i]=(pixels[i] and -0x1000000) or (min(255,(AndroidColor.red(c)*shade).toInt()) shl 16) or (min(255,(AndroidColor.green(c)*shade).toInt()) shl 8) or min(255,(AndroidColor.blue(c)*shade).toInt())}
        val original=Bitmap.createBitmap(pixels,256,256,Bitmap.Config.ARGB_8888);val result=if(pixel)Bitmap.createScaledBitmap(original,128,128,false).also{original.recycle()}else original
        images.put(key,result);return result.asImageBitmap()
    }
    fun cachedBytes()=images.size()
}
