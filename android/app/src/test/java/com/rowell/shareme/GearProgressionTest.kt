package com.rowell.shareme

import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class GearProgressionTest {
    @Test fun seriousAndAnimeClassesHaveEarnedStarterAndMasteryForms(){
        assertEquals(listOf(12,40,65,85,50),Adventure.roles.drop(7).take(5).map{it.level})
        assertEquals("Luffy-inspired",Adventure.nextRole(8)!!.name)
        for((role,mastery)in listOf(7 to 55,8 to 80,9 to 90,10 to 100)){
            assertFalse(Adventure.canEquip(role,Adventure.roles[role].level-1))
            assertFalse(Adventure.gear(role,mastery-1).ornate)
            assertTrue(Adventure.gear(role,mastery).ornate)
            assertEquals(2,Adventure.gear(role,mastery).source)
            val signature=Adventure.signature(AvatarStyle(look=1),role)
            assertEquals(1,signature.look);assertEquals(3,signature.eyes)
        }
    }
    @Test fun yugiAppendsWithoutChangingEarlierUnlocksAndKeepsMasteryRare(){
        assertEquals("Yugi-inspired",Adventure.roles[11].name)
        assertFalse(Adventure.canEquip(11,49));assertTrue(Adventure.canEquip(11,50))
        assertTrue(Adventure.isSpecial(11));assertFalse(Adventure.isSpecial(7))
        assertEquals("Card Duelist",Adventure.gear(11,50).title)
        assertFalse(Adventure.gear(11,94).ornate)
        assertTrue(Adventure.gear(11,95).ornate)
        assertEquals("Pharaoh Duelist",Adventure.gear(11,95).title)
        assertEquals(0,Adventure.specialRow(11,50));assertEquals(2,Adventure.specialRow(11,95))
    }
    @Test fun courierStartsUnarmouredAndEvolvesAtTheActualMilestones(){
        assertEquals("Novice Courier",Adventure.gear(0,1).title)
        assertEquals(0,Adventure.gear(0,9).pair)
        assertEquals("Courier Trainee",Adventure.gear(0,10).title)
        assertEquals("Iron Courier",Adventure.gear(0,25).title)
        assertFalse(Adventure.gear(0,44).ornate)
        assertEquals("Courier Knight",Adventure.gear(0,45).title)
        assertTrue(Adventure.gear(0,45).ornate)
    }
    @Test fun advancedBodiesEffectsAndFinalAscensionCannotAppearEarly(){
        for(role in Adventure.roles.indices)for(level in 1..44)assertFalse(Adventure.gear(role,level).ornate)
        for((role,level)in listOf(1 to 45,2 to 45,3 to 60,4 to 75,5 to 85,6 to 100)){
            assertFalse("Advanced gear appeared early for $role",Adventure.gear(role,level-1).ornate)
            assertTrue(Adventure.gear(role,level).ornate)
        }
        assertEquals(Adventure.gear(0,1),Adventure.gear(6,1))
        assertNotEquals(Adventure.gear(6,99).id,Adventure.gear(6,100).id)
        assertEquals(7,Adventure.gear(6,100).pair)
        assertEquals(1,Adventure.gear(6,100).source)
    }
    @Test fun tinyFilesCannotFarmXpAndEveryFileHasAStrictCap(){
        assertEquals(0,Adventure.fileXp(1))
        assertEquals(0,Adventure.fileXp(256L*1024-1))
        assertEquals(1,Adventure.fileXp(256L*1024))
        assertEquals(4,Adventure.fileXp(1024L*1024))
        assertEquals(50,Adventure.fileXp(50L*256*1024))
        assertEquals(50,Adventure.fileXp(Long.MAX_VALUE))
        assertEquals(3_302_937L,Adventure.threshold(100))
        assertEquals(66_059L,(Adventure.threshold(100)+49)/50)
    }
    @Test fun changingFutureRewardsDoesNotRecalculateOldXpOrReceipts(){
        val context=RuntimeEnvironment.getApplication()
        context.deleteDatabase("adventurer.db")
        context.openOrCreateDatabase("adventurer.db",0,null).use{db->
            db.execSQL("CREATE TABLE journey (id INTEGER PRIMARY KEY,xp INTEGER NOT NULL,files INTEGER NOT NULL,bytes INTEGER NOT NULL)")
            db.execSQL("INSERT INTO journey VALUES(1,65,5,4194304)")
            db.execSQL("CREATE TABLE credited (digest TEXT NOT NULL,size INTEGER NOT NULL,PRIMARY KEY(digest,size)) WITHOUT ROWID")
            db.execSQL("INSERT INTO credited VALUES(?,?)",arrayOf("a".repeat(64),1048576))
            db.version=1
        }
        val before=JourneyStore.load(context)
        assertEquals(65L,before.xp);assertEquals(2,before.level)
        assertEquals(0,JourneyStore.award(context,FileMeta("old","renamed.bin",1048576,"a".repeat(64))))
        assertEquals(before,JourneyStore.load(context))
        assertEquals(4,JourneyStore.award(context,FileMeta("new","new.bin",1048576,"b".repeat(64))))
        assertEquals(69L,JourneyStore.load(context).xp)
    }
}
