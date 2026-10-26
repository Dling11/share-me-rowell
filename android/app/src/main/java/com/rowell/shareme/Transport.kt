package com.rowell.shareme

import android.content.Context
import android.net.Network
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.math.BigInteger
import java.net.*
import java.security.*
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.net.ssl.*
import javax.security.auth.x500.X500Principal

class Transport(private val context:Context) : Closeable {
    private val executor=Executors.newFixedThreadPool(3)
    private val connections=Collections.synchronizedSet(mutableSetOf<Socket>())
    private val store=ReceiveStore(context)
    private var listener:SSLServerSocket?=null
    @Volatile private var stopped=false
    private val epoch=Model.epochs.next()
    private fun state(change:(ScreenState)->ScreenState){Model.epochs.update(epoch){Model.set(change)}}
    private fun status(message:String){state{it.copy(status=message)}}
    private fun phase(next:ConnectionPhase){state{it.copy(connection=it.connection.advance(next))}}
    private var claimed=false
    private val guard=Any()
    private var session:Pairing?=null
    private var expires=0L
    private var sources=emptyList<Prepared>()
    private val identity:KeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
            if(!containsAlias("shareme-tls"))KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA,"AndroidKeyStore").apply {
                initialize(KeyGenParameterSpec.Builder("shareme-tls",KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY)
                    .setKeySize(2048).setDigests(KeyProperties.DIGEST_SHA256,KeyProperties.DIGEST_SHA512)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    .setCertificateSubject(X500Principal("CN=Share-Me-Rowell"))
                    .setCertificateSerialNumber(BigInteger(64,SecureRandom()).add(BigInteger.ONE))
                    .setCertificateNotBefore(Date(System.currentTimeMillis()-86400000))
                    .setCertificateNotAfter(Date(System.currentTimeMillis()+5L*365*86400000)).build())
                generateKeyPair()
            }
        }
    }
    fun start(mode:String,files:List<Prepared>) {
        check(!stopped)
        require(mode=="receive"||files.isNotEmpty()) {"Choose files to send first."}
        sources=files
        val managers=KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply{init(identity,null)}
        val ssl=SSLContext.getInstance("TLS").apply{init(managers.keyManagers,null,SecureRandom())}
        val server=ssl.serverSocketFactory.createServerSocket(0) as SSLServerSocket
        server.enabledProtocols=arrayOf("TLSv1.2");synchronized(guard){if(stopped){server.close();return};listener=server}
        val fp=hex(MessageDigest.getInstance("SHA-256").digest(identity.getCertificate("shareme-tls").encoded))
        val secret=ByteArray(32).also{SecureRandom().nextBytes(it)}
        val pair=Pairing(addresses(),server.localPort,fp,Base64.getUrlEncoder().withoutPadding().encodeToString(secret),mode,Model.deviceName,Model.value.ssid,Model.value.password)
        session=pair;expires=System.currentTimeMillis()+10*60*1000
        state{it.copy(mode=mode,pairing=pair,busy=false,status=if(mode=="send")"Ready. Scan this code from Receive on the other phone." else "Ready. Scan this code from Send on the other device.")}
        Thread({while(!stopped){try{val socket=server.accept() as SSLSocket;val accepted=synchronized(connections){if(stopped||connections.size>=3)false else{connections+=socket;true}};if(!accepted){socket.close();continue};executor.execute{handle(socket)}}catch(_:Exception){if(!stopped)status("Receiver stopped. Tap Start to try again.")}}},"shareme-listener").apply{isDaemon=true;start()}
    }
    private fun handle(socket:SSLSocket) {
        var owner=false
        try {
            if(!privateHost(socket.inetAddress.hostAddress.orEmpty()))return
            socket.soTimeout=15000;socket.startHandshake()
            val input=BufferedInputStream(socket.inputStream,256*1024);val output=BufferedOutputStream(socket.outputStream,256*1024)
            val request=Wire.read(input);val pair=session?:return
            if(request.optInt("v")!=1||System.currentTimeMillis()>expires||!MessageDigest.isEqual(request.optString("code").toByteArray(),pair.code.toByteArray())) {Wire.write(output,reject("Pairing expired. Scan the current QR code."));return}
            synchronized(guard){if(claimed){Wire.write(output,reject("This device is busy."));return};claimed=true;owner=true}
            socket.soTimeout=600000
            val op=request.optString("op")
            val peer=request.optString("name","Nearby device").take(40)
            state{it.copy(connection=PeerConnection.begin(peer,ConnectionPhase.APPROVAL),current="",done=0,total=0)}
            if(op=="push"&&pair.mode=="receive"){
                val files=Wire.files(request)
                if(!approve(peer,files,false)){phase(ConnectionPhase.DECLINED);status("Transfer declined.");Wire.write(output,reject("Transfer declined."));return}
                receive(input,output,files)
            }else if(op=="pull"&&pair.mode=="send"){
                if(!approve(peer,sources.map{it.meta},true)){phase(ConnectionPhase.DECLINED);status("Transfer declined.");Wire.write(output,reject("Transfer declined."));return}
                Wire.write(output,JSONObject().put("ok",true).put("sender",Model.deviceName).put("files",JSONArray(sources.map{it.meta.json()})))
                phase(ConnectionPhase.WAITING);val response=Wire.read(input);Wire.success(response);send(input,output,sources,response.getJSONArray("offsets"))
            }else{phase(ConnectionPhase.FAILED);Wire.write(output,reject("Choose Send on one device and Receive on the other."))}
        }catch(e:Exception){if(!stopped&&owner){phase(if(e.message.orEmpty().contains("declined",true))ConnectionPhase.DECLINED else ConnectionPhase.FAILED);status(e.message?:"Connection interrupted. Scan again to resume.")}}
        finally{socket.close();connections.remove(socket);if(owner){synchronized(guard){claimed=false};state{it.copy(busy=false,approval=null)}}}
    }
    fun connect(pair:Pairing,files:List<Prepared>,network:Network?=null,sameSubnet:Boolean?=null) {
        if(stopped)return
        synchronized(guard){if(claimed)return;claimed=true}
        try{executor.execute {
            var socket:SSLSocket?=null
            var rawClient:Socket?=null
            try{
                state{it.copy(busy=true,status="Connecting to ${pair.name}…",connection=PeerConnection.begin(pair.name),current="",done=0,total=0)}
                val trust=object:X509TrustManager {
                    override fun getAcceptedIssuers():Array<X509Certificate> = emptyArray()
                    override fun checkClientTrusted(chain:Array<X509Certificate>,authType:String):Unit = throw java.security.cert.CertificateException("Client certificates are not used.")
                    override fun checkServerTrusted(chain:Array<X509Certificate>,authType:String){check(chain.isNotEmpty());verifyCertificate(chain[0].encoded,pair.fingerprint);chain[0].checkValidity()}
                }
                val ssl=SSLContext.getInstance("TLS").apply{init(null,arrayOf(trust),SecureRandom())}
                var last:Exception?=null
                for((index,host) in pair.hosts.withIndex()){
                    check(!stopped){"Sharing stopped."};status("Connecting to ${pair.name} · address ${index+1} of ${pair.hosts.size}…")
                    try{val raw=network?.socketFactory?.createSocket()?:Socket();rawClient=raw;synchronized(connections){check(!stopped);connections+=raw};var candidate:SSLSocket?=null
                        try{raw.connect(InetSocketAddress(host,pair.port),7000);candidate=ssl.socketFactory.createSocket(raw,host,pair.port,true) as SSLSocket;candidate.enabledProtocols=arrayOf("TLSv1.2");candidate.soTimeout=600000;candidate.startHandshake();socket=candidate;break}
                        catch(e:Exception){connections.remove(raw);runCatching{candidate?.close()};raw.close();throw e}
                    }catch(e:Exception){if(last !is SSLException||e is SSLException)last=e}
                }
                val channel=socket?:throw(last?:ConnectException("The device could not be reached."))
                connections+=channel
                val input=BufferedInputStream(channel.inputStream,256*1024);val output=BufferedOutputStream(channel.outputStream,256*1024)
                phase(ConnectionPhase.WAITING)
                if(pair.mode=="receive"){
                    check(files.isNotEmpty()){ "Choose files in Send first." }
                    Wire.write(output,JSONObject().put("v",1).put("op","push").put("code",pair.code).put("name",Model.deviceName).put("files",JSONArray(files.map{it.meta.json()})))
                    status("Waiting for approval on ${pair.name}…")
                    val response=Wire.read(input);Wire.success(response);send(input,output,files,response.getJSONArray("offsets"))
                }else{
                    Wire.write(output,JSONObject().put("v",1).put("op","pull").put("code",pair.code).put("name",Model.deviceName))
                    status("Waiting for ${pair.name}…")
                    val offer=Wire.read(input);Wire.success(offer);val incoming=Wire.files(offer)
                    if(!approve(pair.name,incoming,false)){phase(ConnectionPhase.DECLINED);status("Transfer declined.");Wire.write(output,reject("Transfer declined."));return@execute}
                    receive(input,output,incoming)
                }
            }catch(e:Exception){if(!stopped){phase(if(e.message.orEmpty().contains("declined",true))ConnectionPhase.DECLINED else ConnectionPhase.FAILED);status(ConnectionProblem.describe(e,pair.hosts.firstOrNull().orEmpty(),sameSubnet))}}
            finally{socket?.let{connections.remove(it);runCatching{it.close()}};rawClient?.let{connections.remove(it);runCatching{it.close()}};synchronized(guard){claimed=false};state{it.copy(busy=false,approval=null)}}
        }}catch(_:java.util.concurrent.RejectedExecutionException){synchronized(guard){claimed=false}}
    }
    private fun approve(peer:String,files:List<FileMeta>,sending:Boolean):Boolean {
        val approval=Approval(peer,files,sending)
        state{it.copy(approval=approval,busy=true,status="Waiting for your approval…",connection=it.connection.advance(ConnectionPhase.APPROVAL))}
        return try{approval.answer.get(90,TimeUnit.SECONDS)}finally{state{it.copy(approval=null)}}
    }
    private fun send(input:InputStream,output:OutputStream,files:List<Prepared>,offsets:JSONArray){
        require(offsets.length()==files.size)
        files.forEachIndexed{index,source->
            val offset=offsets.getLong(index);require(offset in 0..source.meta.size)
            state{it.copy(busy=true,current=source.meta.name,status="Sending file ${index+1} of ${files.size}…",done=offset,total=source.meta.size,fileIndex=index+1,fileCount=files.size,connection=it.connection.advance(ConnectionPhase.SENDING))}
            var updated=0L
            source.open(context,offset).use{Wire.copy(it,output,source.meta.size-offset){done->if(System.currentTimeMillis()-updated>120||offset+done==source.meta.size){updated=System.currentTimeMillis();state{s->s.copy(done=offset+done)}}}}
            status("Waiting for file verification…")
            phase(ConnectionPhase.VERIFYING)
            Wire.success(Wire.read(input));store.addHistory("Sent",source.meta.name,source.meta.size,source.uri.toString())
        }
        phase(ConnectionPhase.FINISHED);status("Transfer complete. Files verified.")
    }
    private fun receive(input:InputStream,output:OutputStream,files:List<FileMeta>){
        val used=mutableSetOf<String>()
        val offsets=files.map{if(used.add(it.sha+it.size))store.offset(it) else 0L}
        Wire.write(output,JSONObject().put("ok",true).put("offsets",JSONArray(offsets)))
        files.forEachIndexed{index,file->
            state{it.copy(busy=true,current=file.name,status="Receiving file ${index+1} of ${files.size}…",done=offsets[index],total=file.size,fileIndex=index+1,fileCount=files.size,connection=it.connection.advance(ConnectionPhase.RECEIVING))}
            var updated=0L
            try {store.receive(file,offsets[index],input,{phase(ConnectionPhase.VERIFYING);status("Verifying ${file.name}…")},checkCancelled={check(!stopped){"Disconnected."}}){done->if(System.currentTimeMillis()-updated>120||done==file.size){updated=System.currentTimeMillis();state{it.copy(done=done)}}};Wire.write(output,JSONObject().put("ok",true).put("index",index))}
            catch(e:Exception){runCatching{Wire.write(output,reject(e.message?:"The file could not be received."))};throw e}
        }
        phase(ConnectionPhase.FINISHED);status("Transfer complete. Files saved in Downloads/SHARE-ME-ROWELL.")
    }
    private fun reject(message:String)=JSONObject().put("ok",false).put("error",message)
    override fun close(){synchronized(guard){stopped=true;listener?.close()};Model.value.approval?.answer?.complete(false);synchronized(connections){connections.toList().forEach{runCatching{it.close()}};connections.clear()};executor.shutdownNow()}
    companion object {
        fun addresses():List<String> = NetworkInterface.getNetworkInterfaces().toList().sortedBy{if(it.name.startsWith("ap")||it.name.contains("swlan"))0 else 1}.flatMap{it.inetAddresses.toList()}.filterIsInstance<Inet4Address>().mapNotNull{it.hostAddress}.filter{privateHost(it)&&!it.startsWith("127.")}.distinct().take(8).ifEmpty{listOf("127.0.0.1")}
    }
}
