package com.lunacode.mobilesecurity.scanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppOpsFindingPolicyTest {
    private fun evidence(mode: Int, systemApp: Boolean = false) = AppOpsEvidence(
        "com.example.app", "Example", "overlay", "overlay", mode, systemApp, "MEDIUM"
    )

    @Test fun reportsOnlyExplicitlyAllowedOperation() {
        assertNull(AppOpsFindingPolicy.toFinding(evidence(1)))
        val finding = AppOpsFindingPolicy.toFinding(evidence(0))
        assertEquals("MEDIUM", finding?.severity)
        assertTrue(finding?.id?.contains("com.example.app") == true)
    }

    @Test fun deniedIgnoredErroredAndDefaultModesAreNotReportedAsGranted() {
        for (mode in listOf(1, 2, 3, 4)) {
            assertNull(AppOpsFindingPolicy.toFinding(evidence(mode)))
        }
    }

    @Test fun systemAppEvidenceIsInformational() {
        assertEquals("INFO", AppOpsFindingPolicy.toFinding(evidence(0, true))?.severity)
    }

    @Test fun evidenceDoesNotClaimOperationWasUsed() {
        assertTrue(AppOpsFindingPolicy.toFinding(evidence(0))!!.detail.contains("не доказывает фактическое использование"))
    }

    @Test fun ignoresMissingPackageIdentity() {
        assertNull(AppOpsFindingPolicy.toFinding(evidence(0).copy(packageName = "")))
    }
}
