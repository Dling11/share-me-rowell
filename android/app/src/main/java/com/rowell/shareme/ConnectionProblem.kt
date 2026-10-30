package com.rowell.shareme

import java.io.EOFException
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException

class PeerRejected(message:String):IOException(message)

object ConnectionProblem{
    fun describe(error:Throwable,host:String="",sameSubnet:Boolean?=null):String{
        val causes=generateSequence(error){it.cause}.take(12).toList()
        causes.filterIsInstance<PeerRejected>().firstOrNull()?.let{return it.message?:"The other device declined the request."}
        if(causes.any{it is SSLException})return "The secure connection check failed. Refresh the other device's QR code and check the date and time on both devices."
        if(causes.any{it is SecurityException})return "Android could not access the selected file or network. Check the permission and choose the file again."
        val subnetHint=if(sameSubnet==false)" The QR address is outside this phone's Wi-Fi subnet; it may use a different network or PC adapter." else " Guest Wi-Fi or a firewall may block local connections."
        if(causes.any{it is SocketTimeoutException||it is NoRouteToHostException})return "The other device could not be reached${if(host.isNotEmpty())" ($host)" else ""}.$subnetHint Join the same router/hotspot, refresh its QR code and scan again."
        if(causes.any{it is ConnectException})return "The connection was refused. Open Share Mode on the other device, choose Send or Receive, refresh its QR code and scan again.$subnetHint"
        if(causes.any{it is EOFException})return "The transfer was interrupted. Reconnect and send the same files to resume unfinished downloads."
        return error.message?:"The transfer could not finish. Reconnect and send the same files to resume."
    }
}
