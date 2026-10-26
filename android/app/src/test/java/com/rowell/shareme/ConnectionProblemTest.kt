package com.rowell.shareme

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.EOFException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

class ConnectionProblemTest{
    @Test fun busyAndExpiredCodeRepliesKeepTheActualPeerReason(){
        for(message in listOf("This device is busy with another transfer.","Pairing expired. Scan the current QR code.")){
            try{Wire.success(JSONObject().put("ok",false).put("error",message));fail("Expected rejection")}
            catch(error:PeerRejected){assertEquals(message,ConnectionProblem.describe(error,"192.168.1.2",false))}
        }
    }
    @Test fun subnetMismatchIsAHintRatherThanAClaimAboutWifiNames(){
        val message=ConnectionProblem.describe(SocketTimeoutException(),"172.29.32.1",false)
        assertTrue(message.contains("outside"));assertTrue(message.contains("may"));assertTrue(message.contains("refresh",true))
    }
    @Test fun aTimeoutWithoutSubnetEvidenceDoesNotInventAMismatch(){
        val message=ConnectionProblem.describe(SocketTimeoutException(),"192.168.1.2",null)
        assertFalse(message.contains("outside"));assertTrue(message.contains("may block"))
    }
    @Test fun wrappedSecurityFailureIsNotReportedAsWifiMismatch(){
        val message=ConnectionProblem.describe(IOException("Connect failed",SSLHandshakeException("Certificate mismatch")),sameSubnet=false)
        assertTrue(message.contains("secure connection"));assertTrue(message.contains("date and time"));assertFalse(message.contains("subnet"))
    }
    @Test fun refusedConnectionSuggestsOpeningTheAppAndRefreshingTheCode(){
        val message=ConnectionProblem.describe(ConnectException(),sameSubnet=true)
        assertTrue(message.contains("refused"));assertTrue(message.contains("Open Share-Me-Rowell"));assertTrue(message.contains("refresh"))
    }
    @Test fun interruptedStreamExplainsHowToResume(){assertTrue(ConnectionProblem.describe(EOFException()).contains("resume"))}
}
