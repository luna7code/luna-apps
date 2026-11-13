package com.lunacode.mobilesecurity.intelligence

import com.lunacode.mobilesecurity.scanner.AuditFinding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AttackChainSignalAdapterTest {
    @Test
    fun correlatesSentinelInstallWithObservedEnabledNotificationListener() {
        val chains = AttackChainSignalAdapter.detect(
            findings = listOf(
                AuditFinding(
                    id = "notification-listener-com.example.spy",
                    title = "Notification access enabled",
                    detail = "Observed enabled listener",
                    severity = "MEDIUM",
                    source = "com.example.spy/Listener"
                )
            ),
            changes = listOf(
                SentinelChange(
                    kind = "installed",
                    title = "New app installed",
                    detail = "Package appeared in inventory",
                    level = "MEDIUM",
                    packageName = "com.example.spy"
                )
            ),
            observedAtMillis = 10_000L
        )
        assertEquals(1, chains.size)
        assertEquals("com.example.spy", chains.single().packageName)
        assertEquals(78, chains.single().confidenceScore)
        assertTrue(chains.single().explanation.contains("уведомлений"))
    }

    @Test
    fun correlatesNewInputMethodWithNotificationAccess() {
        val chains = AttackChainSignalAdapter.detect(
            findings = listOf(
                AuditFinding(
                    id = "input-method-com.example.keyboard",
                    title = "Enabled keyboard",
                    detail = "Enabled input method",
                    severity = "MEDIUM",
                    source = "com.example.keyboard/.Ime"
                ),
                AuditFinding(
                    id = "notification-listener-com.example.keyboard",
                    title = "Notification access",
                    detail = "Enabled listener",
                    severity = "MEDIUM",
                    source = "com.example.keyboard/.Listener"
                )
            ),
            changes = emptyList(),
            observedAtMillis = 20_000L
        )
        assertEquals(1, chains.size)
        assertEquals(68, chains.single().confidenceScore)
    }

    @Test
    fun ignoresSystemInputMethodWhenCorrelatingWithNotificationAccess() {
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

    @Test
    fun doesNotTreatDeclaredButDisabledServicesAsEnabledAccess() {
        val chains = AttackChainSignalAdapter.detect(
            findings = listOf(
                AuditFinding(
                    id = "declared-control-service-com.example.app",
                    title = "Declared service",
                    detail = "Service exists in manifest",
                    severity = "INFO",
                    source = "com.example.app"
                )
            ),
            changes = emptyList(),
            observedAtMillis = 10_000L
        )
        assertTrue(chains.isEmpty())
    }

    @Test
    fun doesNotCorrelatePackageLessChanges() {
        val chains = AttackChainSignalAdapter.detect(
            findings = emptyList(),
            changes = listOf(SentinelChange("installed", "Install", "Package appeared", "MEDIUM")),
            observedAtMillis = 10_000L
        )
        assertTrue(chains.isEmpty())
    }
}
