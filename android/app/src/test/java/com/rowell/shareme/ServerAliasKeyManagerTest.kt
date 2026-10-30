package com.rowell.shareme

import org.junit.Assert.*
import org.junit.Test
import java.net.Socket
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.net.ssl.X509KeyManager

class ServerAliasKeyManagerTest{
    private val migrated="shareme-tls-v2"
    private val key=object:PrivateKey{
        override fun getAlgorithm()="RSA"
        override fun getFormat():String?=null
        override fun getEncoded():ByteArray?=null // Android's private key is non-exportable.
    }
    private fun manager(aliases:Array<String> = arrayOf("shareme-tls",migrated))=ServerAliasKeyManager(object:X509KeyManager{
        override fun getServerAliases(keyType:String?,issuers:Array<Principal>?)=if(keyType=="RSA")aliases else null
        override fun chooseServerAlias(keyType:String?,issuers:Array<Principal>?,socket:Socket?)=aliases.firstOrNull()
        override fun getCertificateChain(alias:String?)=emptyArray<X509Certificate>()
        override fun getPrivateKey(alias:String?)=key
        override fun getClientAliases(keyType:String?,issuers:Array<Principal>?)=aliases
        override fun chooseClientAlias(keyTypes:Array<String>?,issuers:Array<Principal>?,socket:Socket?)=aliases.firstOrNull()
    },migrated)
    @Test fun migratedIdentityWinsWhenDelegatePrefersTheOldKey(){
        val manager=manager()
        assertEquals(migrated,manager.chooseServerAlias("RSA",null,null))
        assertEquals(migrated,manager.chooseEngineServerAlias("RSA",null,null))
        assertArrayEquals(arrayOf(migrated),manager.getServerAliases("RSA",null))
        assertSame(key,manager.getPrivateKey(migrated))
    }
    @Test fun oldAndUnrelatedKeysCannotBeReturnedToTls(){
        val selected=manager()
        assertNull(selected.getPrivateKey("shareme-tls"))
        assertNull(selected.getCertificateChain("shareme-tls"))
        assertNull(selected.chooseServerAlias("EC",null,null))
        assertNull(manager(arrayOf("shareme-tls")).chooseServerAlias("RSA",null,null))
    }
    @Test fun serverIdentityIsNotOfferedForClientAuthentication(){
        val manager=manager()
        assertNull(manager.getClientAliases("RSA",null))
        assertNull(manager.chooseClientAlias(arrayOf("RSA"),null,null))
        assertNull(manager.chooseEngineClientAlias(arrayOf("RSA"),null,null))
    }
}
