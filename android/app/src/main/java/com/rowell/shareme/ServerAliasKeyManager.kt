package com.rowell.shareme

import java.net.Socket
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedKeyManager
import javax.net.ssl.X509KeyManager

/** An old keystore entry must never win alias selection after the TLS key migration. */
class ServerAliasKeyManager(private val delegate:X509KeyManager,private val selectedAlias:String):X509ExtendedKeyManager(){
    override fun getServerAliases(keyType:String?,issuers:Array<Principal>?):Array<String>? =
        delegate.getServerAliases(keyType,issuers)?.takeIf{selectedAlias in it}?.let{arrayOf(selectedAlias)}
    override fun chooseServerAlias(keyType:String?,issuers:Array<Principal>?,socket:Socket?):String? =
        getServerAliases(keyType,issuers)?.firstOrNull()
    override fun chooseEngineServerAlias(keyType:String?,issuers:Array<Principal>?,engine:SSLEngine?):String? =
        chooseServerAlias(keyType,issuers,null)
    override fun getCertificateChain(alias:String?):Array<X509Certificate>? =
        if(alias==selectedAlias)delegate.getCertificateChain(alias)else null
    override fun getPrivateKey(alias:String?):PrivateKey? =
        if(alias==selectedAlias)delegate.getPrivateKey(alias)else null
    override fun getClientAliases(keyType:String?,issuers:Array<Principal>?):Array<String>? = null
    override fun chooseClientAlias(keyTypes:Array<String>?,issuers:Array<Principal>?,socket:Socket?):String? = null
    override fun chooseEngineClientAlias(keyTypes:Array<String>?,issuers:Array<Principal>?,engine:SSLEngine?):String? = null
}
