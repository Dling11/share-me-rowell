package com.rowell.shareme

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.*

class ProtocolTest {
    private val file=FileMeta("abc","game.iso",5_000_000_000,"a".repeat(64))
    private fun rejected(block:()->Unit){try{block();fail("Expected rejection")}catch(_:Exception){}}
    @Test fun pairingRoundTripsHotspotDetailsAndCertificatePin(){
        val pair=Pairing(listOf("192.168.43.1"),48290,"b".repeat(64),"x".repeat(43),"receive","My phone","Offline hotspot","my-password")
        assertEquals(pair,Pairing.parse(pair.text()))
    }
    @Test fun pairingRejectsPublicHostsWrongSchemesAndInvalidPins(){
        val pair=Pairing(listOf("8.8.8.8"),48290,"b".repeat(64),"x".repeat(43),"receive","PC")
        rejected{Pairing.parse(pair.text())};rejected{Pairing.parse("https://example.com")}
        rejected{Pairing.parse(pair.copy(hosts=listOf("192.168.1.2"),fingerprint="bad").text())}
    }
    @Test fun metadataPreservesSizesAboveFourGigabytes(){assertEquals(5_000_000_000,FileMeta.parse(file.json()).size)}
    @Test fun framingDoesNotConsumeBinaryPayload(){
        val output=ByteArrayOutputStream();Wire.write(output,JSONObject().put("ok",true));output.write(byteArrayOf(0,10,-1,4))
        val input=ByteArrayInputStream(output.toByteArray());assertTrue(Wire.read(input).getBoolean("ok"));assertArrayEquals(byteArrayOf(0,10,-1,4),input.readBytes())
    }
    @Test fun copyStopsAtTheExactFileBoundary(){val output=ByteArrayOutputStream();val input=ByteArrayInputStream(byteArrayOf(1,2,3,4));Wire.copy(input,output,3);assertArrayEquals(byteArrayOf(1,2,3),output.toByteArray());assertEquals(4,input.read())}
    @Test fun truncatedTransfersFailWithoutPretendingTheyCompleted(){rejected{Wire.copy(ByteArrayInputStream(byteArrayOf(1)),ByteArrayOutputStream(),10)}}
    @Test fun oversizedHeadersAndDuplicateIdsAreRejected(){
        rejected{Wire.read(ByteArrayInputStream("x".repeat(Wire.HEADER_LIMIT+2).toByteArray()))}
        rejected{Wire.files(JSONObject().put("files",JSONArray(listOf(file.json(),file.json()))))}
    }
    @Test fun namesStayInsideTheReceiveFolderAndWindowsReservedNamesAreSafe(){
        for(name in listOf("../../bad.apk","CON.txt","a\\b.iso","\u202egame.exe")){val safe=safeName(name);assertFalse('/' in safe);assertFalse('\\' in safe);assertFalse('\u202e' in safe)}
        assertEquals("_CON.txt",safeName("CON.txt"))
        assertEquals("save.zip",safeName("save.zip"))
    }
    @Test fun emptyFilesHaveTheStandardSha256(){assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",sha256(ByteArrayInputStream(byteArrayOf())))}
    @Test fun disconnectCanCancelChecksumVerificationBeforeReadingTheWholeFile(){
        val input=ByteArrayInputStream(ByteArray(2*1024*1024){7});var checks=0
        try{sha256(input){if(++checks==2)throw java.util.concurrent.CancellationException("Disconnected")};fail("Verification should stop")}
        catch(_:java.util.concurrent.CancellationException){assertTrue(input.available()>1024*1024)}
    }
    @Test fun resumeSkipReadsTheCorrectSuffix(){val input=ByteArrayInputStream(byteArrayOf(1,2,3,4,5));Wire.skip(input,3);assertArrayEquals(byteArrayOf(4,5),input.readBytes())}
    @Test fun certificatePinRejectsADifferentCertificate(){val encoded="test certificate".toByteArray();val expected=hex(java.security.MessageDigest.getInstance("SHA-256").digest(encoded));verifyCertificate(encoded,expected);rejected{verifyCertificate(encoded,"a".repeat(64))}}
    @Test fun aCancelledSessionCannotClearTheNewSessionsProgress(){val sessions=SessionEpoch();var status="idle";val old=sessions.next();sessions.update(old){status="old receiving"};val current=sessions.next();sessions.update(current){status="new receiving"};assertFalse(sessions.update(old){status="old stopped"});assertEquals("new receiving",status);assertTrue(sessions.update(current){status="complete"})}
}
