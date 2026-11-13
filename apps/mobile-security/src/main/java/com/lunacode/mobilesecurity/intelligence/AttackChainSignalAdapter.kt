package com.lunacode.mobilesecurity.intelligence

import com.lunacode.mobilesecurity.scanner.AuditFinding

/**
 * Converts Change Sentinel deltas and current-state findings into correlation
 * evidence. A current-state finding is timestamped at scan time; this does not
 * claim to know when the access was originally enabled.
 */
object AttackChainSignalAdapter {
    fun detect(
        findings: List<AuditFinding>,
        changes: List<SentinelChange>,
        observedAtMillis: Long,
        windowMillis: Long = 72L * 60L * 60L * 1000L
    ): List<AttackChain> {
        if (observedAtMillis < 0L) return emptyList()
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
                summary = change.title,
                evidenceQuality = EvidenceQuality.DIRECT_CHANGE
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

            if ((finding.id.startsWith("input-method-") || finding.id.startsWith("app-op-")) &&
                finding.severity == "INFO"
            ) return@forEach

            // For AppOps the package identity comes from the adapter's explicit source field.
            // Never correlate an AppOps finding whose source disagrees with its encoded ID.
            if (finding.id.startsWith("app-op-")) {
                val idPackage = finding.id.substringAfterLast('-')
                if (idPackage.contains('.') && idPackage != packageName) return@forEach
            }

            val type = when {
                finding.id.startsWith("notification-listener-") -> ThreatSignalType.NOTIFICATION_ACCESS_ENABLED
                finding.id.startsWith("input-method-") -> ThreatSignalType.INPUT_METHOD_ENABLED
                finding.id.startsWith("app-op-overlay-") -> ThreatSignalType.OVERLAY_ACCESS_ENABLED
                finding.id.startsWith("app-op-camera-") -> ThreatSignalType.APP_OP_CAMERA
                finding.id.startsWith("app-op-record_audio-") -> ThreatSignalType.APP_OP_MICROPHONE
                finding.id.startsWith("app-op-fine_location-") ||
                    finding.id.startsWith("app-op-coarse_location-") -> ThreatSignalType.APP_OP_LOCATION
                finding.id.startsWith("app-op-background_location-") -> ThreatSignalType.APP_OP_BACKGROUND_LOCATION
                finding.id.startsWith("app-op-bluetooth_scan-") -> ThreatSignalType.APP_OP_BLUETOOTH
                finding.id.startsWith("app-op-read_media_") -> ThreatSignalType.APP_OP_MEDIA
                else -> ThreatSignalType.SPECIAL_ACCESS_ENABLED
            }
            signals += ThreatSignal(
                id = "finding:${finding.id}",
                packageName = packageName,
                type = type,
                occurredAtMillis = observedAtMillis,
                severity = finding.severity,
                summary = finding.title,
                evidenceQuality = EvidenceQuality.CURRENT_STATE
            )
        }
        return AttackChainDetector.detect(signals, windowMillis)
    }
}
