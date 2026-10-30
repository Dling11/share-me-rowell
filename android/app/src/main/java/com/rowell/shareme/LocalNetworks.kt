package com.rowell.shareme

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import java.net.Inet4Address
import java.net.NetworkInterface

object LocalNetworks {
    fun addresses(context: Context, hotspot: Boolean = false): List<LocalAddress> {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        val wifi = manager.allNetworks.filter { network ->
            manager.getNetworkCapabilities(network)?.let {
                !it.hasTransport(NetworkCapabilities.TRANSPORT_VPN) &&
                    (it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
            } == true
        }.flatMap { network ->
            val kind = if (manager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true)
                LocalAddress.Kind.WIFI else LocalAddress.Kind.ETHERNET
            manager.getLinkProperties(network)?.linkAddresses.orEmpty().mapNotNull {
                val ip = it.address as? Inet4Address ?: return@mapNotNull null
                LocalAddress(ip.hostAddress.orEmpty(), it.prefixLength, kind)
            }
        }
        // The hotspot interface is not exposed as a ConnectivityManager Wi-Fi Network.
        // Never advertise carrier (rmnet/ccmni), VPN or virtual-adapter private addresses.
        val interfaces = runCatching { NetworkInterface.getNetworkInterfaces().toList() }.getOrDefault(emptyList())
            .filter { it.isUp && !it.isLoopback }
            .flatMap { adapter ->
                val name = adapter.name.lowercase()
                val ap = name.startsWith("ap") || name.contains("swlan") || name.contains("softap")
                val lan = ap || name.startsWith("wlan") || name.startsWith("wifi") || name.startsWith("eth") || name.startsWith("rndis")
                if (!lan) emptyList() else adapter.interfaceAddresses.mapNotNull {
                    val ip = (it.address as? Inet4Address)?.hostAddress ?: return@mapNotNull null
                    if (wifi.any { known -> known.host == ip }) return@mapNotNull null
                    LocalAddress(ip, it.networkPrefixLength.toInt(), if (ap || hotspot) LocalAddress.Kind.HOTSPOT else LocalAddress.Kind.WIFI)
                }
            }
        return (interfaces + wifi).filter { privateHost(it.host) && !it.host.startsWith("127.") }.distinctBy { it.host }
    }

    fun ready(properties: LinkProperties): Boolean = properties.linkAddresses.any { address ->
        address.address is Inet4Address && properties.routes.any { route ->
            !route.isDefaultRoute && route.destination.contains(address.address)
        }
    }

    fun reaches(properties: LinkProperties, hosts: List<String>): Boolean = properties.linkAddresses.any { address ->
        val ip = (address.address as? Inet4Address)?.hostAddress ?: return@any false
        hosts.any { LocalRouting.sameSubnet(ip, it, address.prefixLength) }
    }

    fun wifiForHosts(context: Context, hosts: List<String>): Network? {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        return manager.allNetworks.firstOrNull { network ->
            manager.getNetworkCapabilities(network)?.let {
                it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) && !it.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            } == true && manager.getLinkProperties(network)?.let { ready(it) && reaches(it, hosts) } == true
        }
    }
}
