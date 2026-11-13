package com.rowell.shareme

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.util.ReflectionHelpers
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],qualifiers="w480dp-h400dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PixelMotionTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun fixedCharacterRenderingIgnoresPersonalCustomization(){
        var alternate by mutableStateOf(false)
        compose.mainClock.autoAdvance=false
        compose.activity.setContent{ShareTheme{Surface(color=Bg){Column(Modifier.fillMaxSize()){
            Adventure.roles.indices.filter{Adventure.isSpecial(it)}.chunked(4).forEach{roles->Row{for(role in roles)ChibiAvatar(if(alternate)AvatarStyle(5,4,4,2,1,1,role,5)else AvatarStyle(role=role),Modifier.size(96.dp),level=100)}}
        }}}}
        compose.mainClock.advanceTimeBy(32);compose.waitForIdle();val first=hash(capture())
        compose.runOnIdle{alternate=true};compose.mainClock.advanceTimeBy(32);compose.waitForIdle()
        assertEquals(first,hash(capture()))
    }
    @Test fun lockedNinjaPreviewKeepsAUniformShadowAcrossSkinChoices(){
        var skin by mutableIntStateOf(0)
        compose.mainClock.autoAdvance=false
        compose.activity.setContent{ShareTheme{Surface(color=Bg){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){
            ChibiAvatar(Adventure.signature(AvatarStyle(skin=skin),10),Modifier.size(200.dp),level=1,gearLevel=100,locked=true)
        }}}}
        compose.mainClock.advanceTimeBy(32);compose.waitForIdle();val first=hash(capture())
        compose.runOnIdle{skin=4};compose.mainClock.advanceTimeBy(32);compose.waitForIdle()
        assertEquals(first,hash(capture()))
    }
    @Test fun idleChangesPixelsAndFreezesWhenAnimationIsDisabled(){
        var moving by mutableStateOf(true)
        compose.mainClock.autoAdvance=false
        compose.activity.setContent{ShareTheme{Surface(color=Bg){Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
            Text("Share Mode · Novice to God-tier",style=MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                for((role,level)in listOf(0 to 1,6 to 100))Column(horizontalAlignment=Alignment.CenterHorizontally){
                    ChibiAvatar(AvatarStyle(hair=1,hairColour=5,role=role),Modifier.size(200.dp),level=level,animated=moving,interactive=true)
                    Text(if(level==1)"Lv. 1 · Novice" else "Lv. 100 · Ascended",color=Muted)
                }
            }
            Text("Progression preview · actual app renderer",color=Muted,style=MaterialTheme.typography.bodySmall)
        }}}}
        compose.mainClock.advanceTimeBy(32);compose.waitForIdle()
        val folder=File(checkNotNull(System.getProperty("sharemode.screenshots"))).apply{mkdirs()}
        val frames=File(folder,"pixel-idle-frames").apply{mkdirs()}
        val hashes=mutableSetOf<Int>()
        repeat(80){frame->
            compose.mainClock.advanceTimeBy(120);compose.waitForIdle()
            val bitmap=capture();val colours=IntArray(bitmap.width*bitmap.height)
            bitmap.getPixels(colours,0,bitmap.width,0,0,bitmap.width,bitmap.height);hashes.add(colours.contentHashCode())
            File(frames,"idle-%03d.png".format(frame)).outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
            bitmap.recycle()
        }
        assertTrue("Pixel idle must visibly animate",hashes.size>=3)
        compose.runOnIdle{moving=false};compose.mainClock.advanceTimeBy(64);compose.waitForIdle()
        val paused=hash(capture());compose.mainClock.advanceTimeBy(1600);compose.waitForIdle()
        assertEquals("Disabled animation must remain still",paused,hash(capture()))
    }
    private fun hash(bitmap:Bitmap):Int {val values=IntArray(bitmap.width*bitmap.height);bitmap.getPixels(values,0,bitmap.width,0,0,bitmap.width,bitmap.height);bitmap.recycle();return values.contentHashCode()}
    private fun capture():Bitmap {
        var result:Bitmap?=null
        compose.runOnIdle{
            val global=ReflectionHelpers.callStaticMethod<Any>(Class.forName("android.view.WindowManagerGlobal"),"getInstance")
            val views=ReflectionHelpers.getField<List<View>>(global,"mViews");val view=views.last{it.width>0&&it.height>0}
            result=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888).also{view.draw(Canvas(it))}
        }
        return checkNotNull(result)
    }
}
