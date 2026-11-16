package com.luna.secretnotes.data

import com.luna.secretnotes.security.PasswordKdf
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest

class EncryptedBackupManagerTest {
    private class FastKdf : PasswordKdf {
        override fun derive(password: CharArray, salt: ByteArray): ByteArray {
            val plain = password.concatToString().toByteArray()
            return try {
                MessageDigest.getInstance("SHA-256").digest(plain + salt)
            } finally { plain.fill(0) }
        }
    }

    private fun workspace() =
        File(System.getProperty("java.io.tmpdir"), "secret-notes-backup-${System.nanoTime()}").apply { mkdirs() }

    @Test
    fun portableBackupRoundTripsDatabaseAttachmentsAndVaultKey() {
        val root = workspace()
        val manager = EncryptedBackupManager(root, FastKdf())
        val dbDir = File(root, "live-db").apply { mkdirs() }
        val attachments = File(root, "live-attachments").apply { mkdirs() }
        val dbBytes = "encrypted-sqlcipher-database".toByteArray()
        val attachmentBytes = "opaque-encrypted-attachment".toByteArray()
        File(dbDir, "secret_notes.db").writeBytes(dbBytes)
        File(attachments, "a1b2c3d4-1234-5678-9012-abcdefabcdef.sna").writeBytes(attachmentBytes)
        val key = ByteArray(32) { (it * 3).toByte() }
        val archive = ByteArrayOutputStream()

        manager.export("12345678".toCharArray(), key.copyOf(), File(dbDir, "secret_notes.db"), attachments, archive)
        val archiveBytes = archive.toByteArray()
        assertFalse(archiveBytes.toString(Charsets.ISO_8859_1).contains("encrypted-sqlcipher-database"))

        manager.restore("12345678".toCharArray(), ByteArrayInputStream(archiveBytes)).use { restored ->
            assertArrayEquals(dbBytes, restored.databaseFile.readBytes())
            assertArrayEquals(
                attachmentBytes,
                File(restored.attachmentsDirectory, "a1b2c3d4-1234-5678-9012-abcdefabcdef.sna").readBytes()
            )
            assertArrayEquals(key, restored.copyDataKey())
        }
        root.deleteRecursively()
    }

    @Test
    fun wrongPasswordAndTamperedArchiveAreRejected() {
        val root = workspace()
        val manager = EncryptedBackupManager(root, FastKdf())
        val db = File(root, "cipher.db").apply { writeBytes(ByteArray(128) { 7 }) }
        val attachments = File(root, "attachments").apply { mkdirs() }
        val archive = ByteArrayOutputStream()
        manager.export("12345678".toCharArray(), ByteArray(32) { 1 }, db, attachments, archive)
        val bytes = archive.toByteArray()

        var wrongPasswordRejected = false
        try { manager.restore("87654321".toCharArray(), ByteArrayInputStream(bytes)) }
        catch (_: Exception) { wrongPasswordRejected = true }
        assertTrue(wrongPasswordRejected)

        val corrupted = bytes.copyOf().also {
            it[it.lastIndex - 3] = (it[it.lastIndex - 3].toInt() xor 1).toByte()
        }
        var tamperingRejected = false
        try { manager.restore("12345678".toCharArray(), ByteArrayInputStream(corrupted)) }
        catch (_: Exception) { tamperingRejected = true }
        assertTrue(tamperingRejected)
        root.deleteRecursively()
    }
}
