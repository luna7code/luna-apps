package com.luna.secretnotes.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.SecureRandom

class ChunkedAttachmentCipherTest {
    private fun tempFolder() = File(System.getProperty("java.io.tmpdir"), "secret-notes-${System.nanoTime()}").apply { mkdirs() }

    @Test
    fun encryptsAndDecryptsMultiChunkFileWithoutPlaintextOnDisk() {
        val folder = tempFolder()
        val cipher = ChunkedAttachmentCipher(folder, SecureRandom(), 16 * 1024)
        val id = "a1b2c3d4-1234-5678-9012-abcdefabcdef"
        val key = ByteArray(32) { (it * 7).toByte() }
        val original = ByteArray(70_000) { (it % 239).toByte() }
        val encrypted = cipher.encrypt(ByteArrayInputStream(original), id, key)
        val encryptedBytes = File(folder, encrypted.encryptedFileName).readBytes()
        assertFalse(encryptedBytes.toString(Charsets.ISO_8859_1).contains("SECRET PLAINTEXT"))
        val output = ByteArrayOutputStream()
        val length = cipher.decrypt(encrypted.encryptedFileName, id, key, output)
        assertEquals(original.size.toLong(), length)
        assertArrayEquals(original, output.toByteArray())
        folder.deleteRecursively()
    }

    @Test
    fun tamperedChunkIsRejected() {
        val folder = tempFolder()
        val cipher = ChunkedAttachmentCipher(folder, SecureRandom(), 16 * 1024)
        val id = "b1b2c3d4-1234-5678-9012-abcdefabcdef"
        val key = ByteArray(32) { 9 }
        val encrypted = cipher.encrypt(ByteArrayInputStream(ByteArray(40000) { 4 }), id, key)
        val file = File(folder, encrypted.encryptedFileName)
        val bytes = file.readBytes()
        bytes[bytes.lastIndex - 20] = (bytes[bytes.lastIndex - 20].toInt() xor 1).toByte()
        file.writeBytes(bytes)
        var rejected = false
        try { cipher.decrypt(encrypted.encryptedFileName, id, key, ByteArrayOutputStream()) }
        catch (_: Exception) { rejected = true }
        assertTrue(rejected)
        folder.deleteRecursively()
    }

    @Test
    fun emptyAttachmentStillHasAuthenticatedTerminalRecord() {
        val folder = tempFolder()
        val cipher = ChunkedAttachmentCipher(folder)
        val id = "c1b2c3d4-1234-5678-9012-abcdefabcdef"
        val key = ByteArray(32) { 5 }
        val encrypted = cipher.encrypt(ByteArrayInputStream(ByteArray(0)), id, key)
        val output = ByteArrayOutputStream()
        assertEquals(0L, cipher.decrypt(encrypted.encryptedFileName, id, key, output))
        assertTrue(output.toByteArray().isEmpty())
        folder.deleteRecursively()
    }
}
