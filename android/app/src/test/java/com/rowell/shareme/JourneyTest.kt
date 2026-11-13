package com.rowell.shareme

import android.content.Context
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class JourneyTest {
    @Test fun yugiGuestMetadataKeepsThePreviousHeroFieldReadable(){
        val style=AvatarStyle(role=11)
        val p=ShareProfile("12345678-abcd","Card buddy",style)
        val pair=Pairing(listOf("192.168.1.2"),48900,"a".repeat(64),"s".repeat(43),"receive","Card buddy")
        val attributes=NearbyProtocol.attributes(p,pair,50)
        assertFalse(attributes.containsKey("hero"));assertFalse(attributes.containsKey("role"))
        assertEquals("11",attributes["guest"])
        assertEquals(11,NearbyProtocol.parse("192.168.1.2",48900,attributes).avatar.role)
        assertEquals(0,NearbyProtocol.parse("192.168.1.2",48900,attributes-"guest").avatar.role)
        try{NearbyProtocol.parse("192.168.1.2",48900,attributes+mapOf("guest" to "12"));fail("Invalid guest accepted")}catch(_:IllegalArgumentException){}
        val hello=org.json.JSONObject().put("avatar",style.legacyEncode()).apply{NearbyProtocol.cosmetics(style,50).forEach{(key,value)->put(key,value)}}
        assertFalse(hello.has("role"));assertFalse(hello.has("hero"));assertEquals("11",hello.getString("guest"))
        assertEquals(11,NearbyProtocol.parseAvatar(hello.getString("avatar"),guest=hello.getString("guest")).role)
        // The previous hello parser reads only the base role/appearance, ignoring new keys.
        assertEquals(0,NearbyProtocol.parseAvatar(hello.getString("avatar")).role)
    }
    @Test fun fixedCharactersPreserveThePersonalAvatarAndAllowSwitchingBack(){
        Model.set{it.copy(journey=AdventurerJourney(Adventure.threshold(100)))}
        val initial=ProfileStore.load(context).copy(avatar=AvatarStyle(3,4,4,2,1,1,0,5))
        ProfileStore.save(context,initial)
        for(role in Adventure.roles.indices.filter{Adventure.isSpecial(it)}){
            ProfileStore.save(context,initial.copy(name="My device",avatar=AvatarStyle(role=role)))
            val special=ProfileStore.load(context)
            assertEquals(initial.avatar.copy(role=role),special.avatar)
            assertEquals("My device",special.name)
        }
        ProfileStore.save(context,ProfileStore.load(context).copy(avatar=ProfileStore.load(context).avatar.copy(role=7)))
        assertEquals(initial.avatar.copy(role=0),ProfileStore.load(context).avatar)
        ProfileStore.save(context,ProfileStore.load(context).copy(avatar=initial.avatar.copy(hair=2,role=7)))
        assertEquals(2,ProfileStore.load(context).avatar.hair)
    }
    @Test fun newHeroesAndFocusedMoodKeepTheLegacyDiscoveryFieldsReadable(){
        val style=Adventure.signature(AvatarStyle(look=1),8)
        val profile=ShareProfile("12345678-abcd","Teacher",style)
        val pair=Pairing(listOf("192.168.1.2"),48900,"a".repeat(64),"s".repeat(43),"receive","Teacher")
        val attributes=NearbyProtocol.attributes(profile,pair,80)
        assertFalse(attributes.containsKey("role"))
        assertEquals("8",attributes["hero"]);assertEquals("3",attributes["mood"])
        assertEquals(0,AvatarStyle.parse(attributes.getValue("avatar")).eyes)
        assertEquals(style,NearbyProtocol.parse("192.168.1.2",48900,attributes).avatar)
        assertEquals(style,AvatarStyle.parse(style.encode()))
    }
    private val context:Context get()=RuntimeEnvironment.getApplication()
    @Before fun reset(){context.deleteDatabase("adventurer.db");Model.set{ScreenState(avatarAnimations=false)}}
    @Test fun curveHasIncreasingCostsAndExactLevelBoundaries(){
        assertEquals(1,Adventure.level(-50));assertEquals(1,Adventure.level(0))
        var cost=0L
        for(level in 2..100){val threshold=Adventure.threshold(level);assertEquals(level,Adventure.level(threshold));assertEquals(level-1,Adventure.level(threshold-1));val next=threshold-Adventure.threshold(level-1);assertTrue(next>cost);cost=next}
        assertEquals(100,Adventure.level(Long.MAX_VALUE));assertEquals(1f,AdventurerJourney(Adventure.threshold(100)).fraction)
        assertEquals(listOf(1,8,18,35,55,75,95),Adventure.roles.take(7).map{it.level})
    }
    @Test fun progressionStartsFreshWithoutReadingPastHistory(){
        context.getSharedPreferences("transfers",0).edit().clear().commit()
        ReceiveStore(context).addHistory("Sent","old-transfer.txt",100,"content://history/one")
        assertEquals(1,ReceiveStore(context).history().size)
        assertEquals(AdventurerJourney(),JourneyStore.load(context))
        assertFalse(Adventure.canEquip(1,1));assertTrue(Adventure.canEquip(1,8));assertFalse(Adventure.canEquip(24,100))
    }
    @Test fun durableDigestReceiptPreventsRenamesResendsAndBothDirectionRewards(){
        val file=FileMeta("one","notes.txt",1024*1024,"a".repeat(64))
        assertEquals(4,JourneyStore.award(context,file));val first=JourneyStore.load(context)
        assertEquals(0,JourneyStore.award(context,file.copy(id="other-id",name="renamed.txt")))
        Model.set{ScreenState()};assertEquals(0,JourneyStore.award(context,file))
        assertEquals(first,JourneyStore.load(context));assertEquals(first,Model.value.journey)
        assertEquals(1L,first.files);assertEquals(file.size,first.bytes)
        assertEquals(4,JourneyStore.award(context,file.copy(sha="b".repeat(64))))
        assertEquals(2L,JourneyStore.load(context).files)
    }
    @Test fun invalidAndEmptyContentCannotEarnXpAndLargeFilesStayBounded(){
        assertEquals(0,JourneyStore.award(context,FileMeta("one","empty",0,"a".repeat(64))))
        assertEquals(0,JourneyStore.award(context,FileMeta("two","invalid",20,"bad")))
        assertEquals(0L,JourneyStore.load(context).xp)
        assertEquals(0,Adventure.fileXp(1));assertEquals(50,Adventure.fileXp(Long.MAX_VALUE))
    }
    @Test fun optionalCosmeticsRoundTripWhileOldPeersGetFivePartAvatar(){
        val style=AvatarStyle(3,2,1,4,1,1,5,3)
        assertEquals(style,AvatarStyle.parse(style.encode()))
        val profile=ShareProfile("12345678-abcd","Rowell",style)
        val pair=Pairing(listOf("192.168.1.2"),48900,"a".repeat(64),"s".repeat(43),"receive","Rowell")
        val attributes=NearbyProtocol.attributes(profile,pair,82)
        assertEquals(5,attributes.getValue("avatar").split('.').size)
        val peer=NearbyProtocol.parse("192.168.1.2",48900,attributes)
        assertEquals(style,peer.avatar);assertEquals(82,peer.level)
        for((key,value)in listOf("role" to "7","element" to "6","level" to "101","level" to "0")){
            try{NearbyProtocol.parse("192.168.1.2",48900,attributes+mapOf(key to value));fail("Accepted $key=$value")}catch(_:IllegalArgumentException){}
        }
        assertEquals(AvatarStyle(3,2,1,4,1),AvatarStyle.parse(style.legacyEncode()))
    }
    @Test fun lockedRolesCannotBeSavedButAllElementsAndLooksRemainFree(){
        val profile=ProfileStore.load(context).copy(name=" Rowell ",avatar=AvatarStyle(look=1,role=6,element=5))
        ProfileStore.save(context,profile)
        assertEquals(0,ProfileStore.load(context).avatar.role);assertEquals(5,ProfileStore.load(context).avatar.element)
        Model.set{it.copy(journey=AdventurerJourney(Adventure.threshold(95)))}
        ProfileStore.save(context,profile);assertEquals(6,ProfileStore.load(context).avatar.role)
        assertEquals("Rowell",ProfileStore.load(context).name)
        ProfileStore.animations(context,false);assertFalse(ProfileStore.animations(context))
    }
}
