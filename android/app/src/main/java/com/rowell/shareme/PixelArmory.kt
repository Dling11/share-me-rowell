package com.rowell.shareme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColour
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb

/** One generated 4x4 equipment atlas, sampled to 64px cells. Heads remain fully customizable. */
object PixelArmory {
    const val CELL=64
    private val atlases=arrayOfNulls<Bitmap>(3)
    private val cache=object:LruCache<Int,ImageBitmap>(24){}
    @Synchronized fun sprite(context:Context,style:AvatarStyle,level:Int=1):ImageBitmap {
        val gear=Adventure.gear(style.role,level)
        require(gear.source in 0..2){"Use complete signature cels for this character"}
        val slot=gear.pair*2+style.look
        val key=((((gear.source*16+slot)*5+style.shirt)*6+style.element)*5+style.skin)
        cache.get(key)?.let{return it}
        val source=atlases[gear.source]?:run{
            val resource=when(gear.source){0->R.drawable.pixel_armory;1->R.drawable.pixel_progression;else->R.drawable.pixel_anime}
            val decoded=checkNotNull(BitmapFactory.decodeResource(context.resources,resource,BitmapFactory.Options().apply{inScaled=false}))
            require(decoded.width==decoded.height && decoded.width>=CELL*4){"Invalid equipment atlas"}
            Bitmap.createScaledBitmap(decoded,CELL*4,CELL*4,false).also{atlases[gear.source]=it;if(decoded!==it)decoded.recycle()}
        }
        val pixels=IntArray(CELL*CELL)
        source.getPixels(pixels,0,CELL,(slot%4)*CELL,(slot/4)*CELL,CELL,CELL)
        val shirt=FloatArray(3);val accent=FloatArray(3);val skin=FloatArray(3);val hsv=FloatArray(3)
        AndroidColour.colorToHSV(shirtTones[style.shirt].toArgb(),shirt)
        AndroidColour.colorToHSV(elementTones[style.element].toArgb(),accent)
        AndroidColour.colorToHSV(skinTones[style.skin].toArgb(),skin)
        // Find the central boot baseline; tall side weapons do not affect registration.
        val baseline=(CELL-1 downTo 45).firstOrNull{y->(23..41).any{x->AndroidColour.alpha(pixels[y*CELL+x])>=128}}?:61
        val shift=(61-baseline).coerceIn(-4,4)
        val output=IntArray(CELL*CELL)
        for(y in 0 until CELL)for(x in 0 until CELL){
            val targetY=y+shift;if(targetY !in 0 until CELL)continue
            val colour=pixels[y*CELL+x]
            // Hard alpha and a head socket prevent fringes and keep every hair choice independent.
            if(AndroidColour.alpha(colour)<128 || (x in 18..46 && targetY<35))continue
            AndroidColour.colorToHSV(colour,hsv)
            val target=when{
                gear.source==2 && hsv[0] in 0f..40f && hsv[1]>.12f && hsv[1]<.42f && hsv[2]>.65f->skin
                hsv[1]>.28f && hsv[0] in 260f..335f->shirt
                hsv[1]>.32f && hsv[0] in 165f..205f->accent
                else->null
            }
            output[targetY*CELL+x]=if(target==null)colour or (0xff shl 24) else{
                val value=(hsv[2]*target[2]/if(target===skin).96f else .78f).coerceIn(.05f,1f)
                val saturation=(hsv[1]*target[1]/if(target===skin).24f else .65f).coerceIn(if(target===skin).03f else .15f,1f)
                AndroidColour.HSVToColor(floatArrayOf(target[0],saturation,value))
            }
        }
        return Bitmap.createBitmap(output,CELL,CELL,Bitmap.Config.ARGB_8888).asImageBitmap().also{cache.put(key,it)}
    }
}

/** A discrete pose keeps draw invalidations below the display refresh rate. */
object PixelIdle {
    fun pose(phase:Float):Int {
        val p=phase.coerceIn(0f,.999999f)
        val c=(p*80).toInt()
        val breath=if(c in 8..21||c in 48..61)1 else 0
        val blink=if(c in 20..21||c==24||c in 72..73)2 else 0
        val sway=if(c in 30..45)1 else if(c in 62..75)2 else 0
        val turn=if(c in 34..42)1 else if(c in 68..74)2 else 0
        val hair=if(c in 9..12||c in 49..52)1 else 0
        return breath or blink or ((c*6/80) shl 2) or (sway shl 5) or (turn shl 7) or (hair shl 9) or ((if(c in 36..42)1 else 0) shl 10) or ((if(c in 68..74)1 else 0) shl 11) or (soloFrame(c) shl 12)
    }
    fun direction(pose:Int,shift:Int)=when((pose shr shift) and 3){1->-1;2->1;else->0}
    private fun soloFrame(c:Int)=when(c){in 8..13->1;in 14..19->2;in 20..21,in 72..73->3;in 29..31->4;in 32..37->5;in 38..40,in 44..46->6;in 41..43->7;in 54..57->8;in 58..63->9;in 64..68->10;else->0}
}
