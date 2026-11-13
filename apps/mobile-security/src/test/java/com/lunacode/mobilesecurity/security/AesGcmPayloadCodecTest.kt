package com.lunacode.mobilesecurity.security

import javax.crypto.AEADBadTagException
import javax.crypto.KeyGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AesGcmPayloadCodecTest {
    private fun key() = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    @Test fun roundTripsUnicodeReportPayload() {
        val key = key()
        val payload = "{\"device\":\"Huawei Pura 70\",\"status\":\"проверено\"}"
        assertEquals(payload, AesGcmPayloadCodec.decrypt(AesGcmPayloadCodec.encrypt(payload, key), key))
    }

    @Test fun usesFreshIvForEveryEncryption() {
        val key = key()
        assertNotEquals(AesGcmPayloadCodec.encrypt("same", key), AesGcmPayloadCodec.encrypt("same", key))
    }

    @Test fun rejectsCiphertextTampering() {
        val key = key()
        val bytes = java.util.Base64.getDecoder().decode(AesGcmPayloadCodec.encrypt("immutable", key))
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
        val tampered = java.util.Base64.getEncoder().encodeToString(bytes)
        assertThrows(AEADBadTagException::class.java) { AesGcmPayloadCodec.decrypt(tampered, key) }
    }

    @Test fun roundTripsEmptyPayload() {
        val key = key()
        assertEquals("", AesGcmPayloadCodec.decrypt(AesGcmPayloadCodec.encrypt("", key), key))
    }

    @Test fun rejectsTruncatedPayload() {
        assertThrows(IllegalArgumentException::class.java) { AesGcmPayloadCodec.decrypt("AQID", key()) }
    }

    @Test fun rejectsWrongKey() {
        val encrypted = AesGcmPayloadCodec.encrypt("private", key())
        assertThrows(AEADBadTagException::class.java) { AesGcmPayloadCodec.decrypt(encrypted, key()) }
    }
}
