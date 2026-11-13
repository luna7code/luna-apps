package com.lunacode.mobilesecurity.intelligence

data class SigningLineage(
    val currentSigners: Set<String>,
    val history: List<String>,
    val multipleSigners: Boolean
)

enum class SigningLineageResult {
    EXACT_MATCH, VERIFIED_FORWARD_ROTATION, HISTORICAL_SIGNER,
    MULTI_SIGNER_MATCH, MULTI_SIGNER_MISMATCH, MISMATCH, UNKNOWN
}

/** Compares Android-verified signing histories in oldest-to-current order. */
object SigningLineageComparator {
    fun compare(installed: SigningLineage?, candidate: SigningLineage?): SigningLineageResult {
        if (installed == null || candidate == null ||
            installed.currentSigners.isEmpty() || candidate.currentSigners.isEmpty()
        ) return SigningLineageResult.UNKNOWN

        if (installed.multipleSigners || candidate.multipleSigners) {
            return if (installed.multipleSigners && candidate.multipleSigners &&
                installed.currentSigners == candidate.currentSigners
            ) SigningLineageResult.MULTI_SIGNER_MATCH
            else SigningLineageResult.MULTI_SIGNER_MISMATCH
        }
        if (installed.currentSigners == candidate.currentSigners) return SigningLineageResult.EXACT_MATCH

        val installedHistory = installed.history.ifEmpty { installed.currentSigners.toList() }
        val candidateHistory = candidate.history.ifEmpty { candidate.currentSigners.toList() }
        return when {
            candidateHistory.size > installedHistory.size &&
                candidateHistory.take(installedHistory.size) == installedHistory ->
                SigningLineageResult.VERIFIED_FORWARD_ROTATION
            installedHistory.size > candidateHistory.size &&
                installedHistory.take(candidateHistory.size) == candidateHistory ->
                SigningLineageResult.HISTORICAL_SIGNER
            else -> SigningLineageResult.MISMATCH
        }
    }
}
