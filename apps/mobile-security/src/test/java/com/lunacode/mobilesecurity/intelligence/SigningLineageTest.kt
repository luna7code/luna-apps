package com.lunacode.mobilesecurity.intelligence

import org.junit.Assert.assertEquals
import org.junit.Test

class SigningLineageTest {
    @Test fun recognizesExactSignerMatch() {
        val installed = SigningLineage(setOf("B"), listOf("A", "B"), false)
        assertEquals(SigningLineageResult.EXACT_MATCH, SigningLineageComparator.compare(installed, installed))
    }

    @Test fun recognizesAndroidVerifiedForwardRotation() {
        val installed = SigningLineage(setOf("A"), listOf("A"), false)
        val candidate = SigningLineage(setOf("B"), listOf("A", "B"), false)
        assertEquals(SigningLineageResult.VERIFIED_FORWARD_ROTATION, SigningLineageComparator.compare(installed, candidate))
    }

    @Test fun flagsOldCertificateAsHistoricalNotCurrent() {
        val installed = SigningLineage(setOf("B"), listOf("A", "B"), false)
        val candidate = SigningLineage(setOf("A"), listOf("A"), false)
        assertEquals(SigningLineageResult.HISTORICAL_SIGNER, SigningLineageComparator.compare(installed, candidate))
    }

    @Test fun multiSignerPackagesRequireExactSignerSet() {
        val installed = SigningLineage(setOf("A", "B"), listOf("A", "B"), true)
        assertEquals(SigningLineageResult.MULTI_SIGNER_MATCH, SigningLineageComparator.compare(installed, installed))
        assertEquals(
            SigningLineageResult.MULTI_SIGNER_MISMATCH,
            SigningLineageComparator.compare(installed, SigningLineage(setOf("A"), listOf("A"), false))
        )
    }
}
