package com.lunacode.mobilesecurity.intelligence

enum class ThreatSignalType {
    APP_INSTALLED,
    SPECIAL_ACCESS_ENABLED,
    SENSITIVE_PERMISSION_CHANGED,
    SIGNER_CHANGED,
    INPUT_METHOD_ENABLED,
    NOTIFICATION_ACCESS_ENABLED,
    OVERLAY_ACCESS_ENABLED,
    VPN_CONFIGURATION_CHANGED
}

data class ThreatSignal(
    val id: String,
    val packageName: String?,
    val type: ThreatSignalType,
    val occurredAtMillis: Long,
    val severity: String,
    val summary: String
)

data class AttackChain(
    val packageName: String,
    val signals: List<ThreatSignal>,
    val title: String,
    val explanation: String,
    /** Heuristic evidence-strength score, not a probability of compromise. */
    val confidenceScore: Int,
    val severity: String
)

object AttackChainDetector {
    private const val DEFAULT_WINDOW_MILLIS = 72L * 60L * 60L * 1000L

    fun detect(
        signals: List<ThreatSignal>,
        windowMillis: Long = DEFAULT_WINDOW_MILLIS
    ): List<AttackChain> {
        if (windowMillis < 0L) return emptyList()
        val result = mutableListOf<AttackChain>()
        signals.asSequence()
            .filter { !it.packageName.isNullOrBlank() && it.id.isNotBlank() }
            .groupBy { it.packageName!! }
            .toSortedMap()
            .forEach { (packageName, packageSignals) ->
                val ordered = packageSignals.distinctBy { it.id }.sortedBy { it.occurredAtMillis }
                correlatedGroups(ordered, windowMillis).forEach { group ->
                    val types = group.map { it.type }.toSet()
                    val score = evidenceScore(types)
                    if (score > 0) {
                        result += AttackChain(
                            packageName = packageName,
                            signals = group,
                            title = chainTitle(types),
                            explanation = chainExplanation(types),
                            confidenceScore = score,
                            severity = group.maxByOrNull { severityRank(it.severity) }?.severity ?: "INFO"
                        )
                    }
                }
            }
        return result.sortedWith(
            compareByDescending<AttackChain> { it.confidenceScore }
                .thenBy { it.packageName }
                .thenBy { it.signals.firstOrNull()?.occurredAtMillis ?: 0L }
        )
    }

    private fun correlatedGroups(signals: List<ThreatSignal>, windowMillis: Long): List<List<ThreatSignal>> {
        val groups = mutableListOf<List<ThreatSignal>>()
        for (start in signals.indices) {
            val group = signals.drop(start).takeWhile {
                withinWindow(signals[start].occurredAtMillis, it.occurredAtMillis, windowMillis)
            }
            val types = group.map { it.type }.toSet()
            if (types.size >= 2 && evidenceScore(types) > 0) {
                val signature = group.map { it.id }.toSet()
                if (groups.none { it.map { signal -> signal.id }.toSet() == signature }) groups += group
            }
        }
        return groups
    }

    private fun withinWindow(start: Long, end: Long, windowMillis: Long): Boolean =
        end >= start && end - start <= windowMillis

    private fun evidenceScore(types: Set<ThreatSignalType>): Int = when {
        ThreatSignalType.SIGNER_CHANGED in types &&
            (ThreatSignalType.SPECIAL_ACCESS_ENABLED in types ||
                ThreatSignalType.OVERLAY_ACCESS_ENABLED in types ||
                ThreatSignalType.NOTIFICATION_ACCESS_ENABLED in types) -> 90
        ThreatSignalType.APP_INSTALLED in types &&
            (ThreatSignalType.SPECIAL_ACCESS_ENABLED in types ||
                ThreatSignalType.OVERLAY_ACCESS_ENABLED in types ||
                ThreatSignalType.NOTIFICATION_ACCESS_ENABLED in types) -> 78
        ThreatSignalType.APP_INSTALLED in types &&
            ThreatSignalType.INPUT_METHOD_ENABLED in types -> 74
        ThreatSignalType.SENSITIVE_PERMISSION_CHANGED in types &&
            (ThreatSignalType.SPECIAL_ACCESS_ENABLED in types ||
                ThreatSignalType.OVERLAY_ACCESS_ENABLED in types ||
                ThreatSignalType.NOTIFICATION_ACCESS_ENABLED in types) -> 72
        ThreatSignalType.INPUT_METHOD_ENABLED in types &&
            ThreatSignalType.NOTIFICATION_ACCESS_ENABLED in types -> 68
        ThreatSignalType.SPECIAL_ACCESS_ENABLED in types &&
            ThreatSignalType.VPN_CONFIGURATION_CHANGED in types -> 60
        else -> 0
    }

    private fun chainTitle(types: Set<ThreatSignalType>): String = when {
        ThreatSignalType.SIGNER_CHANGED in types -> "Изменение идентичности и специальные доступы"
        ThreatSignalType.APP_INSTALLED in types && ThreatSignalType.INPUT_METHOD_ENABLED in types ->
            "Новое приложение зарегистрировано как способ ввода"
        ThreatSignalType.INPUT_METHOD_ENABLED in types -> "Новый способ ввода требует проверки"
        ThreatSignalType.APP_INSTALLED in types -> "Новое приложение получило специальные доступы"
        else -> "Связанные изменения безопасности приложения"
    }

    private fun chainExplanation(types: Set<ThreatSignalType>): String {
        val reasons = buildList {
            if (ThreatSignalType.APP_INSTALLED in types && ThreatSignalType.INPUT_METHOD_ENABLED in types) {
                add("обнаружена установка приложения, зарегистрированного как способ ввода")
            } else if (ThreatSignalType.APP_INSTALLED in types) {
                add("обнаружена установка приложения")
            }
            if (ThreatSignalType.SIGNER_CHANGED in types) add("изменился сертификат подписи")
            if (ThreatSignalType.SENSITIVE_PERMISSION_CHANGED in types) add("изменились чувствительные разрешения")
            if (ThreatSignalType.SPECIAL_ACCESS_ENABLED in types) add("появился специальный доступ")
            if (ThreatSignalType.OVERLAY_ACCESS_ENABLED in types) add("обнаружен доступ к отображению поверх других приложений")
            if (ThreatSignalType.NOTIFICATION_ACCESS_ENABLED in types) add("обнаружен доступ к уведомлениям")
            if (ThreatSignalType.INPUT_METHOD_ENABLED in types) add("обнаружен включённый способ ввода")
            if (ThreatSignalType.VPN_CONFIGURATION_CHANGED in types) add("изменилась конфигурация VPN")
        }
        return "В коротком временном интервале ${reasons.joinToString("; ")}. " +
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
