package com.rowell.shareme

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[35])
class IncomingShareTest {
    private val one=Uri.parse("content://example.documents/document/one")
    private val two=Uri.parse("content://example.documents/document/two")
    private fun rejected(intent:Intent){assertThrows(IllegalArgumentException::class.java){IncomingShare.files(intent)}}
    @Test fun singleAndMultipleSharesKeepContentHandlesAndDeduplicate(){
        assertEquals(listOf(one),IncomingShare.files(Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM,one)))
        assertEquals(listOf(one,two),IncomingShare.files(Intent(Intent.ACTION_SEND_MULTIPLE).putParcelableArrayListExtra(Intent.EXTRA_STREAM,arrayListOf(one,two,one))))
    }
    @Test fun clipDataFallbackAndServiceGrantCarryEveryUri(){
        val clip=ClipData.newRawUri("Files",one).apply{addItem(ClipData.Item(two))}
        assertEquals(listOf(one,two),IncomingShare.files(Intent(Intent.ACTION_SEND_MULTIPLE).apply{clipData=clip}))
        val service=Intent();IncomingShare.attach(service,listOf(one,two),true)
        assertEquals(listOf(one.toString(),two.toString()),service.getStringArrayListExtra("uris"))
        assertEquals(2,service.clipData!!.itemCount)
        assertEquals(two,service.clipData!!.getItemAt(1).uri)
        assertTrue(service.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION!=0)
        assertTrue(service.getBooleanExtra("replace_selection",false))
    }
    @Test fun filesAreRequiredAndPrivateFilePathsCannotBeInjected(){
        assertEquals(emptyList<Uri>(),IncomingShare.files(Intent(Intent.ACTION_MAIN)))
        rejected(Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT,"https://example.com"))
        for(uri in listOf("file:///data/data/com.rowell.shareme/secret","https://example.com/file","content://com.rowell.shareme.files/installer/private","content:/missing-authority"))
            rejected(Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM,Uri.parse(uri)))
        rejected(Intent(Intent.ACTION_SEND_MULTIPLE).putParcelableArrayListExtra(Intent.EXTRA_STREAM,ArrayList((0..16).map{Uri.parse("content://files/$it")})))
    }
}
