package com.lunacode.mobilesecurity.intelligence

import com.lunacode.mobilesecurity.scanner.AuditFinding

/** Current-state findings are timestamped at scan time, not at the unknown activation time. */
object AttackChainSignalAdapter {
    private val appOpIdPrefixes = listOf(
        "app-op-background-location-", "app-op-bluetooth-scan-", "app-op-read-media-images-",
        "app-op-read-media-video-", "app-op-read-media-audio-", "app-op-record-audio-",
        "app-op-fine-location-", "app-op-coarse-location-", "app-op-install-packages-",
        "app-op-manage-storage-", "app-op-post-notification-", "app-op-camera-", "app-op-overlay-",
        "app-op-usage-", "app-op-write-settings-"
    )

    fun detect(
        findings: List<AuditFinding>, changes: List<SentinelChange>, observedAtMillis: Long,
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
                "change:$index:${change.kind}:$packageName", packageName, type, observedAtMillis,
                change.level, change.title, EvidenceQuality.DIRECT_CHANGE
            )
        }

        findings.forEach { finding ->
            val isAppOp = finding.id.startsWith("app-op-")
            val packageName = when {
                isAppOp -> finding.source.takeIf { it.isNotBlank() }
                finding.id.startsWith("accessibility-") && finding.id != "accessibility-none" ->
                    finding.id.removePrefix("accessibility-")
                finding.id.startsWith("notification-listener-") && finding.id != "notification-listener-none" ->
                    finding.id.removePrefix("notification-listener-")
                finding.id.startsWith("admin-") && finding.id != "admin-none" ->
                    finding.id.removePrefix("admin-")
                finding.id.startsWith("input-method-") && finding.id != "input-method-none" ->
                    finding.id.removePrefix("input-method-")
                else -> null
            }?.takeIf { it.isNotBlank() } ?: return@forEach

            if ((finding.id.startsWith("input-method-") || isAppOp) && finding.severity == "INFO") return@forEach
            if (isAppOp) {
                val prefix = appOpIdPrefixes.firstOrNull { finding.id.startsWith(it) } ?: return@forEach
                val idPackage = finding.id.removePrefix(prefix)
                if (idPackage.isBlank() || idPackage != packageName) return@forEach
            }

            val type = when {
                finding.id.startsWith("notification-listener-") -> ThreatSignalType.NOTIFICATION_ACCESS_ENABLED
                finding.id.startsWith("input-method-") -> ThreatSignalType.INPUT_METHOD_ENABLED
                finding.id.startsWith("app-op-overlay-") -> ThreatSignalType.OVERLAY_ACCESS_ENABLED
                finding.id.startsWith("app-op-camera-") -> ThreatSignalType.APP_OP_CAMERA
                finding.id.startsWith("app-op-record-audio-") -> ThreatSignalType.APP_OP_MICROPHONE
                finding.id.startsWith("app-op-fine-location-") ||
                    finding.id.startsWith("app-op-coarse-location-") -> ThreatSignalType.APP_OP_LOCATION
                finding.id.startsWith("app-op-background-location-") -> ThreatSignalType.APP_OP_BACKGROUND_LOCATION
                finding.id.startsWith("app-op-bluetooth-scan-") -> ThreatSignalType.APP_OP_BLUETOOTH
                finding.id.startsWith("app-op-read-media-") -> ThreatSignalType.APP_OP_MEDIA
                finding.id.startsWith("app-op-install-packages-") -> ThreatSignalType.APP_OP_INSTALL_PACKAGES
                finding.id.startsWith("app-op-manage-storage-") -> ThreatSignalType.APP_OP_MANAGE_STORAGE
                isAppOp -> return@forEach
                else -> ThreatSignalType.SPECIAL_ACCESS_ENABLED
            }
            signals += ThreatSignal(
                "finding:${finding.id}", packageName, type, observedAtMillis,
                finding.severity, finding.title, EvidenceQuality.CURRENT_STATE
            )
        }
        return AttackChainDetector.detect(signals, windowMillis)
    }
}
