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

    fun save(context: Context, report: String, savedAt: Long = System.currentTimeMillis()) {
        val json = JSONObject(report)
        require(json.optInt("schema") >= 2)
        require(json.optJSONArray("apps") != null)
        val tag = LocalEvidenceSeal.sign(ReportEnvelopeCodec.material("", report))
        val encrypted = LocalPayloadCipher().encrypt(ReportEnvelopeCodec.encode("", report, tag))
        context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit().putString(REPORT_KEY, encrypted).putLong(SAVED_AT_KEY, savedAt).apply()
    }

    fun load(context: Context): TrustedBaselineRead {
        val prefs = context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val encrypted = prefs.getString(REPORT_KEY, null) ?: return TrustedBaselineRead.Absent
        val savedAt = prefs.getLong(SAVED_AT_KEY, 0L)
        return runCatching {
            val envelope = ReportEnvelopeCodec.decode(LocalPayloadCipher().decrypt(encrypted))
            require(envelope.wrapped && !envelope.chainTag.isNullOrBlank() && envelope.report.isNotBlank())
            require(LocalEvidenceSeal.verify(ReportEnvelopeCodec.material(envelope.previousTag, envelope.report), envelope.chainTag))
            val json = JSONObject(envelope.report)
            require(json.optInt("schema") >= 2 && json.optJSONArray("apps") != null)
            TrustedBaselineRead.Valid(envelope.report, savedAt)
        }.getOrElse { TrustedBaselineRead.Invalid }
    }
}
