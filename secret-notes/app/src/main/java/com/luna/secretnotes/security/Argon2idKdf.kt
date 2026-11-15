package com.luna.secretnotes.security

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

data class Argon2idConfig(
    val memoryKiB: Int = 64 * 1024,
    val iterations: Int = 3,
    val parallelism: Int = 1,
    val outputBytes: Int = 32
) {
    init {
        require(memoryKiB >= 8 * 1024)
        require(iterations >= 1)
        require(parallelism >= 1)
        require(outputBytes >= 16)
    }

    companion object {
        val DEFAULT = Argon2idConfig()
    }
}

fun interface PasswordKdf {
    fun derive(password: CharArray, salt: ByteArray): ByteArray
}

/**
 * Argon2id v1.3. Run off the main thread: default settings use 64 MiB and 3 passes.
 */
class Argon2idKdf(
    private val config: Argon2idConfig = Argon2idConfig.DEFAULT
) : PasswordKdf {
    override fun derive(password: CharArray, salt: ByteArray): ByteArray {
        require(salt.size >= 16) { "Argon2id salt must be at least 16 bytes" }
        val parameters = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withSalt(salt)
            .withIterations(config.iterations)
            .withMemoryAsKB(config.memoryKiB)
            .withParallelism(config.parallelism)
            .build()
        val output = ByteArray(config.outputBytes)
        try {
            Argon2BytesGenerator().apply { init(parameters) }
                .generateBytes(password, output)
            return output
        } catch (error: Throwable) {
            output.fill(0)
            throw error
        } finally {
            parameters.clear()
        }
    }
}
