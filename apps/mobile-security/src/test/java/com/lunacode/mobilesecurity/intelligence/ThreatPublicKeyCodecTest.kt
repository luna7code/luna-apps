package com.lunacode.mobilesecurity.intelligence

import java.security.KeyPairGenerator
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ThreatPublicKeyCodecTest {
    private fun rsa(bits: Int = 2048) =
        KeyPairGenerator.getInstance("RSA").apply { initialize(bits) }.generateKeyPair()

    @Test
    fun acceptsPemAndComputesStableFingerprint() {
        val pair = rsa()
        val encoded = Base64.getMimeEncoder(64, "\n".toByteArray()).encodeToString(pair.public.encoded)
        val pem = "-----BEGIN PUBLIC KEY-----\n$encoded\n-----END PUBLIC KEY-----"
        val parsed = ThreatPublicKeyCodec.parse("luna-code-primary", pem)
        assertEquals("luna-code-primary", parsed.keyId)
        assertEquals(Base64.getEncoder().encodeToString(pair.public.encoded), parsed.publicKeyBase64)
        assertEquals(32, parsed.sha256Fingerprint.split(":").size)
        val expectedFingerprint = java.security.MessageDigest.getInstance("SHA-256")
            .digest(pair.public.encoded)
            .joinToString(":") { "%02X".format(it) }
        assertEquals(expectedFingerprint, parsed.sha256Fingerprint)
    }

    @Test
    fun rejectsSmallRsaKeys() {
        assertThrows(IllegalArgumentException::class.java) {
            ThreatPublicKeyCodec.parse("old-key", Base64.getEncoder().encodeToString(rsa(1024).public.encoded))
        }
    }

    @Test
    fun rejectsInvalidKeyIdAndMalformedPem() {
        assertThrows(IllegalArgumentException::class.java) {
            ThreatPublicKeyCodec.parse("bad key id", "not-a-key")
        }
        assertThrows(IllegalArgumentException::class.java) {
            ThreatPublicKeyCodec.parse("good-key", "not-a-key")
        }
    }
}
