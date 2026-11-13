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

    @Test
    fun detectsDowngradeFromSealedToUnsealedRecord() {
        val result = HistoryChainVerifier.verifyChronological(
            listOf(
                HistoryChainRecord(1, "", "A", "VERIFIED"),
                HistoryChainRecord(2, "", null, "LEGACY")
            )
        )
        assertEquals("FAILED", result.last().status)
    }

    @Test
    fun acceptsFirstSealedRecordAfterLegacyHistoryOnlyWhenItStartsANewChain() {
        val valid = HistoryChainVerifier.verifyChronological(
            listOf(
                HistoryChainRecord(1, "", null, "LEGACY"),
                HistoryChainRecord(2, "", "B", "VERIFIED")
            )
        )
        assertEquals("VERIFIED", valid.last().status)

        val invalid = HistoryChainVerifier.verifyChronological(
            listOf(
                HistoryChainRecord(1, "", null, "FAILED"),
                HistoryChainRecord(2, "A", "B", "VERIFIED")
            )
        )
        assertEquals("FAILED", invalid.last().status)
    }
}
