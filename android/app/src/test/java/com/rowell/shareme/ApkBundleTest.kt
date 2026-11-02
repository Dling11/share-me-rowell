package com.rowell.shareme

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ApkBundleTest {
    private fun archive(vararg entries:Pair<String,ByteArray>):ByteArray=ByteArrayOutputStream().also{output->ZipOutputStream(output).use{zip->entries.forEach{(name,bytes)->zip.putNextEntry(ZipEntry(name));zip.write(bytes);zip.closeEntry()}}}.toByteArray()
    private fun rejected(bytes:ByteArray){try{ApkBundle.read(ByteArrayInputStream(bytes)){_,copy->copy(ByteArrayOutputStream())}}catch(_:IllegalArgumentException){return};fail("Invalid APK bundle accepted")}
    @Test fun splitPartsStreamInOrderWithoutExtractingPaths(){
        val received=linkedMapOf<String,ByteArray>()
        ApkBundle.read(ByteArrayInputStream(archive("base.apk" to byteArrayOf(1,2),"split-1.apk" to byteArrayOf(3,4)))){name,copy->received[name]=ByteArrayOutputStream().also(copy).toByteArray()}
        assertEquals(listOf("base.apk","split-1.apk"),received.keys.toList());assertArrayEquals(byteArrayOf(3,4),received["split-1.apk"])
    }
    @Test fun traversalDirectoriesAndNonApkEntriesAreRejected(){
        for(name in listOf("../bad.apk","nested/bad.apk","x\\bad.apk","readme.txt","/absolute.apk","dir/"))rejected(archive("base.apk" to byteArrayOf(1),name to byteArrayOf(2)))
    }
    @Test fun missingEmptyOrMisorderedBaseCannotStartAnInstall(){
        rejected(archive());rejected(archive("split-1.apk" to byteArrayOf(1)));rejected(archive("base.apk" to byteArrayOf()))
    }
    @Test fun excessivePartsAreRejected(){
        val entries=listOf("base.apk" to byteArrayOf(1))+(1..128).map{"split-$it.apk" to byteArrayOf(1)}
        rejected(archive(*entries.toTypedArray()))
    }
    @Test fun cancellationStopsDecompression(){
        var checks=0
        try{ApkBundle.read(ByteArrayInputStream(archive("base.apk" to ByteArray(1024*1024))),checkCancelled={if(++checks>2)throw java.util.concurrent.CancellationException()}){_,copy->copy(ByteArrayOutputStream())};fail("Expected cancellation")}
        catch(_:java.util.concurrent.CancellationException){assertTrue(checks>2)}
    }
}
