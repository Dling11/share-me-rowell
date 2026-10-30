package com.rowell.shareme

import org.junit.Assert.*
import org.junit.Test

class LocalRoutingTest {
    @Test fun defaultRoutesAndMalformedAddressesDoNotProveLocalReachability() {
        assertFalse(LocalRouting.sameSubnet("192.168.1.2", "10.1.2.3", 0))
        assertFalse(LocalRouting.sameSubnet("192.168.1.2", "192.168.1.999", 24))
        assertFalse(LocalRouting.sameSubnet("192.168.1.2", "example.com", 24))
        assertTrue(LocalRouting.sameSubnet("172.20.10.2", "172.20.10.1", 28))
        assertFalse(LocalRouting.sameSubnet("172.20.10.2", "172.20.10.17", 28))
    }
    @Test fun hotspotPairingMustJoinTheNamedNetworkEvenWhenHomeWifiHasTheSameSubnet() {
        assertTrue(LocalRouting.needsHotspotRequest("DIRECT-phone", null, true))
        assertTrue(LocalRouting.needsHotspotRequest("DIRECT-phone", "Home Wi-Fi", true))
        assertTrue(LocalRouting.needsHotspotRequest("DIRECT-phone", "DIRECT-phone", false))
        assertFalse(LocalRouting.needsHotspotRequest("DIRECT-phone", "DIRECT-phone", true))
        assertFalse(LocalRouting.needsHotspotRequest("", null, false))
    }
    @Test fun hotspotQrOnlyAdvertisesTheHotspotRatherThanTheConcurrentRouter() {
        val addresses = listOf(
            LocalAddress("192.168.1.4", 24, LocalAddress.Kind.WIFI),
            LocalAddress("192.168.43.1", 24, LocalAddress.Kind.HOTSPOT),
            LocalAddress("127.0.0.1", 8, LocalAddress.Kind.HOTSPOT))
        assertEquals(listOf("192.168.43.1"), LocalRouting.advertised(addresses, true))
        assertEquals(2, LocalRouting.advertised(addresses, false).size)
        assertTrue(LocalRouting.advertised(addresses.take(1), true).isEmpty())
    }
    @Test fun reachableWifiAddressIsTriedBeforeUnrelatedQrAlternatives() {
        val hosts = listOf("10.0.0.5", "192.168.1.55", "172.29.32.1")
        assertEquals(listOf("192.168.1.55", "10.0.0.5", "172.29.32.1"), LocalRouting.orderedHosts(hosts,
            listOf(LocalAddress("192.168.1.7", 24, LocalAddress.Kind.WIFI))))
    }
}
