package com.luna.secretnotes.security

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class VaultRecord(
    val formatVersion: Int,
    val memoryKiB: Int,
    val iterations: Int,
    val parallelism: Int,
    val salt: ByteArray,
    val nonce: ByteArray,
    val wrappedDataKey: ByteArray
)

class VaultKeyProtector(
    private val kdf: PasswordKdf,
    private val deviceMac: KeyedMac,
    private val random: SecureRandom = SecureRandom()
) {
    fun createRecord(password: CharArray, dataKey: ByteArray): VaultRecord {
        require(dataKey.size == DATA_KEY_BYTES)
        val config = Argon2idConfig.DEFAULT
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val nonce = ByteArray(GCM_NONCE_BYTES).also(random::nextBytes)
        val derived = kdf.derive(password, salt)
        val wrappingKey = try {
            deviceMac.authenticate(DOMAIN_SEPARATOR + derived)
        } finally {
            derived.fill(0)
        }
        require(wrappingKey.size == DATA_KEY_BYTES) { "Keystore MAC must produce a 256-bit key" }
        val cipherText = try {
            cipher(Cipher.ENCRYPT_MODE, wrappingKey, nonce).doFinal(dataKey)
        } finally {
            wrappingKey.fill(0)
        }
        return VaultRecord(
            formatVersion = FORMAT_VERSION,
            memoryKiB = config.memoryKiB,
            iterations = config.iterations,
            parallelism = config.parallelism,
            salt = salt,
            nonce = nonce,
            wrappedDataKey = cipherText
        )
    }

    fun unwrapDataKey(password: CharArray, record: VaultRecord): ByteArray {
        val config = Argon2idConfig.DEFAULT
        require(record.formatVersion == FORMAT_VERSION) { "Unsupported vault format" }
        require(record.memoryKiB == config.memoryKiB)
        require(record.iterations == config.iterations)
        require(record.parallelism == config.parallelism)
        require(record.salt.size == SALT_BYTES)
        require(record.nonce.size == GCM_NONCE_BYTES)
        val derived = kdf.derive(password, record.salt)
        val wrappingKey = try {
            deviceMac.authenticate(DOMAIN_SEPARATOR + derived)
        } finally {
            derived.fill(0)
        }
        require(wrappingKey.size == DATA_KEY_BYTES) { "Keystore MAC must produce a 256-bit key" }
        return try {
            cipher(Cipher.DECRYPT_MODE, wrappingKey, record.nonce).doFinal(record.wrappedDataKey)
        } finally {
            wrappingKey.fill(0)
        }
    }

    private fun cipher(mode: Int, key: ByteArray, nonce: ByteArray): Cipher =
        Cipher.getInstance(AES_GCM).apply {
            init(mode, SecretKeySpec(key, AES), GCMParameterSpec(GCM_TAG_BITS, nonce))
            updateAAD(AAD)
        }

    companion object {
        const val FORMAT_VERSION = 1
        const val DATA_KEY_BYTES = 32
        const val SALT_BYTES = 16
        const val GCM_NONCE_BYTES = 12
        private const val GCM_TAG_BITS = 128
        private const val AES = "AES"
        private const val AES_GCM = "AES/GCM/NoPadding"
        private val DOMAIN_SEPARATOR = "SECRET_NOTES_VAULT_KEK_V1".toByteArray(Charsets.UTF_8)
        private val AAD = "SECRET_NOTES_VAULT_RECORD_V1".toByteArray(Charsets.UTF_8)
    }
}

class VaultKeyMaterial internal constructor(bytes: ByteArray) : AutoCloseable {
    private val keyBytes = bytes.copyOf()
    private var closed = false

    @Synchronized
    fun copyForUse(): ByteArray {
        check(!closed) { "Vault key material has been closed" }
        return keyBytes.copyOf()
    }

    @Synchronized
    override fun close() {
        if (!closed) {
            keyBytes.fill(0)
            closed = true
        }
    }
}
