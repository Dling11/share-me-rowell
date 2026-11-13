package com.rowell.shareme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import androidx.compose.ui.graphics.ImageBitmap
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PortraitArtTest {
    private val context:Context get()=RuntimeEnvironment.getApplication()
    private fun pixels(image:ImageBitmap)=IntArray(image.width*image.height).also{image.readPixels(it)}
    @Test fun curatedCollectionRetiresClassesWithoutChangingWireIdsOrUnlockCosts(){
        assertEquals(19,CharacterRoster.roles.size);assertFalse(CharacterRoster.roles.any{it in listOf(1,2,3,4,7)})
        assertTrue(CharacterRoster.roles.containsAll(listOf(0,5,6,8,11,12,13,19,23)))
        assertEquals(82,CharacterRoster.mastery(13));assertEquals(96,Adventure.roles[23].level)
        assertEquals(3_302_937L,Adventure.threshold(100));assertEquals(20,Adventure.nextRole(1)!!.level)
        assertEquals(7,AvatarStyle.parse(AvatarStyle(role=7).encode()).role)
    }
    @Test fun legacyEquippedClassesMigrateToTheNewPersonalPortraitWithoutResettingSavedIdentityOrProgress(){
        val prefs=context.getSharedPreferences("nearby-profile",Context.MODE_PRIVATE)
        val old=AvatarStyle(3,4,5,2,3,1,7,5)
        prefs.edit().putString("id","existing-device").putString("name","Keep my name").putString("avatar",old.encode()).putBoolean("animations",false).apply()
        val journey=AdventurerJourney(3210,7,987654);Model.set{it.copy(journey=journey)}
        val updated=ProfileStore.load(context)
        assertEquals(old.copy(role=0),updated.avatar);assertEquals("existing-device",updated.id);assertEquals("Keep my name",updated.name);assertFalse(ProfileStore.animations(context));assertEquals(journey,Model.value.journey)
        assertEquals(updated,ProfileStore.load(context))
    }
    @Test fun bothWholePersonalLooksSupportEveryHairExpressionAndPaletteWithoutDetachedParts(){
        for(look in 0..1){
            val style=AvatarStyle(look=look);val base=pixels(PortraitArt.portrait(context,style,1,0,0,false));assertTrue(base.count{it ushr 24>127}>3000)
            for(variant in listOf(style.copy(skin=4),style.copy(hairColour=5),style.copy(shirt=2),style.copy(element=4)))assertNotEquals(base.contentHashCode(),pixels(PortraitArt.portrait(context,variant,1,0,0,false)).contentHashCode())
            val hairs=(0..5).map{pixels(PortraitArt.portrait(context,style.copy(hair=it),1,0,0,false)).contentHashCode()};assertEquals(6,hairs.toSet().size)
            val eyes=(0..3).map{pixels(PortraitArt.portrait(context,style.copy(eyes=it),1,0,0,false)).contentHashCode()};assertEquals(4,eyes.toSet().size)
        }
        assertTrue(PortraitArt.cachedBytes()<=8*1024*1024)
    }
    @Test fun allFixedStarterAndMasteryDesignsUseHdWholeCelsAndIgnorePersonalCustomization(){
        for(role in CharacterRoster.roles.filter{CharacterRoster.fixed(it)}){
            val first=Adventure.roles[role].level;val mastery=CharacterRoster.mastery(role)
            val base=PortraitArt.portrait(context,AvatarStyle(role=role),first,0,0,false)
            val changed=PortraitArt.portrait(context,AvatarStyle(5,4,5,4,3,1,role,5),first,0,0,false)
            assertEquals(256,base.width);assertArrayEquals(pixels(base),pixels(changed))
            assertNotEquals("Distinct mastery for $role",pixels(base).contentHashCode(),pixels(PortraitArt.portrait(context,AvatarStyle(role=role),mastery,0,0,false)).contentHashCode())
            assertEquals(128,PortraitArt.portrait(context,AvatarStyle(role=role),first,0,0,true).width)
        }
    }
    @Test fun skillTimelinesUseValidAuthoredFramesAndFastDurationsWithNoProgressChanges(){
        val before=Model.value
        assertFalse(CharacterSkills.available(13,81));assertTrue(CharacterSkills.available(13,82))
        assertEquals(listOf(2800L,4800L,5200L),listOf(13,19,23).map{CharacterSkills.duration(it)})
        val folder=File(System.getProperty("sharemode.screenshots"),"skill-proof").apply{mkdirs()}
        for(role in listOf(13,19,23)){
            val seen=mutableSetOf<Int>()
            for(t in 0L..CharacterSkills.duration(role) step 10){val frame=CharacterSkills.at(role,t);assertTrue(frame.body in 0..if(role==13)71 else 23);assertTrue(frame.effect in -1..23);assertTrue(frame.eye in -1..15);seen.add(frame.body)}
            assertTrue(seen.size>=24);assertTrue(CharacterSkills.at(role,CharacterSkills.duration(role)).complete)
            for(time in listOf(350L,1600L,2200L)){
                val bitmap=Bitmap.createBitmap(256,256,Bitmap.Config.ARGB_8888);CharacterSkills.draw(Canvas(bitmap),context,role,time,RectF(0f,0f,256f,256f),false)
                val values=IntArray(256*256);bitmap.getPixels(values,0,256,0,0,256,256);assertTrue(values.toSet().size>100)
                File(folder,"$role-$time.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
            }
        }
        assertEquals(before,Model.value)
    }
    @Test fun hdPixelAndAutoSkillPreferencesSurviveReloadWithoutChangingTheProfile(){
        val initial=ProfileStore.load(context);ProfileStore.pixels(context,true);ProfileStore.autoSkills(context,false)
        assertTrue(ProfileStore.pixels(context));assertFalse(ProfileStore.autoSkills(context));assertEquals(initial,ProfileStore.load(context))
    }
}
