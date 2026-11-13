package com.lunacode.mobilesecurity.intelligence

import java.security.KeyFactory
import java.security.MessageDigest
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

data class ThreatPublicKeyCandidate(
    val keyId: String,
    val publicKeyBase64: String,
    val sha256Fingerprint: String
)

/** Strict parser for an RSA X.509 SubjectPublicKeyInfo PEM / Base64 trust anchor. */
object ThreatPublicKeyCodec {
    private val keyIdPattern = Regex("^[A-Za-z0-9._-]{1,80}$")
    private const val MAX_PEM_CHARS = 64 * 1024

    fun parse(keyId: String, pemOrBase64: String): ThreatPublicKeyCandidate {
        require(keyIdPattern.matches(keyId)) {
            "ID ключа должен содержать 1–80 символов A–Z, a–z, 0–9, точку, дефис или подчёркивание."
        }
        require(pemOrBase64.length <= MAX_PEM_CHARS) { "Файл публичного ключа слишком большой." }
        val body = pemOrBase64
            .replace(Regex("-----BEGIN [^-]+-----"), "")
            .replace(Regex("-----END [^-]+-----"), "")
            .filterNot { it.isWhitespace() }
        require(body.isNotBlank()) { "Файл не содержит публичный ключ." }
        val der = try {
            Base64.getDecoder().decode(body)
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("Ключ не является корректным PEM или Base64.")
        }
        require(der.size in 256..16_384) { "Недопустимый размер закодированного публичного ключа." }
        val publicKey = try {
            KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(der))
        } catch (_: Exception) {
            throw IllegalArgumentException("Не удалось прочитать RSA X.509 публичный ключ.")
        }
        val rsa = publicKey as? RSAPublicKey
            ?: throw IllegalArgumentException("Поддерживаются только RSA публичные ключи.")
        require(rsa.modulus.bitLength() >= 2048) { "Для ключа подписи требуется RSA не менее 2048 бит." }

        val fingerprint = MessageDigest.getInstance("SHA-256")
            .digest(der)
            .joinToString(":") { "%02X".format(it) }
        return ThreatPublicKeyCandidate(
            keyId = keyId,
            publicKeyBase64 = Base64.getEncoder().encodeToString(der),
            sha256Fingerprint = fingerprint
        )
    }
}
