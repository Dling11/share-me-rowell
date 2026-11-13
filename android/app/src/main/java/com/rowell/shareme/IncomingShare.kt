package com.rowell.shareme

import android.content.ClipData
import android.content.Intent
import android.net.Uri

/** Only file handles are accepted. Selection/receiver approval still happen in the app. */
object IncomingShare {
    @Suppress("DEPRECATION")
    fun files(intent:Intent):List<Uri> {
        if(intent.action !in setOf(Intent.ACTION_SEND,Intent.ACTION_SEND_MULTIPLE))return emptyList()
        val streams=if(intent.action==Intent.ACTION_SEND_MULTIPLE)
            intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM).orEmpty()
        else listOfNotNull(intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
        val uris=if(streams.isNotEmpty())streams else intent.clipData?.let{clip->
            require(clip.itemCount<=16){"Share up to 16 files at once."}
            (0 until clip.itemCount).mapNotNull{clip.getItemAt(it).uri}
        }.orEmpty()
        require(uris.isNotEmpty()){"Share a file, photo or video from the other app."}
        require(uris.size<=16){"Share up to 16 files at once."}
        require(uris.all{it.scheme=="content" && !it.authority.isNullOrBlank() && it.authority!="com.rowell.shareme.files"}){
            "This app didn't provide a shareable file. Try choosing it inside Share Mode."
        }
        return uris.distinct()
    }
    fun attach(intent:Intent,uris:List<Uri>,replace:Boolean=false) {
        intent.putStringArrayListExtra("uris",ArrayList(uris.map(Uri::toString)))
        intent.putExtra("replace_selection",replace)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.clipData=ClipData.newRawUri("Shared files",uris.first()).also{clip->uris.drop(1).forEach{clip.addItem(ClipData.Item(it))}}
    }
}
