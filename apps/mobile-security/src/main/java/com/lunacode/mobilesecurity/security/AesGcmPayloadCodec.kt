package com.lunacode.mobilesecurity.security

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Android-independent AES-GCM framing; production keys stay in Android Keystore. */
object AesGcmPayloadCodec {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128
    private const val TAG_BYTES = TAG_BITS / 8

    fun encrypt(plainText: String, key: SecretKey, random: SecureRandom = SecureRandom()): String {
        val iv = ByteArray(IV_BYTES).also(random::nextBytes)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        val ciphertext = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(iv + ciphertext)
    }

    fun decrypt(encoded: String, key: SecretKey): String {
        val payload = try { Base64.getDecoder().decode(encoded) }
        catch (_: IllegalArgumentException) { throw IllegalArgumentException("Зашифрованная запись не является корректным Base64.") }
        require(payload.size >= IV_BYTES + TAG_BYTES) { "Зашифрованная запись повреждена или обрезана." }
        val iv = payload.copyOfRange(0, IV_BYTES)
        val ciphertext = payload.copyOfRange(IV_BYTES, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }
}
