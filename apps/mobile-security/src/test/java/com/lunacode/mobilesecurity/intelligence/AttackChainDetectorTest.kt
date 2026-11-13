package com.lunacode.mobilesecurity.intelligence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AttackChainDetectorTest {
    private fun signal(id: String, type: ThreatSignalType, at: Long,
        packageName: String? = "com.example.app", severity: String = "MEDIUM") =
        ThreatSignal(id, packageName, type, at, severity, id)

    @Test fun correlatesNewInstallWithSpecialAccess() {
        val result = AttackChainDetector.detect(listOf(
            signal("install", ThreatSignalType.APP_INSTALLED, 1_000L),
            signal("access", ThreatSignalType.SPECIAL_ACCESS_ENABLED, 2_000L, severity = "HIGH")
        ))
        assertEquals(1, result.size)
        assertEquals(78, result.single().confidenceScore)
        assertEquals("HIGH", result.single().severity)
    }

    @Test fun signerChangeWithSensitiveAccessHasHighestRuleScore() {
        val result = AttackChainDetector.detect(listOf(
            signal("signer", ThreatSignalType.SIGNER_CHANGED, 1_000L),
            signal("overlay", ThreatSignalType.OVERLAY_ACCESS_ENABLED, 1_500L)
        ))
        assertEquals(90, result.single().confidenceScore)
    }

    @Test fun doesNotCorrelateDifferentPackages() {
        val result = AttackChainDetector.detect(listOf(
            signal("install", ThreatSignalType.APP_INSTALLED, 1_000L, "com.example.one"),
            signal("access", ThreatSignalType.SPECIAL_ACCESS_ENABLED, 2_000L, "com.example.two")
        ))
        assertTrue(result.isEmpty())
    }

    @Test fun doesNotCorrelateSignalsOutsideTimeWindow() {
        val result = AttackChainDetector.detect(listOf(
            signal("install", ThreatSignalType.APP_INSTALLED, 1_000L),
            signal("access", ThreatSignalType.SPECIAL_ACCESS_ENABLED, 300_000_000L)
        ))
        assertTrue(result.isEmpty())
    }

    @Test fun ignoresSignalsWithoutPackageIdentity() {
        val result = AttackChainDetector.detect(listOf(
            signal("install", ThreatSignalType.APP_INSTALLED, 1_000L, null),
            signal("access", ThreatSignalType.SPECIAL_ACCESS_ENABLED, 2_000L, null)
        ))
        assertTrue(result.isEmpty())
    }

    @Test fun doesNotCreateChainForUnrelatedSignalTypes() {
        val result = AttackChainDetector.detect(listOf(
            signal("install", ThreatSignalType.APP_INSTALLED, 1_000L),
            signal("permission", ThreatSignalType.SENSITIVE_PERMISSION_CHANGED, 2_000L)
        ))
        assertTrue(result.isEmpty())
    }

    @Test fun invalidWindowReturnsNoChains() {
        assertTrue(AttackChainDetector.detect(emptyList(), -1L).isEmpty())
    }

    @Test fun deduplicatesSignalIds() {
        val result = AttackChainDetector.detect(listOf(
            signal("same", ThreatSignalType.APP_INSTALLED, 1_000L),
            signal("same", ThreatSignalType.APP_INSTALLED, 1_000L),
            signal("access", ThreatSignalType.SPECIAL_ACCESS_ENABLED, 2_000L)
        ))
        assertEquals(1, result.single().signals.count { it.id == "same" })
    }
}
