package com.lunacode.mobilesecurity.intelligence

import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

/**
 * Stores public trust anchors only. Pinning requires an explicit confirmation
 * that the SHA-256 fingerprint was checked through an independent trusted channel.
 */
class ThreatKeyTrustStore(private val context: Context) {
    private val preferences = context.getSharedPreferences("pro_sec_threat_trust", Context.MODE_PRIVATE)

    fun publicKeys(): Map<String, String> {
        val keyId = preferences.getString(KEY_ID, null)?.takeIf { it.isNotBlank() } ?: return emptyMap()
        val key = preferences.getString(KEY_BASE64, null)?.takeIf { it.isNotBlank() } ?: return emptyMap()
        return mapOf(keyId to key)
    }

    fun currentFingerprint(): String? = preferences.getString(KEY_FINGERPRINT, null)

    fun readCandidate(uri: Uri, keyId: String): ThreatPublicKeyCandidate {
        val content = context.contentResolver.openInputStream(uri)?.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                total += read
                require(total <= MAX_BYTES) { "Файл публичного ключа превышает 64 КБ." }
                output.write(buffer, 0, read)
            }
            String(output.toByteArray(), StandardCharsets.UTF_8)
        } ?: throw IllegalArgumentException("Не удалось открыть файл публичного ключа.")
        return ThreatPublicKeyCodec.parse(keyId, content)
    }

    fun pin(candidate: ThreatPublicKeyCandidate, fingerprintCheckedIndependently: Boolean) {
        require(fingerprintCheckedIndependently) {
            "Закрепление запрещено без явного подтверждения независимой сверки SHA-256 отпечатка."
        }
        val reparsed = ThreatPublicKeyCodec.parse(candidate.keyId, candidate.publicKeyBase64)
        require(reparsed.sha256Fingerprint == candidate.sha256Fingerprint) {
            "Отпечаток кандидата не совпадает с повторно рассчитанным."
        }
        val saved = preferences.edit()
            .putString(KEY_ID, candidate.keyId)
            .putString(KEY_BASE64, candidate.publicKeyBase64)
            .putString(KEY_FINGERPRINT, candidate.sha256Fingerprint)
            .commit()
        check(saved) { "Не удалось сохранить доверенный публичный ключ." }
    }

    fun trustedKeySummary(): String {
        val id = preferences.getString(KEY_ID, null) ?: return "Доверенный ключ ещё не закреплён."
        return "ID: $id\nSHA-256: ${currentFingerprint().orEmpty()}"
    }

    companion object {
        private const val KEY_ID = "pinned_key_id"
        private const val KEY_BASE64 = "pinned_public_key"
        private const val KEY_FINGERPRINT = "pinned_fingerprint"
        private const val MAX_BYTES = 64 * 1024
    }
}
