package com.lunacode.mobilesecurity.intelligence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AttackChainDetectorTest {
    private fun signal(
        id: String,
        type: ThreatSignalType,
        at: Long,
        packageName: String? = "com.example.app",
        severity: String = "MEDIUM",
        quality: EvidenceQuality = EvidenceQuality.DIRECT_CHANGE
    ) = ThreatSignal(id, packageName, type, at, severity, id, quality)

    @Test fun correlatesNewInstallWithSensitiveCameraAccess() {
        val result = AttackChainDetector.detect(listOf(
            signal("install", ThreatSignalType.APP_INSTALLED, 1_000L),
            signal("camera", ThreatSignalType.APP_OP_CAMERA, 2_000L, severity = "HIGH")
        ))
        assertEquals(1, result.size)
        assertEquals(78, result.single().confidenceScore)
        assertEquals(100, result.single().evidenceQualityScore)
        assertEquals("HIGH", result.single().severity)
    }

    @Test fun newAppRegisteredAsInputMethodGetsDedicatedExplanation() {
        val result = AttackChainDetector.detect(listOf(
            signal("install", ThreatSignalType.APP_INSTALLED, 1_000L),
            signal("ime", ThreatSignalType.INPUT_METHOD_ENABLED, 2_000L)
        ))
        assertEquals(74, result.single().confidenceScore)
        assertEquals("Новое приложение зарегистрировано как способ ввода", result.single().title)
        assertTrue(result.single().explanation.contains("способа ввода"))
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

    @Test fun rejectsNegativeAndFutureOverflowProneTimestamps() {
        val result = AttackChainDetector.detect(listOf(
            signal("negative", ThreatSignalType.APP_INSTALLED, -1L),
            signal("access", ThreatSignalType.SPECIAL_ACCESS_ENABLED, 2L),
            signal("install", ThreatSignalType.APP_INSTALLED, Long.MAX_VALUE),
            signal("mic", ThreatSignalType.APP_OP_MICROPHONE, Long.MAX_VALUE)
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

    @Test fun deduplicatesRepeatedSignalIdsAndIgnoresRepeatedSameType() {
        val result = AttackChainDetector.detect(listOf(
            signal("same", ThreatSignalType.APP_INSTALLED, 1_000L),
            signal("same", ThreatSignalType.APP_INSTALLED, 1_000L),
            signal("install-again", ThreatSignalType.APP_INSTALLED, 1_100L),
            signal("access", ThreatSignalType.APP_OP_CAMERA, 2_000L)
        ))
        assertTrue(result.isNotEmpty())
        assertEquals(1, result.first().signals.count { it.id == "same" })
        assertTrue(result.first().signals.count { it.type == ThreatSignalType.APP_INSTALLED } <= 2)
    }

    @Test fun evidenceQualityIsReportedSeparatelyFromRuleStrength() {
        val result = AttackChainDetector.detect(listOf(
            signal("install", ThreatSignalType.APP_INSTALLED, 1_000L, quality = EvidenceQuality.DIRECT_CHANGE),
            signal("camera", ThreatSignalType.APP_OP_CAMERA, 2_000L, quality = EvidenceQuality.CURRENT_STATE)
        ))
        assertEquals(78, result.single().confidenceScore)
        assertEquals(82, result.single().evidenceQualityScore)
    }

    @Test fun inferredEvidenceCannotUpgradeRuleStrength() {
        val result = AttackChainDetector.detect(listOf(
            signal("install", ThreatSignalType.APP_INSTALLED, 1_000L, quality = EvidenceQuality.INFERRED),
            signal("camera", ThreatSignalType.APP_OP_CAMERA, 2_000L, quality = EvidenceQuality.INFERRED)
        ))
        assertEquals(78, result.single().confidenceScore)
        assertEquals(30, result.single().evidenceQualityScore)
    }
}
