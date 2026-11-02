package com.rowell.shareme

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class InstalledApp(val packageName:String,val name:String,val version:String,val bytes:Long,val split:Boolean)

@Suppress("DEPRECATION")
object InstalledApps {
    fun list(context:Context):List<InstalledApp> {
        val manager=context.packageManager
        val launcher=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return manager.queryIntentActivities(launcher,0).map{it.activityInfo.applicationInfo}.distinctBy{it.packageName}.mapNotNull{app->runCatching{
            val info=manager.getPackageInfo(app.packageName,0)
            val files=listOf(app.sourceDir)+app.splitSourceDirs.orEmpty()
            InstalledApp(app.packageName,profileName(manager.getApplicationLabel(app).toString()),info.versionName.orEmpty().take(40),files.sumOf{File(it).length()},!app.splitSourceDirs.isNullOrEmpty())
        }.getOrNull()}.sortedBy{it.name.lowercase()}
    }
    fun export(context:Context,packageName:String,checkCancelled:()->Unit):Uri {
        val manager=context.packageManager
        val app=manager.getApplicationInfo(packageName,0)
        val files=listOf(File(app.sourceDir))+app.splitSourceDirs.orEmpty().map(::File)
        require(files.size<=ApkBundle.MAX_PARTS && files.sumOf{it.length()}<=ApkBundle.MAX_BYTES){"This app is too large to export."}
        val folder=File(context.cacheDir,"installer/${UUID.randomUUID()}").apply{check(mkdirs())}
        val extension=if(files.size==1)"apk" else "apks"
        val destination=File(folder,"${safeName(manager.getApplicationLabel(app).toString()).take(90)}.$extension")
        try {
            if(files.size==1)files[0].inputStream().use{input->destination.outputStream().use{output->val buffer=ByteArray(256*1024);while(true){checkCancelled();val count=input.read(buffer);if(count<0)break;output.write(buffer,0,count)}}}
            else ZipOutputStream(destination.outputStream().buffered()).use{zip->
                zip.setLevel(0) // APKs are already compressed; save CPU and preparation time.
                files.forEachIndexed{index,file->
                    checkCancelled();zip.putNextEntry(ZipEntry(if(index==0)"base.apk" else "split-$index.apk"))
                    file.inputStream().use{input->val buffer=ByteArray(256*1024);while(true){checkCancelled();val count=input.read(buffer);if(count<0)break;zip.write(buffer,0,count)}}
                    zip.closeEntry()
                }
            }
            return FileProvider.getUriForFile(context,"${context.packageName}.files",destination)
        }catch(e:Exception){destination.delete();folder.delete();throw e}
    }
    fun installBundle(context:Context,uri:Uri,checkCancelled:()->Unit={}) {
        val installer=context.packageManager.packageInstaller
        val parameters=PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        if(Build.VERSION.SDK_INT>=31)parameters.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED)
        val id=installer.createSession(parameters)
        try {
            installer.openSession(id).use{session->
                val input=context.contentResolver.openInputStream(uri)?:error("This app bundle is no longer available.")
                ApkBundle.read(input,checkCancelled){name,copy->session.openWrite(name,0,-1).use{output->copy(output);session.fsync(output)}}
                val callback=PendingIntent.getBroadcast(context,id,Intent(context,InstallResultReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
                session.commit(callback.intentSender)
            }
        }catch(e:Exception){runCatching{installer.abandonSession(id)};throw e}
    }
}

class InstallResultReceiver:BroadcastReceiver() {
    @Suppress("DEPRECATION")
    override fun onReceive(context:Context,intent:Intent) {
        when(intent.getIntExtra(PackageInstaller.EXTRA_STATUS,PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION->{
                val confirmation=if(Build.VERSION.SDK_INT>=33)intent.getParcelableExtra(Intent.EXTRA_INTENT,Intent::class.java) else intent.getParcelableExtra(Intent.EXTRA_INTENT)
                if(confirmation!=null)runCatching{context.startActivity(confirmation.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))}.onFailure{Model.update(status="Open the received app bundle again to install it.")}
            }
            PackageInstaller.STATUS_SUCCESS->Model.update(status="App installed.")
            else->Model.update(status="Android couldn't install this app. It may need a different device version or installation from the store.")
        }
    }
}
