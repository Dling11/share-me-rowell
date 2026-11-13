package com.rowell.shareme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/** Finished character cels. No personal hair, skin, clothing, expression or palette is applied. */
object SpecialSprites {
    private val atlases=arrayOfNulls<Bitmap>(14)
    private val cache=object:LruCache<Int,List<ImageBitmap>>(8){}
    @Synchronized fun frames(context:Context,role:Int,level:Int):List<ImageBitmap> {
        val row=Adventure.specialRow(role,level)
        val key=role*10+row
        cache.get(key)?.let{return it}
        val index=Adventure.specialSheet(role)
        val resource=listOf(R.drawable.pixel_special,R.drawable.pixel_yugi,R.drawable.pixel_luffy,R.drawable.pixel_zoro,R.drawable.pixel_boa,R.drawable.pixel_shanks,R.drawable.pixel_eren,R.drawable.pixel_levi,R.drawable.pixel_mikasa,R.drawable.pixel_sukuna,R.drawable.pixel_itachi,R.drawable.pixel_rock_lee,R.drawable.pixel_might_guy,R.drawable.pixel_madara)[index]
        val source=atlases[index]?:checkNotNull(BitmapFactory.decodeResource(context.resources,resource,BitmapFactory.Options().apply{inScaled=false})).also{
            require(it.width==256&&it.height==if(index==1)256 else 384){"Invalid fixed-character atlas"};atlases[index]=it
        }
        return (0 until Adventure.specialCels(role)).map{cell->Bitmap.createBitmap(source,cell%4*64,(row+cell/4)*64,64,64).asImageBitmap()}.also{cache.put(key,it)}
    }
    fun poseIndex(pose:Int,role:Int=8,hop:Int=0):Int {
        if(role>=12)return if(hop>0)11 else (pose ushr 12) and 15
        if(role>=11&&hop>0)return 6
        if(pose and 2!=0)return 1
        val turn=PixelIdle.direction(pose,5)
        if(turn!=0)return if(turn<0)2 else 3
        return if(role>=11)when((pose shr 2) and 7){1->4;3->5;5->7;else->0}else 0
    }
}
