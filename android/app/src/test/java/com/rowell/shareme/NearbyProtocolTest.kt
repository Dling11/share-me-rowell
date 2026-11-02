package com.rowell.shareme

import org.junit.Assert.*
import org.junit.Test

class NearbyProtocolTest {
    private val profile=ShareProfile("12345678-abcd","Rowell",AvatarStyle(4,3,2,1,2))
    private val pair=Pairing(listOf("192.168.1.2"),48900,"abcdef0123456789".repeat(4),"s".repeat(43),"receive","Rowell","Secret Wi-Fi","private-password")
    private fun rejected(block:()->Unit){try{block()}catch(_:IllegalArgumentException){return};fail("Invalid discovery data accepted")}
    @Test fun discoveryContainsPublicIdentityAndNoQrOrWifiSecrets(){
        val attributes=NearbyProtocol.attributes(profile,pair)
        assertEquals(setOf("v","id","name","avatar","fp"),attributes.keys)
        assertFalse(attributes.values.contains(pair.code));assertFalse(attributes.values.contains(pair.password));assertFalse(attributes.values.contains(pair.ssid))
        assertEquals(profile.avatar,NearbyProtocol.parse("192.168.1.2",48900,attributes).avatar)
    }
    @Test fun untrustedDiscoveryCannotTargetPublicAddressesOrInvalidPorts(){
        val attributes=NearbyProtocol.attributes(profile,pair)
        rejected{NearbyProtocol.parse("8.8.8.8",48900,attributes)}
        rejected{NearbyProtocol.parse("192.168.1.2",80,attributes)}
        rejected{NearbyProtocol.parse("192.168.1.2",48900,attributes+mapOf("fp" to "bad"))}
    }
    @Test fun spoofedDisplayControlsAndOversizedNamesAreRejected(){
        val attributes=NearbyProtocol.attributes(profile,pair)
        for(name in listOf("a".repeat(25),"A\nB","A\u202eB",""))rejected{NearbyProtocol.parse("192.168.1.2",48900,attributes+mapOf("name" to name))}
    }
    @Test fun nearbyNeedsAnOpenUnexpiredReceiverAndNeverAuthorizesReversePull(){
        assertTrue(NearbyProtocol.allowsRequest(true,true,true,true,false))
        assertFalse(NearbyProtocol.allowsRequest(true,false,true,true,true))
        assertFalse(NearbyProtocol.allowsRequest(true,true,false,true,true))
        assertFalse(NearbyProtocol.allowsRequest(true,true,true,false,true))
        assertFalse(NearbyProtocol.allowsRequest(false,true,true,true,false))
        assertTrue(NearbyProtocol.allowsRequest(false,false,false,true,true))
    }
    @Test fun comparisonCodeBindsTheTlsIdentityAndAFreshClientNonce(){
        val first=NearbyProtocol.transferCode(pair.fingerprint,"0".repeat(32))
        assertTrue(first.matches(Regex("[A-F0-9]{4}( [A-F0-9]{4}){3}")))
        assertEquals(first,NearbyProtocol.transferCode(pair.fingerprint,"0".repeat(32)))
        assertNotEquals(first,NearbyProtocol.transferCode(pair.fingerprint,"1".repeat(32)))
        assertNotEquals(first,NearbyProtocol.transferCode("a".repeat(64),"0".repeat(32)))
        rejected{NearbyProtocol.transferCode("invalid","0".repeat(32))}
        rejected{NearbyProtocol.transferCode(pair.fingerprint,"1".repeat(31))}
    }
    @Test fun avatarParsingBoundsEveryDrawingIndex(){
        assertEquals(profile.avatar,AvatarStyle.parse(profile.avatar.encode()))
        for(value in listOf("6.0.0.0.0","0.-1.0.0.0","0.0.5.0.0","0.0.0.5.0","0.0.0.0.3","0.0.0.0.0.2","0.0.0.0.0.-1","0.0.0.0.0.1.0","0.0","a.b.c.d.e"))rejected{AvatarStyle.parse(value)}
        repeat(100){val style=AvatarStyle.random();assertEquals(style,AvatarStyle.parse(style.encode()))}
    }
    @Test fun girlStyleSurvivesDiscoveryWithoutBreakingOlderAvatarReaders(){
        val girl=profile.copy(avatar=profile.avatar.copy(look=1))
        val attributes=NearbyProtocol.attributes(girl,pair)
        assertEquals("1",attributes["look"])
        assertEquals(5,attributes.getValue("avatar").split('.').size)
        assertEquals(girl.avatar.copy(look=0),AvatarStyle.parse(attributes.getValue("avatar")))
        assertEquals(girl.avatar,NearbyProtocol.parse("192.168.1.2",48900,attributes).avatar)
        assertEquals(profile.avatar,NearbyProtocol.parse("192.168.1.2",48900,NearbyProtocol.attributes(profile,pair)).avatar)
        for(look in listOf("-1","2","girl",""))rejected{NearbyProtocol.parse("192.168.1.2",48900,attributes+mapOf("look" to look))}
        repeat(20){assertEquals(1,AvatarStyle.random(1).look);assertEquals(0,AvatarStyle.random(0).look)}
    }
    @Test fun backgroundCleanupDoesNotInterruptTransfersOrVisibleReceiving(){
        assertTrue(SharingLifetime.shouldStop(false,false,false,120000))
        assertFalse(SharingLifetime.shouldStop(false,false,false,119999))
        assertFalse(SharingLifetime.shouldStop(true,false,false,999999))
        assertFalse(SharingLifetime.shouldStop(false,true,false,999999))
        assertFalse(SharingLifetime.shouldStop(false,false,true,999999))
    }
}
