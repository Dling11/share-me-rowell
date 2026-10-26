package com.rowell.shareme

import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.Inet4Address
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.security.MessageDigest
import java.text.Normalizer
import java.util.Base64

data class FileMeta(val id: String, val name: String, val size: Long, val sha: String) {
    fun json() = JSONObject().put("id", id).put("name", name).put("size", size).put("sha256", sha)
    companion object {
        fun parse(o: JSONObject): FileMeta {
            val result = FileMeta(o.getString("id"), safeName(o.getString("name")), o.getLong("size"), o.getString("sha256"))
            require(result.id.matches(Regex("[a-zA-Z0-9_-]{1,64}")) && result.size in 0..(1024L * 1024 * 1024 * 1024) && result.sha.matches(Regex("[a-f0-9]{64}"))) { "Invalid file details." }
            return result
        }
    }
}
fun safeName(raw: String): String {
    val cleaned = Normalizer.normalize(raw, Normalizer.Form.NFC).map { if (it.code < 32 || it in "/\\:*?\"<>|" || it in '\u202a'..'\u202e' || it in '\u2066'..'\u2069') '_' else it }.joinToString("").trim().trimEnd('.') .take(180)
    val name = cleaned.ifBlank { "Received file" }
    return if (name.substringBefore('.').uppercase().matches(Regex("CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]"))) "_$name" else name
}
fun sha256(input:InputStream):String=sha256(input,{})
fun sha256(input: InputStream,checkCancelled:()->Unit): String {
    val digest = MessageDigest.getInstance("SHA-256"); val buffer = ByteArray(256 * 1024)
    while (true) {checkCancelled(); val count = input.read(buffer); if (count < 0) break; digest.update(buffer, 0, count) }
    return hex(digest.digest())
}
fun hex(bytes: ByteArray) = bytes.joinToString("") { "%02x".format(it) }
fun verifyCertificate(encoded:ByteArray,expected:String){
    require(expected.matches(Regex("[a-f0-9]{64}")))
    check(hex(MessageDigest.getInstance("SHA-256").digest(encoded))==expected){"The device identity does not match this QR code. Scan it again."}
}
class SessionEpoch {
    private var value=0L
    @Synchronized fun next():Long=++value
    @Synchronized fun update(token:Long,action:()->Unit):Boolean{if(token!=value)return false;action();return true}
}
fun privateHost(host: String): Boolean = runCatching {
    if (!host.matches(Regex("[0-9.]{7,15}"))) return@runCatching false
    val address = InetAddress.getByName(host)
    address is Inet4Address && (address.isSiteLocalAddress || address.isLoopbackAddress || address.isLinkLocalAddress)
}.getOrDefault(false)

data class Pairing(val hosts: List<String>, val port: Int, val fingerprint: String, val code: String, val mode: String, val name: String, val ssid: String = "", val password: String = "") {
    fun text(): String {
        val json = JSONObject().put("v", 1).put("hosts", JSONArray(hosts)).put("port", port).put("fp", fingerprint).put("code", code).put("mode", mode).put("name", name)
        if (ssid.isNotEmpty()) json.put("ssid", ssid).put("password", password)
        return "shareme-rowell://pair/" + Base64.getUrlEncoder().withoutPadding().encodeToString(json.toString().toByteArray())
    }
    companion object {
        fun parse(text: String): Pairing {
            require(text.length <= 8192 && text.startsWith("shareme-rowell://pair/")) { "Scan a Share-Me-Rowell pairing code." }
            val o = JSONObject(String(Base64.getUrlDecoder().decode(text.substringAfter("shareme-rowell://pair/")), Charsets.UTF_8))
            require(o.getInt("v") == 1)
            val hosts = o.getJSONArray("hosts").let { a -> (0 until a.length()).map { a.getString(it) } }
            val result = Pairing(hosts, o.getInt("port"), o.getString("fp"), o.getString("code"), o.getString("mode"), o.optString("name", "Nearby device").take(40), o.optString("ssid"), o.optString("password"))
            require(hosts.size in 1..8 && hosts.all(::privateHost) && result.port in 1024..65535 && result.fingerprint.matches(Regex("[a-f0-9]{64}")) && result.code.matches(Regex("[a-zA-Z0-9_-]{32,64}")) && result.mode in setOf("send", "receive")) { "Invalid pairing code." }
            require(result.ssid.length <= 32 && result.password.length <= 63)
            return result
        }
    }
}

object Wire {
    const val HEADER_LIMIT = 65536
    fun read(input: InputStream): JSONObject {
        val bytes = ByteArrayOutputStream()
        while (bytes.size() <= HEADER_LIMIT) {
            val next = input.read(); if (next < 0) throw EOFException("The other device disconnected. Scan again to resume.")
            if (next == 10) {
                val decoder = Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                return JSONObject(decoder.decode(ByteBuffer.wrap(bytes.toByteArray())).toString())
            }
            bytes.write(next)
        }
        error("Connection header is too large.")
    }
    fun write(output: OutputStream, json: JSONObject) {
        val data = json.toString().toByteArray(); require(data.size <= HEADER_LIMIT)
        output.write(data); output.write(10); output.flush()
    }
    fun files(json: JSONObject): List<FileMeta> {
        val array = json.getJSONArray("files"); require(array.length() in 1..16) { "Choose between 1 and 16 files." }
        val files = (0 until array.length()).map { FileMeta.parse(array.getJSONObject(it)) }
        require(files.map { it.id }.distinct().size == files.size) { "Duplicate file IDs." }
        return files
    }
    fun success(json: JSONObject) { check(json.optBoolean("ok")) { json.optString("error", "The other device declined the transfer.") } }
    fun copy(input: InputStream, output: OutputStream, size: Long, progress: (Long) -> Unit = {}) {
        require(size >= 0); val buffer = ByteArray(256 * 1024); var done = 0L
        while (done < size) {
            val count = input.read(buffer, 0, minOf(buffer.size.toLong(), size - done).toInt())
            if (count < 0) throw EOFException("Transfer interrupted. Scan again to resume.")
            output.write(buffer, 0, count); done += count; progress(done)
        }
        output.flush()
    }
    fun skip(input: InputStream, count: Long) {
        var left = count; val buffer = ByteArray(64 * 1024)
        while (left > 0) {
            val skipped = input.skip(left)
            if (skipped > 0) left -= skipped
            else { val read = input.read(buffer, 0, minOf(left, buffer.size.toLong()).toInt()); if (read < 0) throw EOFException("The source file changed. Choose it again."); left -= read }
        }
    }
}
