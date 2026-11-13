package com.lunacode.mobilesecurity.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import java.util.Base64 as JavaBase64
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
    private const val PREFIX = "PROSEC-CHAIN-1"

    /**
     * Pure-JVM framing keeps this codec testable outside Android.
     * Each field is URL-safe Base64, so the four-line envelope is unambiguous.
     */
    fun encode(previousTag: String, report: String, chainTag: String): String {
        val encoder = JavaBase64.getUrlEncoder().withoutPadding()
        fun field(value: String): String =
            encoder.encodeToString(value.toByteArray(StandardCharsets.UTF_8))
        return listOf(
            PREFIX,
            field(previousTag),
            field(chainTag),
            field(report)
        ).joinToString("\n")
    }

    fun decode(stored: String): ReportEnvelope {
        if (!stored.startsWith("$PREFIX\n")) {
            return ReportEnvelope(
                report = stored,
                previousTag = "",
                chainTag = null,
                wrapped = false
            )
        }

        val fields = stored.split('\n')
        if (fields.size != 4 || fields[0] != PREFIX) {
            return malformedEnvelope()
        }

        return runCatching {
            val decoder = JavaBase64.getUrlDecoder()
            fun field(value: String): String =
                String(decoder.decode(value), StandardCharsets.UTF_8)

            ReportEnvelope(
                report = field(fields[3]),
                previousTag = field(fields[1]),
                chainTag = field(fields[2]).takeIf { it.isNotBlank() },
                wrapped = true
            )
        }.getOrElse {
            malformedEnvelope()
        }
    }

    private fun malformedEnvelope() = ReportEnvelope(
        report = "",
        previousTag = "",
        chainTag = null,
        wrapped = true
    )

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
