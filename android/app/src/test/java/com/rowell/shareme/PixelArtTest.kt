package com.rowell.shareme

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PixelArtTest {
    @Test fun yugiHasEightCompleteCelsAndUsesCardAndCheerPoses(){
        val base=SpecialSprites.frames(context,11,50);val master=SpecialSprites.frames(context,11,95)
        assertEquals(8,base.size);assertEquals(8,master.size)
        assertTrue(base.map{pixels(it).contentHashCode()}.toSet().size>=6)
        assertTrue(master.map{pixels(it).contentHashCode()}.toSet().size>=6)
        assertNotEquals(pixels(base[0]).contentHashCode(),pixels(master[0]).contentHashCode())
        val poses=(0 until 80).map{SpecialSprites.poseIndex(PixelIdle.pose(it/80f),11)}.toSet()+SpecialSprites.poseIndex(0,11,3)
        assertEquals((0..7).toSet(),poses)
    }
    @Test fun fixedCharactersHaveTheirOwnCompleteCelsAndStableMasteryRows(){
        val identities=mutableSetOf<Int>()
        for(role in 8..10){
            val start=SpecialSprites.frames(context,role,Adventure.roles[role].level)
            assertEquals(4,start.size);assertTrue(start.all{it.width==64&&it.height==64})
            assertTrue(start.map{pixels(it).contentHashCode()}.toSet().size>=3)
            identities.add(pixels(start[0]).contentHashCode())
            assertNotEquals(pixels(start[0]).contentHashCode(),pixels(SpecialSprites.frames(context,role,100)[0]).contentHashCode())
            assertSame(start,SpecialSprites.frames(context,role,Adventure.roles[role].level))
        }
        assertEquals(3,identities.size)
        assertEquals(listOf(0,2,4),(8..10).map{Adventure.specialRow(it,Adventure.roles[it].level)})
        assertEquals(listOf(1,3,5),(8..10).map{Adventure.specialRow(it,100)})
    }
    @Test fun focusedIdleHasAuthoredBlinkGlancesAndBoundedIndependentMotion(){
        val expressions=listOf(3,5,6,7).map{pixels(PixelHeads.face(context,it,0,2)).contentHashCode()}
        assertEquals(4,expressions.toSet().size)
        val poses=(0 until 80).map{PixelIdle.pose(it/80f)}
        assertTrue(poses.any{PixelIdle.direction(it,5)==-1})
        assertTrue(poses.any{PixelIdle.direction(it,5)==1})
        assertTrue(poses.any{PixelIdle.direction(it,7)==-1})
        assertTrue(poses.any{PixelIdle.direction(it,7)==1})
        assertTrue(poses.any{(it shr 9) and 1!=0})
        assertTrue(poses.any{(it shr 10) and 1!=0})
        assertTrue(poses.any{(it shr 11) and 1!=0})
        assertTrue(poses.distinct().size>=12)
    }
    private val context get()=RuntimeEnvironment.getApplication()
    private fun pixels(image:ImageBitmap)=IntArray(image.width*image.height).also{image.readPixels(it)}
    @Test fun packagedAtlasIsSmallTransparentAndContainsEveryClass(){
        val atlas=BitmapFactory.decodeResource(context.resources,R.drawable.pixel_armory,BitmapFactory.Options().apply{inScaled=false})
        assertEquals(256,atlas.width);assertEquals(256,atlas.height)
        val identities=mutableSetOf<Int>()
        for(role in Adventure.roles.indices.filterNot{Adventure.isSpecial(it)})for(look in 0..1){
            val image=PixelArmory.sprite(context,AvatarStyle(role=role,look=look),Adventure.roles[role].level);val colours=pixels(image)
            assertEquals(64,image.width);assertEquals(64,image.height)
            assertTrue("Missing equipment for $role/$look",colours.count{it ushr 24==255}>140)
            assertTrue(colours.all{it ushr 24==0||it ushr 24==255})
            identities.add(colours.contentHashCode())
        }
        assertTrue("Classes must have distinct equipment",identities.size>=8)
    }
    @Test fun everyEquipmentLayerLeavesTheCustomizableHeadSocketClear(){
        for(role in Adventure.roles.indices.filterNot{Adventure.isSpecial(it)})for(look in 0..1)for(level in listOf(1,Adventure.roles[role].level,45,60,75,85,95,100)){val colours=pixels(PixelArmory.sprite(context,AvatarStyle(role=role,look=look),level))
            for(y in 0..34)for(x in 18..46)assertEquals("Head covered in $role/$look at $x,$y",0,colours[y*64+x] ushr 24)
        }
    }
    @Test fun outfitAndElementRecolourIndependentlyAndReuseCachedArt(){
        val style=AvatarStyle(role=3,look=1)
        val first=PixelArmory.sprite(context,style,60)
        assertSame(first,PixelArmory.sprite(context,style,60))
        assertFalse(pixels(first).contentEquals(pixels(PixelArmory.sprite(context,style.copy(shirt=2),60))))
        assertFalse(pixels(first).contentEquals(pixels(PixelArmory.sprite(context,style.copy(element=4),60))))
    }
    @Test fun idleUsesDiscretePosesAndHasABriefBlink(){
        assertEquals(PixelIdle.pose(0f),PixelIdle.pose(.05f))
        assertEquals(PixelIdle.pose(0f),PixelIdle.pose(-1f))
        assertTrue(PixelIdle.pose(.91f) and 2!=0);assertEquals(0,PixelIdle.pose(.94f) and 2)
        var previous=PixelIdle.pose(0f);var changes=0
        for(frame in 1..299){val current=PixelIdle.pose(frame/300f);if(previous!=current)changes++;previous=current}
        assertTrue("Idle should not redraw at display refresh rate ($changes changes)",changes in 20..45)
    }
    @Test fun authoredFacesAndHairKeepExpressionSkinAndColourIndependent(){
        val faces=(0..3).map{PixelHeads.face(context,it,0,4)}
        assertEquals(4,faces.map{pixels(it).contentHashCode()}.toSet().size)
        assertTrue(faces.all{it.width==28&&it.height==28})
        assertFalse(pixels(faces[0]).contentEquals(pixels(PixelHeads.face(context,0,4,4))))
        val style=AvatarStyle(hair=0,hairColour=5)
        val silver=PixelHeads.hair(context,style)!!
        assertFalse(pixels(silver.image).contentEquals(pixels(PixelHeads.hair(context,style.copy(hairColour=0))!!.image)))
        assertNull(PixelHeads.hair(context,style.copy(hair=5)))
        assertEquals(5,(0..4).map{pixels(PixelHeads.hair(context,style.copy(hair=it))!!.image).contentHashCode()}.toSet().size)
    }
}
