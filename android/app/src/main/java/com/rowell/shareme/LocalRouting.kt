package com.rowell.shareme

/** Numeric IPv4 routing decisions. A matching private subnet alone is not hotspot identity. */
data class LocalAddress(val host: String, val prefix: Int, val kind: Kind) {
    enum class Kind { WIFI, HOTSPOT, ETHERNET }
    fun reaches(peer: String): Boolean = LocalRouting.sameSubnet(host, peer, prefix)
}

object LocalRouting {
    private fun ipv4(host: String): Long? {
        val parts = host.split('.')
        if (parts.size != 4) return null
        var value = 0L
        for (part in parts) {
            if (part.isEmpty() || part.any { it !in '0'..'9' }) return null
            val number = part.toIntOrNull()?.takeIf { it in 0..255 } ?: return null
            value = (value shl 8) or number.toLong()
        }
        return value
    }
    fun sameSubnet(local: String, peer: String, prefix: Int): Boolean {
        if (prefix !in 1..32) return false // A default route is not proof of LAN reachability.
        val left = ipv4(local) ?: return false
        val right = ipv4(peer) ?: return false
        val mask = (0xffffffffL shl (32 - prefix)) and 0xffffffffL
        return (left and mask) == (right and mask)
    }
    fun advertised(addresses: List<LocalAddress>, hotspot: Boolean): List<String> = addresses
        .filter { !hotspot || it.kind == LocalAddress.Kind.HOTSPOT }
        .map { it.host }.filter { privateHost(it) && !it.startsWith("127.") }.distinct().take(8)

    fun orderedHosts(hosts: List<String>, addresses: List<LocalAddress>): List<String> = hosts
        .distinct().sortedBy { peer -> if (addresses.any { it.reaches(peer) }) 0 else 1 }

    fun needsHotspotRequest(ssid: String, joinedSsid: String?, ready: Boolean): Boolean =
        ssid.isNotEmpty() && !(ssid == joinedSsid && ready)
}
