package com.rowell.shareme

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.OpenableColumns
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import java.io.ByteArrayInputStream
import java.io.File

@RunWith(RobolectricTestRunner::class) @Config(sdk=[35])
class StorageResumeTest {
    private val context get()=RuntimeEnvironment.getApplication()
    private lateinit var provider:DownloadsFixture
    private val bytes=ByteArray(1024*1024+19){(it%251).toByte()}
    private fun meta(id:String="one")=FileMeta(id,"resume.bin",bytes.size.toLong(),sha256(ByteArrayInputStream(bytes)))
    @Before fun setUp(){
        context.getSharedPreferences("transfers",0).edit().clear().commit()
        context.deleteDatabase("adventurer.db");Model.set{ScreenState()}
        provider=DownloadsFixture(File(context.cacheDir,"resume-fixture-${System.nanoTime()}").apply{mkdirs()})
        provider.attachInfo(context,ProviderInfo().apply{authority="media";exported=true})
        ShadowContentResolver.registerProviderInternal("media",provider)
    }
    @Test fun interruptedFileResumesAtItsStoredOffsetAfterStoreRecreation(){
        val file=meta();val first=ReceiveStore(context)
        assertThrows(Exception::class.java){first.receive(file,0,ByteArrayInputStream(bytes.copyOfRange(0,19001)),{},progress={})}
        assertEquals(0L,JourneyStore.load(context).xp)
        val reopened=ReceiveStore(context);val offset=reopened.offset(file)
        assertEquals(19001L,offset)
        val uri=reopened.receive(file,offset,ByteArrayInputStream(bytes.copyOfRange(offset.toInt(),bytes.size)),{},progress={})
        assertArrayEquals(bytes,provider.file(uri).readBytes())
        assertEquals(1,provider.files.size)
        assertEquals(1,reopened.history().size)
        assertEquals(Adventure.fileXp(file.size).toLong(),JourneyStore.load(context).xp)
    }
    @Test fun retryKeepsFinishedFilesButAnExplicitNewSelectionCanSendAnotherCopy(){
        val file=meta();val store=ReceiveStore(context)
        val uri=store.receive(file,0,ByteArrayInputStream(bytes),{},progress={})
        val retry=ReceiveStore(context)
        assertEquals(file.size,retry.offset(file))
        assertEquals(uri,retry.receive(file,file.size,ByteArrayInputStream(byteArrayOf()),{},progress={}))
        assertEquals(1,provider.files.size);assertEquals(1,retry.history().size)
        val newSelection=meta("another-selection")
        assertEquals(0L,retry.offset(newSelection))
        retry.receive(newSelection,0,ByteArrayInputStream(bytes),{},progress={})
        assertEquals(2,provider.files.size)
        assertEquals(Adventure.fileXp(file.size).toLong(),JourneyStore.load(context).xp)
    }
    @Test fun corruptedOrCancelledPayloadsNeverEarnXp(){
        val file=meta();val store=ReceiveStore(context)
        assertThrows(IllegalStateException::class.java){store.receive(file,0,ByteArrayInputStream(ByteArray(bytes.size){7}),{},progress={})}
        assertEquals(0L,JourneyStore.load(context).xp)
        assertThrows(IllegalStateException::class.java){store.receive(file,0,ByteArrayInputStream(bytes),{},checkCancelled={error("Cancelled")},progress={})}
        assertEquals(0L,JourneyStore.load(context).xp)
    }
    @Test fun changedCompletedFileCannotPassVerificationOrBeOverwrittenByRetry(){
        val file=meta();val store=ReceiveStore(context)
        val uri=store.receive(file,0,ByteArrayInputStream(bytes),{},progress={})
        provider.file(uri).writeBytes(ByteArray(bytes.size){7})
        assertThrows(IllegalStateException::class.java){store.receive(file,file.size,ByteArrayInputStream(byteArrayOf()),{},progress={})}
        assertEquals(0L,store.offset(file))
        assertEquals(1,provider.files.size)
    }
}

private class DownloadsFixture(private val directory:File):ContentProvider(){
    val files=linkedMapOf<String,File>()
    private val names=mutableMapOf<String,String>()
    fun file(uri:Uri)=files.getValue(uri.lastPathSegment!!)
    override fun onCreate()=true
    override fun insert(uri:Uri,values:ContentValues?):Uri {
        val id=(files.size+1).toString();files[id]=File(directory,id).apply{createNewFile()}
        names[id]=values?.getAsString(MediaStore.Downloads.DISPLAY_NAME)?:"file.bin"
        return Uri.parse("content://media/external/downloads/$id")
    }
    override fun openFile(uri:Uri,mode:String):ParcelFileDescriptor=ParcelFileDescriptor.open(file(uri),ParcelFileDescriptor.parseMode(mode))
    override fun query(uri:Uri,projection:Array<out String>?,selection:String?,selectionArgs:Array<out String>?,sortOrder:String?):Cursor {
        val columns=projection?:arrayOf(OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE)
        return MatrixCursor(columns).apply{addRow(columns.map{if(it==OpenableColumns.DISPLAY_NAME)names[uri.lastPathSegment] else file(uri).length()}.toTypedArray())}
    }
    override fun update(uri:Uri,values:ContentValues?,selection:String?,selectionArgs:Array<out String>?)=1
    override fun delete(uri:Uri,selection:String?,selectionArgs:Array<out String>?):Int {file(uri).delete();files.remove(uri.lastPathSegment);return 1}
    override fun getType(uri:Uri)="application/octet-stream"
}
