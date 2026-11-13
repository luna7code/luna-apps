package com.lunacode.mobilesecurity.intelligence

import com.lunacode.mobilesecurity.scanner.AuditFinding

data class PostureRecommendation(
    val title: String,
    val detail: String,
    val priority: String,
    val packageName: String? = null
)

data class SecurityPostureReport(
    val score: Int,
    val grade: String,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int,
    val recommendations: List<PostureRecommendation>,
    val explanation: String
)

object SecurityPosture {
    /**
     * This is an explainable exposure index over findings the app can observe,
     * not a probability that the device is infected and not a guarantee of safety.
     */
    fun evaluate(findings: List<AuditFinding>): SecurityPostureReport {
        val unique = findings.distinctBy { it.id }
        val high = unique.count { it.severity == "HIGH" }
        val medium = unique.count { it.severity == "MEDIUM" }
        val low = unique.count { it.severity == "LOW" }
        val deductions = (high * 16) + (medium * 7) + (low * 2)
        val score = (100 - deductions.coerceAtMost(75)).coerceIn(25, 100)
        val grade = when {
            score >= 90 -> "Низкая экспозиция"
            score >= 75 -> "Есть пункты для проверки"
            score >= 55 -> "Повышенная экспозиция"
            else -> "Высокая экспозиция"
        }
        val recommendations = buildRecommendations(unique).take(5)
        return SecurityPostureReport(
            score = score,
            grade = grade,
            highCount = high,
            mediumCount = medium,
            lowCount = low,
            recommendations = recommendations,
            explanation = "Индекс рассчитывается только по сигналам, доступным PRO SEC без root. Это не вероятность заражения и не доказательство отсутствия вредоносного ПО."
        )
    }

    private fun buildRecommendations(findings: List<AuditFinding>): List<PostureRecommendation> {
        val result = mutableListOf<PostureRecommendation>()
        findings.filter { it.severity != "INFO" }.forEach { finding ->
            val packageName = when {
                finding.id.startsWith("remote-") -> finding.source
                finding.id.startsWith("accessibility-") -> finding.source.substringBefore('/')
                finding.id.startsWith("notification-listener-") -> finding.source.substringBefore('/')
                finding.id.startsWith("admin-") -> finding.source.substringBefore('/')
                finding.id.startsWith("permissions-") -> finding.source
                finding.id.startsWith("granted-permissions-") -> finding.source
                finding.id.startsWith("debuggable-") -> finding.source
                else -> null
            }?.takeIf { it.contains('.') }

            when {
                finding.id == "screen-lock-missing" -> result += PostureRecommendation(
                    "Включить защищённую блокировку экрана",
                    "Установите PIN, пароль или графический ключ в настройках безопасности Android.",
                    "HIGH"
                )
                finding.id.startsWith("remote-") -> result += PostureRecommendation(
                    "Проверить средство удалённого доступа",
                    "Убедитесь, что вы сами установили это приложение и узнаёте его владельца. При сомнении откройте карточку приложения.",
                    "HIGH",
                    packageName
                )
                finding.id.startsWith("accessibility-") && finding.id != "accessibility-none" -> result += PostureRecommendation(
                    "Проверить Accessibility-доступ",
                    "Отключите сервис, если не узнаёте его или не используете его назначение.",
                    "HIGH",
                    packageName
                )
                finding.id.startsWith("notification-listener-") -> result += PostureRecommendation(
                    "Проверить доступ к уведомлениям",
                    "Такой доступ может раскрывать содержимое уведомлений. Оставляйте его только доверенным приложениям.",
                    "HIGH",
                    packageName
                )
                finding.id.startsWith("admin-") && finding.id != "admin-none" -> result += PostureRecommendation(
                    "Проверить администратора устройства",
                    "Убедитесь, что приложение должно иметь административные полномочия.",
                    "HIGH",
                    packageName
                )
                finding.id.startsWith("debuggable-") -> result += PostureRecommendation(
                    "Проверить отладочную сборку",
                    "Проверьте источник установки и разработчика приложения. Обычные релизные приложения редко должны быть debuggable.",
                    "MEDIUM",
                    packageName
                )
                finding.id == "build-test-keys" -> result += PostureRecommendation(
                    "Проверить происхождение прошивки",
                    "Сверьте прошивку с официальным каналом производителя и избегайте неизвестных системных образов.",
                    "MEDIUM"
                )
                finding.id.startsWith("granted-permissions-") || finding.id.startsWith("permissions-") -> result += PostureRecommendation(
                    "Пересмотреть разрешения приложения",
                    "Откройте настройки приложения и отзовите ненужные чувствительные разрешения.",
                    "MEDIUM",
                    packageName
                )
            }
        }
        return result.distinctBy { it.title to it.packageName }
            .sortedBy { if (it.priority == "HIGH") 0 else 1 }
    }
}
