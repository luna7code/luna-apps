package com.lunacode.mobilesecurity.security

import android.content.Context
import org.json.JSONObject

sealed class TrustedBaselineRead {
    object Absent : TrustedBaselineRead()
    data class Valid(val report: String, val savedAt: Long) : TrustedBaselineRead()
    object Invalid : TrustedBaselineRead()
}

/** User-approved encrypted snapshot, independent from the rolling scan history. */
object TrustedBaselineStore {
    private const val PREFERENCES = "pro_sec_preferences"
    private const val REPORT_KEY = "trusted_baseline_encrypted_v1"
    private const val SAVED_AT_KEY = "trusted_baseline_saved_at_v1"

    private fun isValidReport(report: String): Boolean = runCatching {
        val json = JSONObject(report)
        json.optInt("schema") >= 2 && json.optJSONArray("apps") != null
    }.getOrDefault(false)

    fun save(context: Context, report: String, savedAt: Long = System.currentTimeMillis()) {
        val envelope = TrustedBaselineCodec.encode(
            report = report,
            savedAt = savedAt,
            validateReport = ::isValidReport,
            sign = LocalEvidenceSeal::sign
        )
        val encrypted = LocalPayloadCipher().encrypt(envelope)
        val persisted = context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(REPORT_KEY, encrypted)
            .putLong(SAVED_AT_KEY, savedAt)
            .commit()
        check(persisted) { "Trusted Baseline could not be persisted" }
    }

    fun load(context: Context): TrustedBaselineRead {
        val prefs = context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val encrypted = prefs.getString(REPORT_KEY, null) ?: return TrustedBaselineRead.Absent
        val savedAt = prefs.getLong(SAVED_AT_KEY, 0L)
        return runCatching {
            val decrypted = LocalPayloadCipher().decrypt(encrypted)
            TrustedBaselineCodec.decode(
                payload = decrypted,
                savedAt = savedAt,
                validateReport = ::isValidReport,
                verify = LocalEvidenceSeal::verify
            )
        }.getOrElse { TrustedBaselineRead.Invalid }
    }
}
