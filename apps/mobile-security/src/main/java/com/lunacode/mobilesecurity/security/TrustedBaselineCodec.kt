package com.lunacode.mobilesecurity.security

/**
 * Pure policy around the encrypted Trusted Baseline envelope.
 * Android Keystore and report-schema validation are injected at the boundary,
 * keeping tamper and policy tests deterministic on the local JVM.
 */
object TrustedBaselineCodec {
    fun encode(
        report: String,
        savedAt: Long,
        validateReport: (String) -> Boolean,
        sign: (String) -> String
    ): String {
        require(savedAt > 0L) { "Trusted Baseline timestamp must be positive" }
        require(report.isNotBlank() && validateReport(report)) { "Trusted Baseline report is invalid" }
        val material = baselineMaterial(savedAt, report)
        val tag = sign(material)
        require(tag.isNotBlank()) { "Trusted Baseline signature is missing" }
        return ReportEnvelopeCodec.encode("", report, tag)
    }

    fun decode(
        payload: String,
        savedAt: Long,
        validateReport: (String) -> Boolean,
        verify: (String, String) -> Boolean
    ): TrustedBaselineRead {
        if (savedAt <= 0L) return TrustedBaselineRead.Invalid
        return runCatching {
            val envelope = ReportEnvelopeCodec.decode(payload)
            require(envelope.wrapped && envelope.previousTag.isEmpty())
            val tag = envelope.chainTag?.takeIf { it.isNotBlank() } ?: error("Missing baseline signature")
            require(envelope.report.isNotBlank() && validateReport(envelope.report))
            require(verify(baselineMaterial(savedAt, envelope.report), tag))
            TrustedBaselineRead.Valid(envelope.report, savedAt)
        }.getOrElse { TrustedBaselineRead.Invalid }
    }

    private fun baselineMaterial(savedAt: Long, report: String): String =
        "PROSEC-TRUSTED-BASELINE-1\n$savedAt\n$report"
}
