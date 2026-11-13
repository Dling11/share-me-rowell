package com.rowell.shareme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColour
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntOffset

data class PixelHair(val image:ImageBitmap,val offset:IntOffset)

/** Authored pixel faces and fringes share a registration, with independent skin/eye/hair palettes. */
object PixelHeads {
    private const val CELL=48
    private var atlas:Bitmap?=null
    private var focusAtlas:Bitmap?=null
    private val cache=object:LruCache<Int,ImageBitmap>(24){}
    @Synchronized fun face(context:Context,expression:Int,skin:Int,element:Int):ImageBitmap {
        val key=expression*100+skin*10+element
        return cache.get(key)?:layer(context,expression,28,28,skin=skin,element=element).also{cache.put(key,it)}
    }
    @Synchronized fun hair(context:Context,style:AvatarStyle):PixelHair? {
        if(style.hair==5)return null
        val sizes=listOf(34 to 23,36 to 27,38 to 28,36 to 38,40 to 35)
        val (width,height)=sizes[style.hair];val top=if(style.hair in 1..2)0 else if(style.hair==4)1 else 3
        val key=1000+style.hair*10+style.hairColour
        val image=cache.get(key)?:layer(context,4+style.hair,width,height,hair=style.hairColour).also{cache.put(key,it)}
        return PixelHair(image,IntOffset(32-width/2,top))
    }
    private fun layer(context:Context,slot:Int,width:Int,height:Int,skin:Int?=null,element:Int=0,hair:Int?=null):ImageBitmap {
        val focused=hair==null&&(slot==3||slot in 5..7)
        val actualSlot=if(focused)when(slot){3->0;else->slot-4}else if(hair==null&&slot==4)3 else slot
        val source=if(focused)focusAtlas?:checkNotNull(BitmapFactory.decodeResource(context.resources,R.drawable.pixel_focus,BitmapFactory.Options().apply{inScaled=false})).also{require(it.width==96&&it.height==96);focusAtlas=it}else atlas?:checkNotNull(BitmapFactory.decodeResource(context.resources,R.drawable.pixel_heads,BitmapFactory.Options().apply{inScaled=false})).also{
            require(it.width==CELL*4&&it.height==CELL*4){"Invalid pixel head atlas"};atlas=it
        }
        val columns=source.width/CELL
        val pixels=IntArray(CELL*CELL);source.getPixels(pixels,0,CELL,actualSlot%columns*CELL,actualSlot/columns*CELL,CELL,CELL)
        val hsv=FloatArray(3);val skinTone=FloatArray(3);val eyeTone=FloatArray(3);val hairTone=FloatArray(3)
        AndroidColour.colorToHSV(skinTones[skin?:0].toArgb(),skinTone)
        AndroidColour.colorToHSV(elementTones[element].toArgb(),eyeTone)
        AndroidColour.colorToHSV(hairTones[hair?:0].toArgb(),hairTone)
        var left=CELL;var top=CELL;var right=0;var bottom=0
        for(y in 0 until CELL)for(x in 0 until CELL){val i=y*CELL+x;val colour=pixels[i]
            if(AndroidColour.alpha(colour)<128){pixels[i]=0;continue}
            left=minOf(left,x);right=maxOf(right,x);top=minOf(top,y);bottom=maxOf(bottom,y)
            AndroidColour.colorToHSV(colour,hsv)
            val target=when{
                hair!=null&&hsv[2]>.15f&&hsv[1]>.12f&&hsv[0] in 240f..335f->hairTone
                skin!=null&&hsv[2]>.25f&&hsv[1]>.12f&&hsv[0] in 0f..45f->skinTone
                skin!=null&&hsv[1]>.35f&&hsv[0] in 165f..210f->eyeTone
                else->null
            }
            pixels[i]=if(target==null)colour or (0xff shl 24)else{
                val sourceValue=if(hair!=null).7f else if(target===skinTone).96f else .9f
                val sourceSaturation=if(target===skinTone).24f else .65f
                AndroidColour.HSVToColor(floatArrayOf(target[0],(hsv[1]*target[1]/sourceSaturation).coerceIn(.03f,1f),(hsv[2]*target[2]/sourceValue).coerceIn(.05f,1f)))
            }
        }
        check(left<=right&&top<=bottom){"Missing pixel head layer $slot"}
        val cell=Bitmap.createBitmap(pixels,CELL,CELL,Bitmap.Config.ARGB_8888)
        val cropped=Bitmap.createBitmap(cell,left,top,right-left+1,bottom-top+1)
        val scaled=Bitmap.createScaledBitmap(cropped,width,height,false)
        if(cell!==cropped&&cell!==scaled)cell.recycle();if(cropped!==scaled)cropped.recycle()
        return scaled.asImageBitmap()
    }
}
