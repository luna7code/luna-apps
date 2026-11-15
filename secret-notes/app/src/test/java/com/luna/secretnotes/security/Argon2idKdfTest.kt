package com.luna.secretnotes.security

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Test

class Argon2idKdfTest {
    private val testKdf = Argon2idKdf(
        Argon2idConfig(memoryKiB = 8 * 1024, iterations = 1, parallelism = 1)
    )

    @Test
    fun derivesStableKeyForSamePasswordAndSalt() {
        val salt = ByteArray(16) { it.toByte() }
        val first = testKdf.derive("test-password".toCharArray(), salt)
        val second = testKdf.derive("test-password".toCharArray(), salt)
        assertEquals(32, first.size)
        assertArrayEquals(first, second)
        first.fill(0)
        second.fill(0)
    }

    @Test
    fun differentSaltProducesDifferentKey() {
        val first = testKdf.derive("test-password".toCharArray(), ByteArray(16) { 1 })
        val second = testKdf.derive("test-password".toCharArray(), ByteArray(16) { 2 })
        assertFalse(first.contentEquals(second))
        first.fill(0)
        second.fill(0)
    }
}
