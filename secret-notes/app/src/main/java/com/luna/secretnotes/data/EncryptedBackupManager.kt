package com.luna.secretnotes.data

import com.luna.secretnotes.security.Argon2idKdf
import com.luna.secretnotes.security.PasswordKdf
import java.io.*
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Offline portable archive. SQLCipher DB and attachment payloads stay encrypted.
 * The vault DEK is wrapped under a PIN-derived portable subkey; the entire archive
 * is encrypted/authenticated again with AES-256-GCM under the same PIN/password.
 */
class EncryptedBackupManager(
    private val stagingDirectory: File,
    private val kdf: PasswordKdf = Argon2idKdf(),
    private val random: SecureRandom = SecureRandom()
) {
    init {
        if (!stagingDirectory.exists()) check(stagingDirectory.mkdirs())
    }

    fun export(
        password: CharArray,
        vaultDataKey: ByteArray,
        encryptedDatabase: File,
        encryptedAttachmentsDirectory: File,
        output: OutputStream
    ) {
        require(vaultDataKey.size == KEY_BYTES)
        require(encryptedDatabase.isFile) { "Encrypted notebook database is missing" }
        val work = File(stagingDirectory, "backup-" + UUID.randomUUID())
        check(work.mkdirs())
        val zipFile = File(work, "payload.zip")
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val nonce = ByteArray(NONCE_BYTES).also(random::nextBytes)
        var archiveKey: ByteArray? = null
        try {
            archiveKey = kdf.derive(password, salt)
            require(archiveKey.size == KEY_BYTES)
            val wrappedDek = wrapDataKey(archiveKey, salt, vaultDataKey)
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zip ->
                putBytes(zip, "vault/dek.wrap", wrappedDek)
                putFile(zip, "database/secret_notes.db", encryptedDatabase)
                val attachmentFiles = encryptedAttachmentsDirectory.listFiles().orEmpty()
                    .filter { it.isFile && ATTACHMENT_PATTERN.matches(it.name) }
                    .sortedBy { it.name }
                attachmentFiles.forEach { putFile(zip, "attachments/" + it.name, it) }
                val manifest = buildString {
                    append("{\"format\":1,")
                    append("\"database\":\"database/secret_notes.db\",")
                    append("\"attachments\":[")
                    append(attachmentFiles.joinToString(",") { "\"" + it.name + "\"" })
                    append("]}")
                }.toByteArray(Charsets.UTF_8)
                putBytes(zip, "manifest.json", manifest)
                manifest.fill(0)
            }
            val header = createHeader(salt, nonce)
            val cipher = gcmCipher(Cipher.ENCRYPT_MODE, archiveKey, nonce, header)
            output.write(header)
            FileInputStream(zipFile).use { encryptStream(it, output, cipher) }
            output.flush()
            wrappedDek.fill(0)
        } finally {
            password.fill('\u0000')
            vaultDataKey.fill(0)
            archiveKey?.fill(0)
            salt.fill(0)
            nonce.fill(0)
            work.deleteRecursively()
        }
    }

    /** Decrypts and validates into private staging. Caller controls final database replacement. */
    fun restore(password: CharArray, input: InputStream): RestoredBackup {
        val work = File(stagingDirectory, "restore-" + UUID.randomUUID())
        check(work.mkdirs())
        val payloadZip = File(work, "payload.zip")
        var archiveKey: ByteArray? = null
        var dataKey: ByteArray? = null
        try {
            val source = DataInputStream(BufferedInputStream(input))
            val header = ByteArray(HEADER_BYTES).also { source.readFully(it) }
            val parsed = parseHeader(header)
            val salt = parsed.first
            val nonce = parsed.second
            archiveKey = kdf.derive(password, salt)
            require(archiveKey.size == KEY_BYTES)
            val cipher = gcmCipher(Cipher.DECRYPT_MODE, archiveKey, nonce, header)
            FileOutputStream(payloadZip).use { decryptStream(source, it, cipher) }
            val extracted = extractArchive(payloadZip, work)
            dataKey = unwrapDataKey(archiveKey, salt, extracted.wrappedDek)
            require(dataKey.size == KEY_BYTES)
            val result = RestoredBackup(work, extracted.databaseFile, extracted.attachmentsDirectory, dataKey)
            dataKey = null
            salt.fill(0)
            nonce.fill(0)
            return result
        } catch (error: Throwable) {
            work.deleteRecursively()
            throw error
        } finally {
            password.fill('\u0000')
            archiveKey?.fill(0)
            dataKey?.fill(0)
        }
    }

    private data class Extracted(
        val databaseFile: File,
        val attachmentsDirectory: File,
        val wrappedDek: ByteArray
    )

    private fun extractArchive(zipFile: File, work: File): Extracted {
        val root = File(work, "extract").apply { check(mkdirs()) }
        val attachments = File(root, "attachments").apply { mkdirs() }
        var database: File? = null
        var wrapped: ByteArray? = null
        var manifest: ByteArray? = null
        val seen = mutableSetOf<String>()
        ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                require(!entry.isDirectory)
                require(seen.add(entry.name)) { "Duplicate archive entry" }
                when {
                    entry.name == "database/secret_notes.db" -> {
                        require(database == null)
                        database = File(root, "secret_notes.db").also { copyEntry(zip, it) }
                    }
                    entry.name == "vault/dek.wrap" -> {
                        require(wrapped == null)
                        wrapped = zip.readBytes()
                    }
                    entry.name == "manifest.json" -> {
                        require(manifest == null)
                        manifest = zip.readBytes()
                    }
                    entry.name.startsWith("attachments/") &&
                        ATTACHMENT_PATTERN.matches(entry.name.removePrefix("attachments/")) -> {
                        copyEntry(zip, File(attachments, entry.name.removePrefix("attachments/")))
                    }
                    else -> error("Unexpected backup entry: ${entry.name}")
                }
                zip.closeEntry()
            }
        }
        require(database?.isFile == true) { "Backup database is missing" }
        require(wrapped?.size == WRAPPED_DEK_BYTES) { "Wrapped vault key is missing" }
        val manifestBytes = requireNotNull(manifest) { "Backup manifest is missing" }
        val manifestText = manifestBytes.toString(Charsets.UTF_8)
        require(manifestText.contains("\"format\":1"))
        require(manifestText.contains("\"database\":\"database/secret_notes.db\""))
        seen.filter { it.startsWith("attachments/") }.forEach { path ->
            require(manifestText.contains("\"" + path.removePrefix("attachments/") + "\"")) {
                "Attachment is not declared in the backup manifest"
            }
        }
        manifestBytes.fill(0)
        return Extracted(requireNotNull(database), attachments, requireNotNull(wrapped))
    }

    private fun wrapDataKey(archiveKey: ByteArray, salt: ByteArray, dataKey: ByteArray): ByteArray {
        val wrapKey = deriveSubkey(archiveKey, salt, "SECRET_NOTES_BACKUP_DEK_WRAP_V1")
        val nonce = ByteArray(NONCE_BYTES).also(random::nextBytes)
        return try {
            val cipher = Cipher.getInstance(AES_GCM).apply {
                init(Cipher.ENCRYPT_MODE, SecretKeySpec(wrapKey, AES), GCMParameterSpec(TAG_BITS, nonce))
                updateAAD(DEK_AAD)
            }
            nonce + cipher.doFinal(dataKey)
        } finally {
            wrapKey.fill(0)
            nonce.fill(0)
        }
    }

    private fun unwrapDataKey(archiveKey: ByteArray, salt: ByteArray, wrapped: ByteArray): ByteArray {
        require(wrapped.size == WRAPPED_DEK_BYTES)
        val nonce = wrapped.copyOfRange(0, NONCE_BYTES)
        val ciphertext = wrapped.copyOfRange(NONCE_BYTES, wrapped.size)
        val wrapKey = deriveSubkey(archiveKey, salt, "SECRET_NOTES_BACKUP_DEK_WRAP_V1")
        return try {
            Cipher.getInstance(AES_GCM).run {
                init(Cipher.DECRYPT_MODE, SecretKeySpec(wrapKey, AES), GCMParameterSpec(TAG_BITS, nonce))
                updateAAD(DEK_AAD)
                doFinal(ciphertext)
            }
        } finally {
            nonce.fill(0)
            ciphertext.fill(0)
            wrapKey.fill(0)
            wrapped.fill(0)
        }
    }

    private fun deriveSubkey(master: ByteArray, salt: ByteArray, info: String): ByteArray {
        val prk = Mac.getInstance(HMAC).run {
            init(SecretKeySpec(salt, HMAC))
            doFinal(master)
        }
        val context = info.toByteArray(Charsets.UTF_8)
        return try {
            Mac.getInstance(HMAC).run {
                init(SecretKeySpec(prk, HMAC))
                update(context)
                update(1.toByte())
                doFinal().copyOf(KEY_BYTES)
            }
        } finally {
            prk.fill(0)
            context.fill(0)
        }
    }

    private fun createHeader(salt: ByteArray, nonce: ByteArray): ByteArray =
        ByteBuffer.allocate(HEADER_BYTES)
            .put(MAGIC).putInt(FORMAT_VERSION)
            .putInt(ARGON_MEMORY_KIB).putInt(ARGON_ITERATIONS).putInt(ARGON_PARALLELISM)
            .put(salt).put(nonce).array()

    private fun parseHeader(header: ByteArray): Pair<ByteArray, ByteArray> {
        val buffer = ByteBuffer.wrap(header)
        val magic = ByteArray(MAGIC.size).also { buffer.get(it) }
        require(MessageDigest.isEqual(magic, MAGIC)) { "Not a SECRET NOTES backup" }
        require(buffer.int == FORMAT_VERSION) { "Unsupported backup format" }
        require(buffer.int == ARGON_MEMORY_KIB)
        require(buffer.int == ARGON_ITERATIONS)
        require(buffer.int == ARGON_PARALLELISM)
        return ByteArray(SALT_BYTES).also { buffer.get(it) } to
            ByteArray(NONCE_BYTES).also { buffer.get(it) }
    }

    private fun gcmCipher(mode: Int, key: ByteArray, nonce: ByteArray, header: ByteArray): Cipher =
        Cipher.getInstance(AES_GCM).apply {
            init(mode, SecretKeySpec(key, AES), GCMParameterSpec(TAG_BITS, nonce))
            updateAAD(header)
        }

    private fun encryptStream(input: InputStream, output: OutputStream, cipher: Cipher) {
        val buffer = ByteArray(STREAM_BUFFER_BYTES)
        try {
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (count > 0) cipher.update(buffer, 0, count)?.let(output::write)
            }
            cipher.doFinal().let(output::write)
        } finally { buffer.fill(0) }
    }

    private fun decryptStream(input: InputStream, output: OutputStream, cipher: Cipher) {
        val buffer = ByteArray(STREAM_BUFFER_BYTES)
        try {
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (count > 0) cipher.update(buffer, 0, count)?.let(output::write)
            }
            cipher.doFinal().let(output::write)
            output.flush()
        } finally { buffer.fill(0) }
    }

    private fun putBytes(zip: ZipOutputStream, name: String, bytes: ByteArray) {
        zip.putNextEntry(ZipEntry(name)); zip.write(bytes); zip.closeEntry()
    }

    private fun putFile(zip: ZipOutputStream, name: String, file: File) {
        zip.putNextEntry(ZipEntry(name))
        FileInputStream(file).use { it.copyTo(zip, STREAM_BUFFER_BYTES) }
        zip.closeEntry()
    }

    private fun copyEntry(input: InputStream, file: File) {
        file.parentFile?.mkdirs()
        FileOutputStream(file).use { input.copyTo(it, STREAM_BUFFER_BYTES) }
    }

    class RestoredBackup internal constructor(
        private val stagingDirectory: File,
        val databaseFile: File,
        val attachmentsDirectory: File,
        dataKey: ByteArray
    ) : AutoCloseable {
        private val key = dataKey.copyOf()
        private var closed = false
        @Synchronized fun copyDataKey(): ByteArray {
            check(!closed)
            return key.copyOf()
        }
        @Synchronized override fun close() {
            if (!closed) {
                key.fill(0)
                stagingDirectory.deleteRecursively()
                closed = true
            }
        }
    }

    companion object {
        const val FORMAT_VERSION = 1
        const val EXTENSION = "snb"
        private const val ARGON_MEMORY_KIB = 64 * 1024
        private const val ARGON_ITERATIONS = 3
        private const val ARGON_PARALLELISM = 1
        private const val AES = "AES"
        private const val HMAC = "HmacSHA256"
        private const val AES_GCM = "AES/GCM/NoPadding"
        private const val KEY_BYTES = 32
        private const val SALT_BYTES = 16
        private const val NONCE_BYTES = 12
        private const val TAG_BITS = 128
        private const val WRAPPED_DEK_BYTES = NONCE_BYTES + KEY_BYTES + 16
        private const val STREAM_BUFFER_BYTES = 128 * 1024
        private const val HEADER_BYTES = 8 + 4 * 4 + SALT_BYTES + NONCE_BYTES
        private val MAGIC = "SNBACK01".toByteArray(Charsets.US_ASCII)
        private val DEK_AAD = "SECRET_NOTES_BACKUP_DEK_V1".toByteArray(Charsets.UTF_8)
        private val ATTACHMENT_PATTERN = Regex("[a-fA-F0-9-]{16,64}\\.sna")
    }
}
