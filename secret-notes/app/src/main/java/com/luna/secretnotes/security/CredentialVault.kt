package com.luna.secretnotes.security

import android.content.Context
import java.security.SecureRandom
import javax.crypto.AEADBadTagException

/**
 * Local credential and data-key lifecycle.
 *
 * Argon2id derives a password key, then an Android Keystore HMAC key binds that
 * derivation to this installation. AES-256-GCM wraps the random 256-bit DEK.
 * Expensive credential operations must run off the main thread.
 */
class CredentialVault(
    context: Context,
    private val recordStore: VaultRecordStore = SharedPreferencesVaultRecordStore(context),
    private val protector: VaultKeyProtector = VaultKeyProtector(
        Argon2idKdf(), AndroidKeystoreMac(VAULT_KEY_ALIAS)
    ),
    private val lockout: AttemptLockout = AttemptLockout(SharedPreferencesLockoutStateStore(context)),
    private val random: SecureRandom = SecureRandom()
) {
    fun hasCredential(): Boolean = recordStore.read() != null

    fun createCredential(secret: CharArray): VaultKeyMaterial {
        try {
            PasswordPolicy.validate(secret)
            check(!hasCredential()) { "Credential is already configured" }
            val dataKey = ByteArray(VaultKeyProtector.DATA_KEY_BYTES).also(random::nextBytes)
            return try {
                val record = protector.createRecord(secret, dataKey)
                recordStore.write(record)
                lockout.recordSuccess()
                VaultKeyMaterial(dataKey).also { dataKey.fill(0) }
            } catch (error: Throwable) {
                dataKey.fill(0)
                throw error
            }
        } finally {
            secret.fill('\u0000')
        }
    }

    /** Enroll a DEK restored from a verified portable backup on a new device. */
    fun importCredential(secret: CharArray, importedDataKey: ByteArray): VaultKeyMaterial {
        try {
            PasswordPolicy.validate(secret)
            require(importedDataKey.size == VaultKeyProtector.DATA_KEY_BYTES)
            check(!hasCredential()) { "A vault already exists; import into a fresh vault only" }
            val record = protector.createRecord(secret, importedDataKey)
            recordStore.write(record)
            lockout.recordSuccess()
            return VaultKeyMaterial(importedDataKey)
        } finally {
            secret.fill('\u0000')
            importedDataKey.fill(0)
        }
    }

    /**
     * Rewraps a verified portable-backup DEK under this installation's Keystore key,
     * but only after the currently configured credential has been confirmed.
     */
    fun replaceVaultKey(currentSecret: CharArray, importedDataKey: ByteArray): VaultKeyMaterial {
        var credentialForRewrap: CharArray? = null
        var importedCopy: ByteArray? = null
        var unlocked: VaultKeyMaterial? = null
        try {
            require(importedDataKey.size == VaultKeyProtector.DATA_KEY_BYTES)
            credentialForRewrap = currentSecret.copyOf()
            unlocked = unlock(currentSecret)
            importedCopy = importedDataKey.copyOf()
            val newRecord = protector.createRecord(credentialForRewrap, importedCopy)
            recordStore.write(newRecord)
            lockout.recordSuccess()
            return VaultKeyMaterial(importedCopy)
        } finally {
            currentSecret.fill('\u0000')
            credentialForRewrap?.fill('\u0000')
            importedDataKey.fill(0)
            importedCopy?.fill(0)
            unlocked?.close()
        }
    }

    fun unlock(secret: CharArray): VaultKeyMaterial {
        try {
            lockout.ensureAllowed()
            val record = recordStore.read() ?: error("Credential is not configured")
            val dataKey = try {
                protector.unwrapDataKey(secret, record)
            } catch (_: AEADBadTagException) {
                lockout.recordFailure()
                throw InvalidCredentialException()
            }
            lockout.recordSuccess()
            return VaultKeyMaterial(dataKey).also { dataKey.fill(0) }
        } finally {
            secret.fill('\u0000')
        }
    }

    fun changeCredential(oldSecret: CharArray, newSecret: CharArray): VaultKeyMaterial {
        try {
            PasswordPolicy.validate(newSecret)
            val unlocked = unlock(oldSecret)
            val dataKey = unlocked.copyForUse()
            return try {
                val newRecord = protector.createRecord(newSecret, dataKey)
                recordStore.write(newRecord)
                lockout.recordSuccess()
                VaultKeyMaterial(dataKey)
            } finally {
                unlocked.close()
                dataKey.fill(0)
            }
        } finally {
            oldSecret.fill('\u0000')
            newSecret.fill('\u0000')
        }
    }

    private companion object {
        const val VAULT_KEY_ALIAS = "secret_notes_vault_kek_v1"
    }
}
