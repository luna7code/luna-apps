package com.luna.secretnotes.security

import android.content.Context
import android.util.Base64

interface VaultRecordStore {
    fun read(): VaultRecord?
    fun write(record: VaultRecord)
    fun clear()
}

class SharedPreferencesVaultRecordStore(context: Context) : VaultRecordStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    override fun read(): VaultRecord? {
        if (!preferences.contains(KEY_SALT)) return null
        return VaultRecord(
            formatVersion = preferences.getInt(KEY_VERSION, -1),
            memoryKiB = preferences.getInt(KEY_MEMORY, -1),
            iterations = preferences.getInt(KEY_ITERATIONS, -1),
            parallelism = preferences.getInt(KEY_PARALLELISM, -1),
            salt = decode(KEY_SALT),
            nonce = decode(KEY_NONCE),
            wrappedDataKey = decode(KEY_WRAPPED_KEY)
        )
    }

    override fun write(record: VaultRecord) {
        val saved = preferences.edit()
            .putInt(KEY_VERSION, record.formatVersion)
            .putInt(KEY_MEMORY, record.memoryKiB)
            .putInt(KEY_ITERATIONS, record.iterations)
            .putInt(KEY_PARALLELISM, record.parallelism)
            .putString(KEY_SALT, encode(record.salt))
            .putString(KEY_NONCE, encode(record.nonce))
            .putString(KEY_WRAPPED_KEY, encode(record.wrappedDataKey))
            .commit()
        check(saved) { "Unable to persist vault metadata" }
    }

    override fun clear() {
        check(preferences.edit().clear().commit()) { "Unable to clear vault metadata" }
    }

    private fun encode(value: ByteArray): String = Base64.encodeToString(value, Base64.NO_WRAP)
    private fun decode(key: String): ByteArray =
        Base64.decode(preferences.getString(key, null) ?: error("Missing vault metadata: $key"), Base64.NO_WRAP)

    private companion object {
        const val PREFS_NAME = "secret_notes_vault_metadata"
        const val KEY_VERSION = "format_version"
        const val KEY_MEMORY = "argon_memory_kib"
        const val KEY_ITERATIONS = "argon_iterations"
        const val KEY_PARALLELISM = "argon_parallelism"
        const val KEY_SALT = "argon_salt"
        const val KEY_NONCE = "wrap_nonce"
        const val KEY_WRAPPED_KEY = "wrapped_data_key"
    }
}
