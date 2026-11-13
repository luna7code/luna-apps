package com.lunacode.mobilesecurity.scanner

import android.app.AppOpsManager
import android.os.Build

data class AppOpsDefinition(val key: String, val operation: String, val label: String, val severity: String)

object AppOpsAuditDefinitions {
    fun forSdk(sdk: Int): List<AppOpsDefinition> = buildList {
        add(AppOpsDefinition("overlay", AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, "отображение поверх других приложений", "MEDIUM"))
        add(AppOpsDefinition("usage", AppOpsManager.OPSTR_GET_USAGE_STATS, "доступ к статистике использования приложений", "MEDIUM"))
        add(AppOpsDefinition("write-settings", AppOpsManager.OPSTR_WRITE_SETTINGS, "изменение системных настроек", "MEDIUM"))
        if (sdk >= Build.VERSION_CODES.M) {
            add(AppOpsDefinition("camera", "android:camera", "доступ к камере", "MEDIUM"))
            add(AppOpsDefinition("record-audio", "android:record_audio", "доступ к микрофону", "MEDIUM"))
            add(AppOpsDefinition("fine-location", "android:fine_location", "точное местоположение", "MEDIUM"))
            add(AppOpsDefinition("coarse-location", "android:coarse_location", "приблизительное местоположение", "LOW"))
        }
        if (sdk >= Build.VERSION_CODES.O) {
            add(AppOpsDefinition("install-packages", "android:request_install_packages", "запрос установки неизвестных приложений", "LOW"))
        }
        if (sdk >= Build.VERSION_CODES.Q) {
            add(AppOpsDefinition("background-location", "android:background_location", "местоположение в фоне", "MEDIUM"))
        }
        if (sdk >= Build.VERSION_CODES.R) {
            add(AppOpsDefinition("manage-storage", "android:manage_external_storage", "широкий доступ к файлам", "MEDIUM"))
        }
        if (sdk >= Build.VERSION_CODES.S) {
            add(AppOpsDefinition("bluetooth-scan", "android:bluetooth_scan", "поиск Bluetooth-устройств", "LOW"))
        }
        if (sdk >= Build.VERSION_CODES.TIRAMISU) {
            add(AppOpsDefinition("post-notification", "android:post_notification", "публикация уведомлений", "LOW"))
            add(AppOpsDefinition("read-media-images", "android:read_media_images", "чтение изображений", "LOW"))
            add(AppOpsDefinition("read-media-video", "android:read_media_video", "чтение видео", "LOW"))
            add(AppOpsDefinition("read-media-audio", "android:read_media_audio", "чтение аудиофайлов", "LOW"))
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
