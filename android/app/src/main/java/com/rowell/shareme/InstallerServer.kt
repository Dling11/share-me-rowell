package com.rowell.shareme

import java.io.Closeable
import java.io.File
import java.net.ServerSocket
import java.net.Socket
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.Executors

/** Only serves this app's signed, public installer, never arbitrary files or received data. */
class InstallerServer(private val apk:File):Closeable {
    private val server=ServerSocket(0,3)
    private val workers=Executors.newFixedThreadPool(2)
    private val connections=mutableSetOf<Socket>()
    private val token=Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(32).also{SecureRandom().nextBytes(it)})
    private val expires=System.currentTimeMillis()+30*60*1000
    @Volatile private var stopped=false
    init{Thread({while(!stopped)try{val client=server.accept();val accepted=synchronized(connections){if(stopped||connections.size>=3)false else{connections+=client;true}};if(!accepted){client.close();continue};try{workers.execute{try{handle(client)}finally{synchronized(connections){connections.remove(client)}}}}catch(_:Exception){synchronized(connections){connections.remove(client)};client.close()}}catch(_:Exception){}},"shareme-installer").apply{isDaemon=true;start()}}
    fun url(host:String)="http://$host:${server.localPort}/$token"
    private fun handle(socket:Socket){socket.use{runCatching{
        if(System.currentTimeMillis()>expires||!privateHost(it.inetAddress.hostAddress.orEmpty()))return
        it.soTimeout=5000
        val input=it.getInputStream();val line=StringBuilder()
        while(line.length<2048){val c=input.read();if(c<0)return;if(c==10)break;if(c!=13)line.append(c.toChar())}
        var headerLength=0;var headerLine=0;var headersComplete=false
        while(headerLength++<16384){val c=input.read();if(c<0)return;if(c==10){if(headerLine==0){headersComplete=true;break};headerLine=0}else if(c!=13)headerLine++};if(!headersComplete)return
        val request=line.toString();val path=request.split(' ').getOrNull(1)
        val output=it.getOutputStream()
        if(!request.startsWith("GET ") || path!in setOf("/$token","/$token/app.apk")){output.write("HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".toByteArray());return}
        if(path!!.endsWith("/app.apk")){
            output.write(("HTTP/1.1 200 OK\r\nContent-Type: application/vnd.android.package-archive\r\nContent-Disposition: attachment; filename=Share-Mode.apk\r\nContent-Length: ${apk.length()}\r\nX-Content-Type-Options: nosniff\r\nConnection: close\r\n\r\n").toByteArray())
            apk.inputStream().use{source->Wire.copy(source,output,apk.length())}
        }else{
            val html="""<!doctype html><meta name="viewport" content="width=device-width,initial-scale=1"><title>Share Mode</title><body style="background:#0c131e;color:white;font:18px system-ui;padding:28px"><h1>Share Mode</h1><p>Get the Android app to share files over local Wi-Fi or a hotspot.</p><p><a style="display:inline-block;background:#70e2c2;color:#0c131e;padding:18px;border-radius:12px" href="/$token/app.apk">Download Android APK</a></p><p>Tap the downloaded APK and follow Android's installation prompt. No account is needed.</p></body>""".toByteArray()
            output.write("HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: ${html.size}\r\nCache-Control: no-store\r\nContent-Security-Policy: default-src 'none'; style-src 'unsafe-inline'\r\nConnection: close\r\n\r\n".toByteArray());output.write(html)
        }
    }}}
    override fun close(){stopped=true;server.close();synchronized(connections){connections.forEach{runCatching{it.close()}};connections.clear()};workers.shutdownNow()}
}
