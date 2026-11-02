package com.rowell.shareme

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class PairingQrTest {
    @Test fun denseHotspotCodesDecodeAtSmallPhoneSizesWithoutAnIntermediateRaster(){
        val random=Random(19381)
        repeat(12){iteration->
            val fp=hex(random.nextBytes(32));val secret=java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(random.nextBytes(32))
            val text=Pairing(listOf("192.168.145.27"),48932,fp,secret,"receive","My nearby phone","AndroidShare_12345","A-secure-passphrase-12345").text()
            val matrix=pairingQr(text)
            assertTrue("Keep QR memory bounded to its module grid",matrix.width<180)
            for(edge in listOf(336,435,560)){
                val qrEdge=qrDisplayPixels(matrix.width,edge);val scale=qrEdge/matrix.width;val padding=(edge-qrEdge)/2
                assertTrue(qrEdge<=edge);assertEquals(0,qrEdge%matrix.width)
                val pixels=IntArray(edge*edge){index->val x=index%edge-padding;val y=index/edge-padding;if(x in 0 until qrEdge&&y in 0 until qrEdge&&matrix[x/scale,y/scale])0xff000000.toInt() else 0xffffffff.toInt()}
                val image=BinaryBitmap(HybridBinarizer(RGBLuminanceSource(edge,edge,pixels)))
                try{assertEquals(text,MultiFormatReader().decode(image,mapOf(DecodeHintType.TRY_HARDER to true)).text)}
                catch(e:Exception){throw AssertionError("QR case=$iteration edge=$edge modules=${matrix.width} text=$text",e)}
            }
        }
    }
}
