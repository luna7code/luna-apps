package com.luna.secretnotes.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

interface KeyedMac {
    fun authenticate(data: ByteArray): ByteArray

    /** Existing keys must be used for unlock; silently recreating one would brick the vault. */
    fun authenticateExisting(data: ByteArray): ByteArray = authenticate(data)
}

/**
 * Non-exportable HMAC-SHA256 key backed by Android Keystore.
 * Creation and use are separate so unlock never silently replaces a missing key.
 */
class AndroidKeystoreMac(
    private val alias: String
) : KeyedMac {
    override fun authenticate(data: ByteArray): ByteArray =
        authenticateWithKey(getOrCreateKey(), data)

    override fun authenticateExisting(data: ByteArray): ByteArray {
        val keyStore = loadKeyStore()
        val key = keyStore.getKey(alias, null) as? SecretKey
            ?: throw KeyUnavailableException("Required Android Keystore key is missing")
        return authenticateWithKey(key, data)
    }

    fun hasKey(): Boolean = loadKeyStore().containsAlias(alias)

    private fun authenticateWithKey(key: SecretKey, data: ByteArray): ByteArray =
        Mac.getInstance(HMAC_ALGORITHM, ANDROID_KEYSTORE).run {
            init(key)
            doFinal(data)
        }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = loadKeyStore()
        val existing = keyStore.getKey(alias, null) as? SecretKey
        if (existing != null) return existing

        val generator = KeyGenerator.getInstance(HMAC_ALGORITHM, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setKeySize(256)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .build()
        )
        return generator.generateKey()
    }

    private fun loadKeyStore(): KeyStore =
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val HMAC_ALGORITHM = "HmacSHA256"
    }
}

class KeyUnavailableException(message: String) : SecurityException(message)
