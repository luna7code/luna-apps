package com.luna.secretnotes.data

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Chunk-authenticated file encryption for large attachments.
 * Every chunk is independently AES-256-GCM authenticated; one authenticated
 * terminal record makes truncation detectable. The supplied vault DEK is never
 * used directly: a per-file key is derived with HKDF-SHA256 and a random salt.
 */
class ChunkedAttachmentCipher(
    private val directory: File,
    private val random: SecureRandom = SecureRandom(),
    private val chunkSize: Int = DEFAULT_CHUNK_SIZE
) {
    init {
        require(chunkSize in MIN_CHUNK_SIZE..MAX_CHUNK_SIZE)
        if (!directory.exists()) check(directory.mkdirs()) { "Cannot create attachment directory" }
    }

    fun encrypt(
        input: InputStream,
        attachmentId: String,
        vaultKey: ByteArray
    ): EncryptedAttachment {
        validateId(attachmentId)
        require(vaultKey.size == KEY_BYTES)
        val fileName = "$attachmentId.sna"
        val target = File(directory, fileName)
        val temporary = File(directory, "$attachmentId.tmp")
        check(!target.exists()) { "Attachment already exists" }

        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val fileKey = deriveFileKey(vaultKey, salt, attachmentId)
        var totalBytes = 0L
        try {
            DataOutputStream(BufferedOutputStream(FileOutputStream(temporary))).use { output ->
                output.write(MAGIC)
                output.writeInt(FORMAT_VERSION)
                output.writeInt(chunkSize)
                output.write(salt)
                output.writeInt(attachmentId.toByteArray(Charsets.UTF_8).size)
                output.write(attachmentId.toByteArray(Charsets.UTF_8))

                val buffer = ByteArray(chunkSize)
                var counter = 0L
                while (true) {
                    val count = readChunk(input, buffer)
                    if (count == 0) break
                    val nonce = ByteArray(NONCE_BYTES).also(random::nextBytes)
                    val encrypted = encryptChunk(fileKey, nonce, buffer, count, attachmentId, counter, false)
                    output.writeInt(count)
                    output.write(nonce)
                    output.write(encrypted)
                    totalBytes += count
                    counter++
                    buffer.fill(0)
                }
                buffer.fill(0)

                // Explicit authenticated end marker: a valid prefix is not a valid file.
                val finalNonce = ByteArray(NONCE_BYTES).also(random::nextBytes)
                val finalTag = encryptChunk(fileKey, finalNonce, ByteArray(0), 0, attachmentId, counter, true)
                output.writeInt(0)
                output.write(finalNonce)
                output.write(finalTag)
                output.flush()
            }
            check(temporary.renameTo(target)) { "Cannot atomically publish encrypted attachment" }
            return EncryptedAttachment(attachmentId, fileName, totalBytes)
        } catch (error: Throwable) {
            temporary.delete()
            target.delete()
            throw error
        } finally {
            salt.fill(0)
            fileKey.fill(0)
        }
    }

    fun decrypt(
        encryptedFileName: String,
        attachmentId: String,
        vaultKey: ByteArray,
        output: OutputStream
    ): Long {
        validateId(attachmentId)
        require(vaultKey.size == KEY_BYTES)
        require(encryptedFileName == "$attachmentId.sna") { "Invalid encrypted attachment path" }
        val source = File(directory, encryptedFileName)
        require(source.isFile) { "Encrypted attachment is missing" }

        DataInputStream(BufferedInputStream(FileInputStream(source))).use { input ->
            val magic = ByteArray(MAGIC.size).also(input::readFully)
            require(MessageDigest.isEqual(magic, MAGIC)) { "Invalid attachment header" }
            val version = input.readInt()
            require(version == FORMAT_VERSION) { "Unsupported attachment format" }
            val storedChunkSize = input.readInt()
            require(storedChunkSize in MIN_CHUNK_SIZE..MAX_CHUNK_SIZE) { "Invalid chunk size" }
            val salt = ByteArray(SALT_BYTES).also(input::readFully)
            val idLength = input.readInt()
            require(idLength == attachmentId.toByteArray(Charsets.UTF_8).size) { "Attachment ID mismatch" }
            val storedId = ByteArray(idLength).also(input::readFully)
            require(MessageDigest.isEqual(storedId, attachmentId.toByteArray(Charsets.UTF_8))) {
                "Attachment ID mismatch"
            }
            val fileKey = deriveFileKey(vaultKey, salt, attachmentId)
            var totalBytes = 0L
            var counter = 0L
            var finalSeen = false
            try {
                while (!finalSeen) {
                    val plainLength = input.readInt()
                    require(plainLength in 0..storedChunkSize) { "Invalid attachment chunk length" }
                    val nonce = ByteArray(NONCE_BYTES).also(input::readFully)
                    val encrypted = ByteArray(plainLength + TAG_BYTES).also(input::readFully)
                    val plain = decryptChunk(
                        fileKey, nonce, encrypted, attachmentId, counter, plainLength == 0
                    )
                    if (plainLength == 0) {
                        require(plain.isEmpty()) { "Invalid terminal record" }
                        finalSeen = true
                    } else {
                        require(plain.size == plainLength)
                        output.write(plain)
                        totalBytes += plain.size
                        counter++
                    }
                    plain.fill(0)
                }
                require(input.read() == -1) { "Unexpected data after attachment terminator" }
                output.flush()
                return totalBytes
            } finally {
                salt.fill(0)
                storedId.fill(0)
                fileKey.fill(0)
            }
        }
    }

    fun delete(encryptedFileName: String, attachmentId: String) {
        validateId(attachmentId)
        require(encryptedFileName == "$attachmentId.sna") { "Invalid encrypted attachment path" }
        File(directory, encryptedFileName).delete()
        File(directory, "$attachmentId.tmp").delete()
    }

    private fun encryptChunk(
        key: ByteArray,
        nonce: ByteArray,
        buffer: ByteArray,
        length: Int,
        id: String,
        counter: Long,
        terminal: Boolean
    ): ByteArray = cipher(Cipher.ENCRYPT_MODE, key, nonce, id, counter, length, terminal)
        .doFinal(buffer, 0, length)

    private fun decryptChunk(
        key: ByteArray,
        nonce: ByteArray,
        encrypted: ByteArray,
        id: String,
        counter: Long,
        terminal: Boolean
    ): ByteArray {
        val plainLength = encrypted.size - TAG_BYTES
        return cipher(
            Cipher.DECRYPT_MODE, key, nonce, id, counter, plainLength, terminal
        ).doFinal(encrypted)
    }

    private fun cipher(
        mode: Int,
        key: ByteArray,
        nonce: ByteArray,
        id: String,
        counter: Long,
        plainLength: Int,
        terminal: Boolean
    ): Cipher {
        val aad = "$DOMAIN|$FORMAT_VERSION|$id|$counter|$plainLength|$terminal".toByteArray(Charsets.UTF_8)
        return Cipher.getInstance(AES_GCM).apply {
            init(mode, SecretKeySpec(key, AES), GCMParameterSpec(TAG_BITS, nonce))
            updateAAD(aad)
            aad.fill(0)
        }
    }

    private fun deriveFileKey(vaultKey: ByteArray, salt: ByteArray, id: String): ByteArray {
        val extract = Mac.getInstance(HMAC).run {
            init(SecretKeySpec(salt, HMAC))
            doFinal(vaultKey)
        }
        val info = "$DOMAIN|attachment-key|$id".toByteArray(Charsets.UTF_8)
        return try {
            Mac.getInstance(HMAC).run {
                init(SecretKeySpec(extract, HMAC))
                update(info)
                update(1.toByte())
                doFinal().copyOf(KEY_BYTES)
            }
        } finally {
            extract.fill(0)
            info.fill(0)
        }
    }

    private fun readChunk(input: InputStream, buffer: ByteArray): Int {
        var count = 0
        while (count < buffer.size) {
            val read = input.read(buffer, count, buffer.size - count)
            if (read < 0) break
            if (read == 0) continue
            count += read
        }
        return count
    }

    private fun validateId(id: String) {
        require(id.matches(Regex("[a-fA-F0-9-]{16,64}"))) { "Invalid attachment identifier" }
    }

    data class EncryptedAttachment(
        val id: String,
        val encryptedFileName: String,
        val sizeBytes: Long
    )

    companion object {
        const val DEFAULT_CHUNK_SIZE = 256 * 1024
        const val MIN_CHUNK_SIZE = 16 * 1024
        const val MAX_CHUNK_SIZE = 1024 * 1024
        const val FORMAT_VERSION = 1
        private const val DOMAIN = "SECRET_NOTES_ATTACHMENT"
        private const val AES = "AES"
        private const val HMAC = "HmacSHA256"
        private const val AES_GCM = "AES/GCM/NoPadding"
        private const val KEY_BYTES = 32
        private const val SALT_BYTES = 16
        private const val NONCE_BYTES = 12
        private const val TAG_BYTES = 16
        private const val TAG_BITS = 128
        private val MAGIC = "SNATTCH1".toByteArray(Charsets.US_ASCII)
    }
}
