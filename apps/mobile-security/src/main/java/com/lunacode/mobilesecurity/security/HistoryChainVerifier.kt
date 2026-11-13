package com.lunacode.mobilesecurity.security

data class HistoryChainRecord(
    val id: Long, val previousTag: String, val chainTag: String?, val status: String
)

/** Verifies links between retained reports; the oldest retained record may have an unseen predecessor. */
object HistoryChainVerifier {
    fun verifyChronological(records: List<HistoryChainRecord>): List<HistoryChainRecord> {
        if (records.size < 2) return records
        val result = records.toMutableList()
        for (index in 1 until result.size) {
            val older = result[index - 1]
            val newer = result[index]
            if (older.chainTag != null && newer.chainTag != null &&
                (newer.previousTag.isBlank() || newer.previousTag != older.chainTag)
            ) {
                result[index] = newer.copy(status = "FAILED")
            }
        }
        return result
    }
}
