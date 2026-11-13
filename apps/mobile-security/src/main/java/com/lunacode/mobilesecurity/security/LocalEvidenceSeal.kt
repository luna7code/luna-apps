package com.lunacode.mobilesecurity.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

data class ReportEnvelope(
    val report: String,
    val previousTag: String,
    val chainTag: String?,
    val wrapped: Boolean
)

object ReportEnvelopeCodec {
    fun encode(previousTag: String, report: String, chainTag: String): String =
        JSONObject().apply {
            put("chainVersion", 1)
            put("previousTag", previousTag)
            put("chainTag", chainTag)
            put("report", report)
        }.toString()

    fun decode(stored: String): ReportEnvelope {
        val outer = runCatching { JSONObject(stored) }.getOrNull()
        if (outer?.optInt("chainVersion") == 1 && outer.has("report")) {
            return ReportEnvelope(
                report = outer.optString("report"),
                previousTag = outer.optString("previousTag"),
                chainTag = outer.optString("chainTag").takeIf { it.isNotBlank() },
                wrapped = true
            )
        }
        return ReportEnvelope(
            report = stored,
            previousTag = "",
            chainTag = null,
            wrapped = false
        )
    }

    fun material(previousTag: String, report: String): String =
        previousTag + "\n" + report
}

object LocalEvidenceSeal {
    private const val KEY_ALIAS = "pro_sec_evidence_hmac_v1"
    private const val TRANSFORMATION = "HmacSHA256"

    fun sign(material: String): String {
        val mac = Mac.getInstance(TRANSFORMATION)
        mac.init(getOrCreateKey())
        return Base64.encodeToString(
            mac.doFinal(material.toByteArray(Charsets.UTF_8)),
            Base64.NO_WRAP
        )
    }

    fun verify(material: String, encodedTag: String): Boolean {
        val expected = Base64.decode(encodedTag, Base64.NO_WRAP)
        val mac = Mac.getInstance(TRANSFORMATION)
        mac.init(getOrCreateKey())
        val actual = mac.doFinal(material.toByteArray(Charsets.UTF_8))
        return MessageDigest.isEqual(expected, actual)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_HMAC_SHA256,
            "AndroidKeyStore"
        )
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(KeyProperties.DIGEST_SHA256)
                .build()
        )
        return generator.generateKey()
    }
}
