package com.rowell.shareme

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.DecodeHintType
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.QRCodeReader

// Keep the original module grid. Scaling an 800px raster twice can blur dense
// hotspot codes; the UI renders these modules using nearest-neighbour sampling.
fun pairingQr(text:String):BitMatrix {
    val writer=QRCodeWriter()
    var first:BitMatrix?=null
    for(mask in -1..7){
        val hints=mutableMapOf<EncodeHintType,Any>(EncodeHintType.MARGIN to 4,EncodeHintType.ERROR_CORRECTION to "M")
        if(mask>=0)hints[EncodeHintType.QR_MASK_PATTERN]=mask
        val matrix=writer.encode(text,BarcodeFormat.QR_CODE,0,0,hints)
        if(first==null)first=matrix
        // Some valid masks form extra finder-like patterns. Prefer one our QR
        // reader can actually locate, before displaying a random pairing code.
        if(readableQr(matrix,text))return matrix
    }
    return checkNotNull(first)
}
private fun readableQr(matrix:BitMatrix,text:String):Boolean=runCatching {
    val scale=4;val edge=matrix.width*scale
    val pixels=IntArray(edge*edge){index->if(matrix[(index%edge)/scale,(index/edge)/scale])0xff000000.toInt() else 0xffffffff.toInt()}
    QRCodeReader().decode(BinaryBitmap(HybridBinarizer(RGBLuminanceSource(edge,edge,pixels))),mapOf(DecodeHintType.TRY_HARDER to true)).text==text
}.getOrDefault(false)
fun qrDisplayPixels(modules:Int,availablePixels:Int):Int=(availablePixels/modules).coerceAtLeast(1)*modules
