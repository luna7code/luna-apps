package com.lunacode.mobilesecurity.security

data class HistoryChainRecord(
    val id: Long,
    val previousTag: String,
    val chainTag: String?,
    val status: String
)

/**
 * Verifies links inside the retained window. The oldest retained record may
 * refer to a predecessor outside the window; later unsealed downgrades and
 * broken links are marked failed.
 */
object HistoryChainVerifier {
    fun verifyChronological(records: List<HistoryChainRecord>): List<HistoryChainRecord> {
        if (records.size < 2) return records
        val result = records.toMutableList()
        for (index in 1 until result.size) {
            val older = result[index - 1]
            val newer = result[index]
            val broken = when {
                newer.chainTag == null && older.chainTag != null -> true
                newer.chainTag != null && older.chainTag != null ->
                    newer.previousTag.isBlank() || newer.previousTag != older.chainTag
                newer.chainTag != null && older.chainTag == null ->
                    newer.previousTag.isNotBlank()
                else -> false
            }
            if (broken) result[index] = newer.copy(status = "FAILED")
        }
        return result
    }
}
