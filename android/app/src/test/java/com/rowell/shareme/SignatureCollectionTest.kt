package com.rowell.shareme

import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SignatureCollectionTest {
    @Test fun newSignaturesAppendWithSeparateHardEarnedMasteryWithoutChangingExistingIds(){
        assertEquals(listOf(1,8,18,35,55,75,95,12,40,65,85,50),Adventure.roles.take(12).map{it.level})
        assertEquals(listOf("Luffy-inspired","Zoro-inspired","Boa Hancock-inspired","Shanks-inspired","Eren-inspired","Levi-inspired","Mikasa-inspired","Sukuna-inspired","Itachi-inspired","Rock Lee-inspired","Might Guy-inspired","Madara-inspired"),Adventure.roles.drop(12).map{it.name})
        val ids=mutableSetOf<Int>()
        for(role in 12..23){val first=Adventure.roles[role].level;val master=Adventure.favouriteMasteries[role-12]
            assertTrue(master>first);assertFalse(Adventure.canEquip(role,first-1));assertTrue(Adventure.canEquip(role,first))
            assertFalse(Adventure.gear(role,master-1).ornate);assertTrue(Adventure.gear(role,master).ornate)
            assertEquals(0,Adventure.specialRow(role,first));assertEquals(3,Adventure.specialRow(role,master))
            assertTrue(ids.add(Adventure.gear(role,first).id));assertTrue(ids.add(Adventure.gear(role,master).id))
        }
        assertFalse(Adventure.canEquip(24,100));assertEquals(50,Adventure.fileXp(Long.MAX_VALUE));assertEquals(3_302_937L,Adventure.threshold(100))
    }
    @Test fun everyNewCharacterOwnsTwoCompleteTwelveCelOutfitsWithDistinctSilhouettes(){
        val identities=mutableSetOf<Int>();val context=RuntimeEnvironment.getApplication()
        for(role in 12..23){val first=SpecialSprites.frames(context,role,Adventure.roles[role].level);val master=SpecialSprites.frames(context,role,100)
            assertEquals(12,first.size);assertEquals(12,master.size)
            val outfits=listOf(first,master).map{frames->frames.map{frame->
                assertEquals(64,frame.width);assertEquals(64,frame.height)
                val pixels=IntArray(4096);frame.readPixels(pixels)
                assertTrue(pixels.count{it ushr 24==255}>150);assertTrue(pixels.all{it ushr 24==0||it ushr 24==255})
                pixels.contentHashCode()
            }}
            assertTrue(outfits[0].toSet().size>=8);assertTrue(outfits[1].toSet().size>=8)
            assertNotEquals(outfits[0][0],outfits[1][0]);assertTrue(identities.add(outfits[0][0]))
        }
        assertEquals(12,identities.size)
    }
    @Test fun authoredIdleUsesEveryInBetweenAndKeepsCheerAsATapReaction(){
        for(role in 12..23){val poses=(0 until 80).map{SpecialSprites.poseIndex(PixelIdle.pose(it/80f),role)}
            assertEquals((0..10).toSet(),poses.toSet());assertEquals(11,SpecialSprites.poseIndex(0,role,2))
            assertEquals(0,poses.first());assertEquals(0,poses.last());assertTrue(poses.count{it==3}<=4)
            assertEquals(listOf(8,9,10),poses.filter{it in 8..10}.distinct())
        }
        assertEquals(6,SpecialSprites.poseIndex(0,11,2));assertEquals(0,SpecialSprites.poseIndex(0,8))
    }
    @Test fun optionalCollectionMetadataRetainsTheOldRoleHeroAndGuestBounds(){
        val pair=Pairing(listOf("192.168.1.2"),48900,"a".repeat(64),"s".repeat(43),"receive","Buddy")
        for(role in 12..23){val style=AvatarStyle(role=role);val attrs=NearbyProtocol.attributes(ShareProfile("12345678-abcd","Buddy",style),pair,100)
            assertFalse(attrs.containsKey("role"));assertFalse(attrs.containsKey("hero"));assertFalse(attrs.containsKey("guest"))
            assertEquals(role.toString(),attrs["collect"]);assertEquals(role,NearbyProtocol.parse("192.168.1.2",48900,attrs).avatar.role)
            assertEquals(0,NearbyProtocol.parse("192.168.1.2",48900,attrs-"collect").avatar.role)
            val hello=NearbyProtocol.cosmetics(style,100);assertEquals(role,NearbyProtocol.parseAvatar(style.legacyEncode(),collect=hello["collect"]).role)
        }
        for(value in listOf("11","24","-1","nope","")){try{NearbyProtocol.parseAvatar("0.0.0.0.0",collect=value);fail("Accepted invalid collect=$value")}catch(_:IllegalArgumentException){}}
    }
}
