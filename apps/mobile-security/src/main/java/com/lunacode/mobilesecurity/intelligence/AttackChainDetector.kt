package com.lunacode.mobilesecurity.intelligence

enum class ThreatSignalType {
    APP_INSTALLED, SPECIAL_ACCESS_ENABLED, SENSITIVE_PERMISSION_CHANGED, SIGNER_CHANGED,
    INPUT_METHOD_ENABLED, NOTIFICATION_ACCESS_ENABLED, OVERLAY_ACCESS_ENABLED,
    VPN_CONFIGURATION_CHANGED, APP_OP_CAMERA, APP_OP_MICROPHONE, APP_OP_LOCATION,
    APP_OP_BACKGROUND_LOCATION, APP_OP_BLUETOOTH, APP_OP_MEDIA, APP_OP_INSTALL_PACKAGES,
    APP_OP_MANAGE_STORAGE
}

enum class EvidenceQuality(val score: Int) {
    DIRECT_CHANGE(100),
    CURRENT_STATE(65),
    INFERRED(30)
}

data class ThreatSignal(
    val id: String, val packageName: String?, val type: ThreatSignalType,
    val occurredAtMillis: Long, val severity: String, val summary: String,
    val evidenceQuality: EvidenceQuality = EvidenceQuality.DIRECT_CHANGE
)

data class AttackChain(
    val packageName: String, val signals: List<ThreatSignal>, val title: String,
    val explanation: String, /** Rule strength, not a probability of compromise. */
    val confidenceScore: Int, /** Mean provenance quality, independent from rule strength. */
    val evidenceQualityScore: Int, val severity: String
)

object AttackChainDetector {
    private const val DEFAULT_WINDOW_MILLIS = 72L * 60L * 60L * 1000L

    fun detect(signals: List<ThreatSignal>, windowMillis: Long = DEFAULT_WINDOW_MILLIS): List<AttackChain> {
        if (windowMillis < 0L) return emptyList()
        val result = mutableListOf<AttackChain>()
        signals.asSequence()
            .filter { !it.packageName.isNullOrBlank() && it.id.isNotBlank() && it.occurredAtMillis >= 0L }
            .groupBy { it.packageName!! }.toSortedMap()
            .forEach { (packageName, packageSignals) ->
                val ordered = packageSignals.distinctBy { it.id }.sortedBy { it.occurredAtMillis }
                correlatedGroups(ordered, windowMillis).forEach { group ->
                    val types = group.map { it.type }.toSet()
                    val score = evidenceScore(types)
                    if (score > 0) result += AttackChain(
                        packageName, group, chainTitle(types), chainExplanation(types), score,
                        group.map { it.evidenceQuality.score }.average().toInt(),
                        group.maxByOrNull { severityRank(it.severity) }?.severity ?: "INFO"
                    )
                }
            }
        return result.sortedWith(
            compareByDescending<AttackChain> { it.confidenceScore }
                .thenByDescending { it.evidenceQualityScore }
                .thenBy { it.packageName }
                .thenBy { it.signals.firstOrNull()?.occurredAtMillis ?: 0L }
        )
    }

    private fun correlatedGroups(signals: List<ThreatSignal>, windowMillis: Long): List<List<ThreatSignal>> {
        val groups = mutableListOf<List<ThreatSignal>>()
        val seen = mutableSetOf<Set<String>>()
        for (start in signals.indices) {
            val group = signals.drop(start).takeWhile {
                withinWindow(signals[start].occurredAtMillis, it.occurredAtMillis, windowMillis)
            }
            val types = group.map { it.type }.toSet()
            if (types.size >= 2 && evidenceScore(types) > 0 && seen.add(group.map { it.id }.toSet())) {
                groups += group
            }
        }
        return groups
    }

    private fun withinWindow(start: Long, end: Long, windowMillis: Long): Boolean =
        start >= 0L && end >= start && end - start <= windowMillis

    private fun isSensitiveAccess(type: ThreatSignalType): Boolean = when (type) {
        ThreatSignalType.SPECIAL_ACCESS_ENABLED, ThreatSignalType.INPUT_METHOD_ENABLED,
        ThreatSignalType.NOTIFICATION_ACCESS_ENABLED, ThreatSignalType.OVERLAY_ACCESS_ENABLED,
        ThreatSignalType.APP_OP_CAMERA, ThreatSignalType.APP_OP_MICROPHONE,
        ThreatSignalType.APP_OP_LOCATION, ThreatSignalType.APP_OP_BACKGROUND_LOCATION,
        ThreatSignalType.APP_OP_BLUETOOTH, ThreatSignalType.APP_OP_MEDIA,
        ThreatSignalType.APP_OP_INSTALL_PACKAGES, ThreatSignalType.APP_OP_MANAGE_STORAGE -> true
        else -> false
    }

    private fun evidenceScore(types: Set<ThreatSignalType>): Int {
        val sensitive = types.any(::isSensitiveAccess)
        return when {
            ThreatSignalType.SIGNER_CHANGED in types && sensitive -> 90
            ThreatSignalType.APP_INSTALLED in types && ThreatSignalType.INPUT_METHOD_ENABLED in types -> 74
            ThreatSignalType.APP_INSTALLED in types && sensitive -> 78
            ThreatSignalType.SENSITIVE_PERMISSION_CHANGED in types && sensitive -> 72
            ThreatSignalType.INPUT_METHOD_ENABLED in types && ThreatSignalType.NOTIFICATION_ACCESS_ENABLED in types -> 68
            ThreatSignalType.SPECIAL_ACCESS_ENABLED in types && ThreatSignalType.VPN_CONFIGURATION_CHANGED in types -> 60
            else -> 0
        }
    }

    private fun chainTitle(types: Set<ThreatSignalType>): String = when {
        ThreatSignalType.SIGNER_CHANGED in types -> "Изменение идентичности и чувствительные доступы"
        ThreatSignalType.APP_INSTALLED in types && ThreatSignalType.INPUT_METHOD_ENABLED in types ->
            "Новое приложение зарегистрировано как способ ввода"
        ThreatSignalType.INPUT_METHOD_ENABLED in types -> "Новый способ ввода требует проверки"
        ThreatSignalType.APP_INSTALLED in types -> "Новое приложение получило чувствительный доступ"
        else -> "Связанные изменения безопасности приложения"
    }

    private fun chainExplanation(types: Set<ThreatSignalType>): String {
        val reasons = buildList {
            if (ThreatSignalType.APP_INSTALLED in types) add("обнаружена установка приложения")
            if (ThreatSignalType.SIGNER_CHANGED in types) add("изменился сертификат подписи")
            if (ThreatSignalType.SENSITIVE_PERMISSION_CHANGED in types) add("изменились чувствительные разрешения")
            if (ThreatSignalType.SPECIAL_ACCESS_ENABLED in types) add("появился специальный доступ")
            if (ThreatSignalType.OVERLAY_ACCESS_ENABLED in types) add("обнаружен доступ к отображению поверх других приложений")
            if (ThreatSignalType.NOTIFICATION_ACCESS_ENABLED in types) add("обнаружен доступ к содержимому уведомлений")
            if (ThreatSignalType.INPUT_METHOD_ENABLED in types) add("приложение зарегистрировано в качестве способа ввода")
            if (ThreatSignalType.VPN_CONFIGURATION_CHANGED in types) add("изменилась конфигурация VPN")
            if (ThreatSignalType.APP_OP_CAMERA in types) add("разрешена операция камеры")
            if (ThreatSignalType.APP_OP_MICROPHONE in types) add("разрешена операция микрофона")
            if (ThreatSignalType.APP_OP_LOCATION in types) add("разрешена операция определения местоположения")
            if (ThreatSignalType.APP_OP_BACKGROUND_LOCATION in types) add("разрешено фоновое местоположение")
            if (ThreatSignalType.APP_OP_BLUETOOTH in types) add("разрешено сканирование Bluetooth")
            if (ThreatSignalType.APP_OP_MEDIA in types) add("разрешён доступ к медиаданным")
            if (ThreatSignalType.APP_OP_INSTALL_PACKAGES in types) add("разрешён запрос установки пакетов")
            if (ThreatSignalType.APP_OP_MANAGE_STORAGE in types) add("разрешён широкий доступ к файлам")
        }
        return "В пределах одного временного окна ${reasons.joinToString("; ")}. " +
            "Это сочетание требует проверки, но само по себе не доказывает заражение или слежку."
    }

    private fun severityRank(value: String): Int = when (value.uppercase()) {
        "CRITICAL" -> 4
        "HIGH" -> 3
        "MEDIUM" -> 2
        "LOW" -> 1
        else -> 0
    }
}
