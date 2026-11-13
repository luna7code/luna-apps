package com.lunacode.mobilesecurity.scanner

import android.app.AppOpsManager
import android.os.Build

data class AppOpsDefinition(val key: String, val operation: String, val label: String, val severity: String)

object AppOpsAuditDefinitions {
    fun forSdk(sdk: Int): List<AppOpsDefinition> = buildList {
        add(AppOpsDefinition("overlay", AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, "отображение поверх других приложений", "MEDIUM"))
        add(AppOpsDefinition("usage", AppOpsManager.OPSTR_GET_USAGE_STATS, "доступ к статистике использования приложений", "MEDIUM"))
        add(AppOpsDefinition("write-settings", AppOpsManager.OPSTR_WRITE_SETTINGS, "изменение системных настроек", "MEDIUM"))
        add(AppOpsDefinition("install-packages", AppOpsManager.OPSTR_REQUEST_INSTALL_PACKAGES, "запрос установки неизвестных приложений", "LOW"))
        if (sdk >= Build.VERSION_CODES.R) {
            add(AppOpsDefinition("manage-storage", AppOpsManager.OPSTR_MANAGE_EXTERNAL_STORAGE, "широкий доступ к файлам", "MEDIUM"))
        }
    }
}

data class AppOpsEvidence(
    val packageName: String, val label: String, val key: String, val accessLabel: String,
    val mode: Int, val systemApp: Boolean, val severity: String
)

/** Pure mapping: only an explicit MODE_ALLOWED result becomes a finding. */
object AppOpsFindingPolicy {
    private const val MODE_ALLOWED = 0

    fun toFinding(evidence: AppOpsEvidence): AuditFinding? {
        if (evidence.packageName.isBlank() || evidence.mode != MODE_ALLOWED) return null
        return AuditFinding(
            id = "app-op-${evidence.key}-${evidence.packageName}",
            title = "Разрешён AppOps-доступ: ${evidence.accessLabel} — ${evidence.label}",
            detail = "Android сообщает, что операция разрешена для пакета ${evidence.packageName}. Это подтверждает доступную возможность, но не доказывает фактическое использование, сбор или передачу данных. Проверьте назначение приложения.",
            severity = if (evidence.systemApp) "INFO" else evidence.severity,
            source = evidence.packageName
        )
    }
}
