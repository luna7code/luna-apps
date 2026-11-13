package com.lunacode.mobilesecurity.security

data class HistoryChainRecord(
    val id: Long,
    val previousTag: String,
    val chainTag: String?,
    val status: String
)

/**
 * Verifies the retained chronological window without silently sorting it.
 * The first retained row may reference a predecessor outside the window.
 * Any malformed link, duplicate/non-monotonic row ID, or failed predecessor
 * invalidates the affected suffix; records are never deleted or rewritten.
 */
object HistoryChainVerifier {
    fun verifyChronological(records: List<HistoryChainRecord>): List<HistoryChainRecord> {
        if (records.isEmpty()) return emptyList()
        val result = records.toMutableList()

        result.indices.forEach { index ->
            val current = result[index]
            val malformedCurrent = when {
                current.id < 0L -> true
                current.chainTag != null && current.chainTag.isBlank() -> true
                current.status == "VERIFIED" && current.chainTag.isNullOrBlank() -> true
                current.status !in setOf("VERIFIED", "LEGACY", "UNSEALED", "UNVERIFIABLE", "FAILED") -> true
                else -> false
            }
            if (malformedCurrent) result[index] = current.copy(status = "FAILED")
        }

        for (index in 1 until result.size) {
            val older = result[index - 1]
            val newer = result[index]
            val broken = when {
                newer.id <= older.id -> true
                older.status == "FAILED" || older.status == "UNVERIFIABLE" -> true
                newer.chainTag == null && older.chainTag != null -> true
                newer.chainTag != null && older.chainTag != null ->
                    newer.previousTag.isBlank() || newer.previousTag != older.chainTag
                newer.chainTag != null && older.chainTag == null ->
                    newer.previousTag.isNotBlank() || older.status != "LEGACY"
                else -> false
            }
            if (broken) result[index] = newer.copy(status = "FAILED")
        }
        return result
    }
}
