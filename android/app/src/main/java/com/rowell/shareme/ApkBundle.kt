package com.rowell.shareme

import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipInputStream

/** Only root-level APK entries; never extracts paths supplied by an archive. */
object ApkBundle {
    const val MAX_PARTS=128
    const val MAX_BYTES=4L*1024*1024*1024
    fun read(input:InputStream,checkCancelled:()->Unit={},write:(String,(OutputStream)->Unit)->Unit) {
        var total=0L;val names=mutableSetOf<String>()
        ZipInputStream(input).use{zip->
            while(true){
                checkCancelled();val entry=zip.nextEntry?:break;val name=entry.name
                require(!entry.isDirectory && name.matches(Regex("[a-zA-Z0-9_-][a-zA-Z0-9_.-]{0,119}\\.apk")) && names.add(name) && names.size<=MAX_PARTS){"This app bundle contains invalid files."}
                require(names.size!=1 || name=="base.apk"){"This app bundle must start with base.apk."}
                require(entry.size<=MAX_BYTES){"This app bundle is too large."}
                var part=0L
                write(name){output->
                    val buffer=ByteArray(256*1024)
                    while(true){checkCancelled();val count=zip.read(buffer);if(count<0)break;part+=count;total+=count;require(total<=MAX_BYTES){"This app bundle is too large."};output.write(buffer,0,count)}
                }
                require(part>0){"This app bundle contains an empty APK."};zip.closeEntry()
            }
        }
        require("base.apk" in names){"This app bundle has no base APK."}
    }
}
