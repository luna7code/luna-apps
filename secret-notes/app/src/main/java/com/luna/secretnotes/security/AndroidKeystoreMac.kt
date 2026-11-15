package com.luna.secretnotes.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

fun interface KeyedMac {
    fun authenticate(data: ByteArray): ByteArray
}

/**
 * Non-exportable HMAC-SHA256 key backed by Android Keystore.
 * The resulting MAC is returned to the app process; the key material is not.
 */
class AndroidKeystoreMac(
    private val alias: String
) : KeyedMac {
    override fun authenticate(data: ByteArray): ByteArray {
        val key = getOrCreateKey()
        return Mac.getInstance(HMAC_ALGORITHM, ANDROID_KEYSTORE).run {
            init(key)
            doFinal(data)
        }
    }

    fun hasKey(): Boolean = loadKeyStore().containsAlias(alias)

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
