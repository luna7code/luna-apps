package com.lunacode.mobilesecurity.intelligence

import com.lunacode.mobilesecurity.scanner.AuditFinding

/**
 * Converts observed Change Sentinel deltas and currently enabled access findings
 * into correlation evidence. A current-state finding is timestamped at scan time;
 * this does not claim to know when the access was originally enabled.
 */
object AttackChainSignalAdapter {
    fun detect(
        findings: List<AuditFinding>,
        changes: List<SentinelChange>,
        observedAtMillis: Long,
        windowMillis: Long = 72L * 60L * 60L * 1000L
    ): List<AttackChain> {
        val signals = mutableListOf<ThreatSignal>()

        changes.forEachIndexed { index, change ->
            val packageName = change.packageName?.takeIf { it.isNotBlank() } ?: return@forEachIndexed
            val type = when (change.kind) {
                "installed" -> ThreatSignalType.APP_INSTALLED
                "signer_changed", "signer_historical", "signer_change_unverified" -> ThreatSignalType.SIGNER_CHANGED
                "permissions_changed" -> ThreatSignalType.SENSITIVE_PERMISSION_CHANGED
                else -> null
            } ?: return@forEachIndexed
            signals += ThreatSignal(
                id = "change:$index:${change.kind}:$packageName",
                packageName = packageName,
                type = type,
                occurredAtMillis = observedAtMillis,
                severity = change.level,
                summary = change.title
            )
        }

        findings.forEach { finding ->
            val packageName = when {
                finding.id.startsWith("accessibility-") && finding.id != "accessibility-none" ->
                    finding.id.removePrefix("accessibility-")
                finding.id.startsWith("notification-listener-") && finding.id != "notification-listener-none" ->
                    finding.id.removePrefix("notification-listener-")
                finding.id.startsWith("admin-") && finding.id != "admin-none" ->
                    finding.id.removePrefix("admin-")
                finding.id.startsWith("input-method-") && finding.id != "input-method-none" ->
                    finding.id.removePrefix("input-method-")
                finding.id.startsWith("app-op-") -> finding.source.takeIf { it.isNotBlank() }
                else -> null
            }?.takeIf { it.isNotBlank() } ?: return@forEach
            if ((finding.id.startsWith("input-method-") || finding.id.startsWith("app-op-")) && finding.severity == "INFO") return@forEach
            val type = when {
                finding.id.startsWith("notification-listener-") -> ThreatSignalType.NOTIFICATION_ACCESS_ENABLED
                finding.id.startsWith("input-method-") -> ThreatSignalType.INPUT_METHOD_ENABLED
                finding.id.startsWith("app-op-overlay-") -> ThreatSignalType.OVERLAY_ACCESS_ENABLED
                else -> ThreatSignalType.SPECIAL_ACCESS_ENABLED
            }
            signals += ThreatSignal(
                id = "finding:${finding.id}",
                packageName = packageName,
                type = type,
                occurredAtMillis = observedAtMillis,
                severity = finding.severity,
                summary = finding.title
            )
        }
        return AttackChainDetector.detect(signals, windowMillis)
    }
}
