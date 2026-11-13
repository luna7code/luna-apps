package com.lunacode.mobilesecurity.intelligence

import com.lunacode.mobilesecurity.scanner.AuditFinding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AttackChainSignalAdapterTest {
    @Test fun correlatesSentinelInstallWithObservedEnabledNotificationListener() {
        val chains = AttackChainSignalAdapter.detect(
            findings = listOf(AuditFinding(
                "notification-listener-com.example.spy", "Notification access enabled",
                "Observed enabled listener", "MEDIUM", "com.example.spy/Listener"
            )),
            changes = listOf(SentinelChange(
                "installed", "New app installed", "Package appeared in inventory",
                "MEDIUM", "com.example.spy"
            )),
            observedAtMillis = 10_000L
        )
        assertEquals(1, chains.size)
        assertEquals("com.example.spy", chains.single().packageName)
        assertEquals(78, chains.single().confidenceScore)
        assertEquals(82, chains.single().evidenceQualityScore)
        assertTrue(chains.single().explanation.contains("уведомлений"))
    }

    @Test fun correlatesNewlyInstalledAppWithCameraAndMicrophoneAppOps() {
        val chains = AttackChainSignalAdapter.detect(
            findings = listOf(
                AuditFinding("app-op-camera-com.example.surveillance", "Camera", "Allowed", "MEDIUM", "com.example.surveillance"),
                AuditFinding("app-op-record-audio-com.example.surveillance", "Microphone", "Allowed", "MEDIUM", "com.example.surveillance")
            ),
            changes = listOf(SentinelChange("installed", "Installed", "New app", "MEDIUM", "com.example.surveillance")),
            observedAtMillis = 26_000L
        )
        assertEquals(1, chains.size)
        assertEquals(3, chains.single().signals.size)
        assertTrue(chains.single().explanation.contains("камеры"))
        assertTrue(chains.single().explanation.contains("микрофона"))
    }

    @Test fun ignoresAppOpsWhenSourceAndFindingPackageDisagree() {
        val chains = AttackChainSignalAdapter.detect(
            findings = listOf(AuditFinding(
                "app-op-camera-com.example.one", "Camera allowed", "Allowed operation",
                "MEDIUM", "com.example.two"
            )),
            changes = listOf(SentinelChange("installed", "Installed", "New app", "MEDIUM", "com.example.one")),
            observedAtMillis = 26_000L
        )
        assertTrue(chains.isEmpty())
    }

    @Test fun ignoresAppOpsWithoutExplicitPackageSource() {
        val chains = AttackChainSignalAdapter.detect(
            findings = listOf(AuditFinding("app-op-camera-com.example.app", "Camera", "Allowed", "MEDIUM", "")),
            changes = listOf(SentinelChange("installed", "Installed", "New app", "MEDIUM", "com.example.app")),
            observedAtMillis = 1_000L
        )
        assertTrue(chains.isEmpty())
    }

    @Test fun ignoresSystemInputMethodWhenCorrelatingWithNotificationAccess() {
        val chains = AttackChainSignalAdapter.detect(
            findings = listOf(
                AuditFinding("input-method-com.android.keyboard", "System keyboard", "System IME", "INFO", "com.android.keyboard"),
                AuditFinding("notification-listener-com.android.keyboard", "Listener", "Enabled listener", "MEDIUM", "com.android.keyboard/Listener")
            ),
            changes = emptyList(),
            observedAtMillis = 30_000L
        )
        assertTrue(chains.isEmpty())
    }

    @Test fun doesNotTreatDeclaredButDisabledServicesAsEnabledAccess() {
        val chains = AttackChainSignalAdapter.detect(
            findings = listOf(AuditFinding(
                "declared-control-service-com.example.app", "Declared service",
                "Service exists in manifest", "INFO", "com.example.app"
            )),
            changes = emptyList(),
            observedAtMillis = 10_000L
        )
        assertTrue(chains.isEmpty())
    }

    @Test fun doesNotCorrelatePackageLessChanges() {
        val chains = AttackChainSignalAdapter.detect(
            findings = emptyList(),
            changes = listOf(SentinelChange("installed", "Install", "Package appeared", "MEDIUM")),
            observedAtMillis = 10_000L
        )
        assertTrue(chains.isEmpty())
    }

    @Test fun rejectsInvalidObservationTime() {
        assertTrue(AttackChainSignalAdapter.detect(emptyList(), emptyList(), -1L).isEmpty())
    }
}
