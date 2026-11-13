package com.lunacode.mobilesecurity.intelligence

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Signature
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineThreatDatabaseTest {
    private fun keyPair(): KeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
    private fun enc(value: String) = Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))

    private fun signed(pair: KeyPair, version: Long = 1L, title: String = "Known threat"): String {
        val lines = listOf(
            "PROSEC-THREAT-DB\t1",
            "version\t$version",
            "issued_at\t1760000000000",
            "key_id\tluna-test-key",
            "indicator\tindicator-1\tcom.example.bad\t-\t" + "A".repeat(64) + "\tHIGH\t95\t" + enc(title) + "\t" + enc("Review and remove if not recognized.")
        )
        val payload = lines.joinToString("\n")
        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(pair.private)
        signer.update(payload.toByteArray(Charsets.UTF_8))
        return payload + "\nsignature\tSHA256withRSA\t" + Base64.getEncoder().encodeToString(signer.sign())
    }

    @Test fun acceptsAValidSignatureAndBuildsPackageIndex() {
        val pair = keyPair()
        val trusted = mapOf("luna-test-key" to Base64.getEncoder().encodeToString(pair.public.encoded))
        val database = OfflineThreatDatabaseVerifier.verify(signed(pair), trusted)
        assertEquals(1L, database.version)
        assertEquals(1, database.byPackageName["com.example.bad"]?.size)
        assertEquals(95, database.indicators.single().confidence)
    }

    @Test fun rejectsUnknownSigningKey() {
        val pair = keyPair()
        val error = assertThrows(IllegalArgumentException::class.java) {
            OfflineThreatDatabaseVerifier.verify(signed(pair), emptyMap())
        }
        assertTrue(error.message.orEmpty().contains("не входит"))
    }

    @Test fun rejectsModifiedContent() {
        val pair = keyPair()
        val trusted = mapOf("luna-test-key" to Base64.getEncoder().encodeToString(pair.public.encoded))
        val tampered = signed(pair).replace(enc("Known threat"), enc("Other threat"))
        assertThrows(IllegalArgumentException::class.java) { OfflineThreatDatabaseVerifier.verify(tampered, trusted) }
    }

    @Test fun rejectsVersionRollback() {
        val pair = keyPair()
        val trusted = mapOf("luna-test-key" to Base64.getEncoder().encodeToString(pair.public.encoded))
        assertThrows(IllegalArgumentException::class.java) {
            OfflineThreatDatabaseVerifier.verify(signed(pair, version = 8), trusted, minimumVersionExclusive = 8)
        }
    }

    @Test fun rejectsMalformedIndicatorDigest() {
        val pair = keyPair()
        val payload = listOf(
            "PROSEC-THREAT-DB\t1", "version\t1", "issued_at\t1760000000000", "key_id\tluna-test-key",
            "indicator\tindicator-1\tcom.example.bad\tNOT-A-HASH\t-\tHIGH\t95\t" + enc("Bad") + "\t" + enc("Details")
        ).joinToString("\n")
        val signer = Signature.getInstance("SHA256withRSA").apply { initSign(pair.private); update(payload.toByteArray(Charsets.UTF_8)) }
        val full = payload + "\nsignature\tSHA256withRSA\t" + Base64.getEncoder().encodeToString(signer.sign())
        val trusted = mapOf("luna-test-key" to Base64.getEncoder().encodeToString(pair.public.encoded))
        assertThrows(IllegalArgumentException::class.java) { OfflineThreatDatabaseVerifier.verify(full, trusted) }
    }
}
