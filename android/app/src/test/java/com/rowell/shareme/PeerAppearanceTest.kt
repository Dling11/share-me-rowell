package com.rowell.shareme

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class PeerAppearanceTest {
    @Test fun completeStylesRoundTripWithoutChangingTransferFields(){
        val avatar=AvatarStyle(5,4,5,4,3,1,13,5)
        val message=JSONObject().put("v",1).put("op","push").put("code","existing-code").withAppearance(avatar,82)
        assertEquals(PeerAppearance(avatar,82),PeerAppearance.parse(message))
        assertEquals(1,message.getInt("v"));assertEquals("push",message.getString("op"));assertEquals("existing-code",message.getString("code"))
    }
    @Test fun oldOrMalformedOptionalMetadataKeepsTheTransferUsable(){
        assertNull(PeerAppearance.parse(JSONObject().put("ok",true)))
        for(value in listOf("0.0.0","0.0.0.0.0.0.999.0","-1.0.0.0.0.0.0.0","1".repeat(81)))assertNull(PeerAppearance.parse(JSONObject().put("appearance",value)))
        assertNull(PeerAppearance.parse(JSONObject().put("appearance",JSONObject())))
    }
    @Test fun claimedLevelsAreBoundedCosmeticsAndNeverAwardProgress(){
        val journey=AdventurerJourney()
        val message=JSONObject().withAppearance(AvatarStyle(role=13),10000)
        assertEquals(100,PeerAppearance.parse(message)?.level)
        message.put("level",10000);assertEquals(1,PeerAppearance.parse(message)?.level)
        assertEquals(0,journey.xp);assertEquals(1,journey.level)
    }
}
