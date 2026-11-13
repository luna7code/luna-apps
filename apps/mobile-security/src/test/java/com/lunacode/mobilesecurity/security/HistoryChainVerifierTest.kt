package com.lunacode.mobilesecurity.security

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryChainVerifierTest {
    @Test
    fun acceptsCorrectAdjacentLinks() {
        val result = HistoryChainVerifier.verifyChronological(
            listOf(
                HistoryChainRecord(1, "", "A", "VERIFIED"),
                HistoryChainRecord(2, "A", "B", "VERIFIED"),
                HistoryChainRecord(3, "B", "C", "VERIFIED")
            )
        )
        assertEquals(listOf("VERIFIED", "VERIFIED", "VERIFIED"), result.map { it.status })
    }

    @Test
    fun marksBrokenAdjacentLinkAsFailed() {
        val result = HistoryChainVerifier.verifyChronological(
            listOf(
                HistoryChainRecord(1, "", "A", "VERIFIED"),
                HistoryChainRecord(2, "WRONG", "B", "VERIFIED")
            )
        )
        assertEquals("FAILED", result.last().status)
    }

    @Test
    fun permitsOldestRetainedRecordToHaveAnUnseenPredecessor() {
        val result = HistoryChainVerifier.verifyChronological(
            listOf(HistoryChainRecord(10, "outside-window", "A", "VERIFIED"))
        )
        assertEquals("VERIFIED", result.single().status)
    }
}
