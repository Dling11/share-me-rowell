package com.rowell.shareme

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.OpenableColumns
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.util.UUID

data class Prepared(val meta: FileMeta, val uri: Uri) {
    fun open(context: Context, offset: Long): InputStream {
        val input = context.contentResolver.openInputStream(uri) ?: error("The selected file is no longer available.")
        try { Wire.skip(input, offset); return input } catch(e: Exception) { input.close(); throw e }
    }
}
data class History(val direction: String, val name: String, val size: Long, val uri: String, val time: Long)

class ReceiveStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("transfers", Context.MODE_PRIVATE)
    private val resolver = context.contentResolver
    private fun key(file: FileMeta) = "part-${file.sha}-${file.size}"
    fun offset(file: FileMeta): Long {
        val value = prefs.getString(key(file), null) ?: return 0
        return runCatching { resolver.openFileDescriptor(Uri.parse(value), "r")!!.use { it.statSize.coerceAtLeast(0) } }.getOrElse { prefs.edit().remove(key(file)).commit(); 0 }.coerceAtMost(file.size)
    }
    private fun part(file: FileMeta): Uri {
        prefs.getString(key(file), null)?.let { saved ->
            val uri=Uri.parse(saved)
            if(runCatching {checkNotNull(resolver.openFileDescriptor(uri,"r")).close()}.isSuccess) return uri
            prefs.edit().remove(key(file)).commit()
        }
        val values=ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME,file.name)
            put(MediaStore.Downloads.MIME_TYPE, if(file.name.endsWith(".apk",true)) "application/vnd.android.package-archive" else "application/octet-stream")
            put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/SHARE-ME-ROWELL")
            put(MediaStore.Downloads.IS_PENDING,1)
        }
        val uri=resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,values) ?: error("Could not create the received file. Check free storage.")
        check(prefs.edit().putString(key(file),uri.toString()).commit())
        return uri
    }
    fun receive(file: FileMeta, offset: Long, input: InputStream, verifying:()->Unit,checkCancelled:()->Unit={},progress: (Long)->Unit): Uri {
        checkCancelled()
        val uri=part(file)
        val descriptor=checkNotNull(resolver.openFileDescriptor(uri,"rw"))
        ParcelFileDescriptor.AutoCloseOutputStream(descriptor).use { output ->
                require(offset<=output.channel.size()) {"The partial file changed. Retry the transfer."}
                output.channel.truncate(offset);output.channel.position(offset)
                Wire.copy(input,output,file.size-offset){checkCancelled();progress(offset+it)}
                output.fd.sync()
        }
        verifying()
        val digest=resolver.openInputStream(uri)!!.use{sha256(it,checkCancelled)}
        if(digest!=file.sha) {resolver.delete(uri,null,null);prefs.edit().remove(key(file)).commit();error("Verification failed. Choose the source file again.")}
        checkCancelled();check(resolver.update(uri,ContentValues().apply{put(MediaStore.Downloads.IS_PENDING,0)},null,null)>0)
        prefs.edit().remove(key(file)).commit()
        val shown=resolver.query(uri,arrayOf(MediaStore.Downloads.DISPLAY_NAME),null,null,null)?.use{if(it.moveToFirst())it.getString(0)else null}?:file.name
        addHistory("Received",shown,file.size,uri.toString())
        return uri
    }
    @Synchronized fun addHistory(direction:String,name:String,size:Long,uri:String) {
        val old=history();val list=listOf(History(direction,name,size,uri,System.currentTimeMillis()))+old
        val json=JSONArray();list.take(50).forEach {json.put(JSONObject().put("direction",it.direction).put("name",it.name).put("size",it.size).put("uri",it.uri).put("time",it.time))}
        prefs.edit().putString("history",json.toString()).commit()
        Model.update(history=history())
    }
    fun history():List<History> = runCatching {
        val a=JSONArray(prefs.getString("history","[]"));(0 until a.length()).map {index->a.getJSONObject(index).let{History(it.getString("direction"),it.getString("name"),it.getLong("size"),it.getString("uri"),it.getLong("time"))}}
    }.getOrDefault(emptyList())
    fun clearIncomplete() {
        prefs.all.filterKeys{it.startsWith("part-")}.forEach{(key,value)->runCatching {resolver.delete(Uri.parse(value as String),null,null)};prefs.edit().remove(key).commit()}
    }
    companion object {
        fun prepare(context: Context, uri: Uri, checkCancelled:()->Unit={},progress:(String,Long,Long)->Unit={_,_,_->}): Prepared {
            val details=context.contentResolver.query(uri,arrayOf(OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE),null,null,null)?.use{cursor->if(cursor.moveToFirst())Pair(cursor.getString(0)?:"Shared file",if(cursor.isNull(1))-1L else cursor.getLong(1))else null}?:Pair("Shared file",-1L)
            val name=details.first;var updated=0L;progress(name,0,details.second)
            var size=0L;val digest=java.security.MessageDigest.getInstance("SHA-256")
            context.contentResolver.openInputStream(uri)?.use{input->val buffer=ByteArray(256*1024);while(true){checkCancelled();val count=input.read(buffer);if(count<0)break;size+=count;require(size<=1024L*1024*1024*1024){"This file is too large."};digest.update(buffer,0,count);val now=System.currentTimeMillis();if(now-updated>=150){updated=now;progress(name,size,details.second)}}} ?: error("Could not read the selected file.")
            checkCancelled();progress(name,size,size)
            return Prepared(FileMeta(UUID.randomUUID().toString().replace("-",""),safeName(name),size,hex(digest.digest())),uri)
        }
    }
}
