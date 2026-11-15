package com.luna.secretnotes.security

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.security.SecureRandom
import javax.crypto.AEADBadTagException

class VaultKeyProtectorTest {
    private class TestKdf : PasswordKdf {
        override fun derive(password: CharArray, salt: ByteArray): ByteArray =
            java.security.MessageDigest.getInstance("SHA-256")
                .digest(password.concatToString().toByteArray() + salt)
    }

    private class TestMac : KeyedMac {
        override fun authenticate(data: ByteArray): ByteArray =
            java.security.MessageDigest.getInstance("SHA-256")
                .digest("test-keystore-key".toByteArray() + data)
    }

    @Test
    fun wrapsAndUnwrapsDataKey() {
        val protector = VaultKeyProtector(TestKdf(), TestMac(), SecureRandom())
        val password = "12345678".toCharArray()
        val dataKey = ByteArray(32) { it.toByte() }
        val record = protector.createRecord(password, dataKey)

        assertArrayEquals(dataKey, protector.unwrapDataKey(password, record))
    }

    @Test(expected = AEADBadTagException::class)
    fun wrongPasswordCannotUnwrapDataKey() {
        val protector = VaultKeyProtector(TestKdf(), TestMac(), SecureRandom())
        val record = protector.createRecord("12345678".toCharArray(), ByteArray(32) { 7 })
        protector.unwrapDataKey("87654321".toCharArray(), record)
    }

    @Test
    fun everyCredentialRecordUsesFreshSaltAndNonce() {
        val protector = VaultKeyProtector(TestKdf(), TestMac(), SecureRandom())
        val key = ByteArray(32) { 4 }
        val a = protector.createRecord("12345678".toCharArray(), key)
        val b = protector.createRecord("12345678".toCharArray(), key)
        assertNotEquals(a.salt.toList(), b.salt.toList())
        assertNotEquals(a.nonce.toList(), b.nonce.toList())
    }
}
