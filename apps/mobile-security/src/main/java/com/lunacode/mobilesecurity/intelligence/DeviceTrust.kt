package com.lunacode.mobilesecurity.intelligence

import android.app.KeyguardManager
import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.os.Build

data class DeviceIntegritySignal(
    val id: String,
    val title: String,
    val detail: String,
    val severity: String
)

object DeviceIntegrityInspector {
    fun inspect(context: Context): List<DeviceIntegritySignal> {
        val signals = mutableListOf<DeviceIntegritySignal>()
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (keyguard.isDeviceSecure) {
            signals += DeviceIntegritySignal(
                "screen-lock",
                "Защищённая блокировка экрана включена",
                "Android сообщает, что для разблокировки используется PIN, пароль или графический ключ.",
                "INFO"
            )
        } else {
            signals += DeviceIntegritySignal(
                "screen-lock-missing",
                "Не обнаружена защищённая блокировка экрана",
                "Включите PIN, пароль или графический ключ. Биометрия сама по себе должна быть привязана к защищённому способу разблокировки.",
                "HIGH"
            )
        }

        val tags = Build.TAGS.orEmpty()
        if (tags.contains("test-keys", ignoreCase = true)) {
            signals += DeviceIntegritySignal(
                "build-test-keys",
                "Системная сборка помечена test-keys",
                "Build tags содержат test-keys. Это бывает у инженерных или пользовательских прошивок и не доказывает компрометацию. Проверьте происхождение прошивки.",
                "MEDIUM"
            )
        } else {
            signals += DeviceIntegritySignal(
                "build-release-tags",
                "Признак test-keys не обнаружен",
                "В доступных Build tags нет метки test-keys. Это ограниченная проверка метаданных, а не подтверждение целостности загрузчика.",
                "INFO"
            )
        }

        signals += DeviceIntegritySignal(
            "adb-visibility-limit",
            "Состояние ADB не проверялось",
            "Android скрывает значения USB ADB и Developer Options от обычных сторонних приложений: соответствующие Settings API возвращают для них 0. PRO SEC не показывает ложный статус «выключено».",
            "INFO"
        )
        signals += DeviceIntegritySignal(
            "build-summary",
            "Сведения об устройстве",
            "${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}",
            "INFO"
        )
        return signals
    }
}

data class InputMethodProfile(
    val id: String,
    val packageName: String,
    val label: String,
    val active: Boolean,
    val systemApp: Boolean
)

data class InputTrustReport(
    val currentPackage: String?,
    val methods: List<InputMethodProfile>
)

object InputTrustInspector {
    fun inspect(context: Context): InputTrustReport {
        val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val activeId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )
        val methods = manager.enabledInputMethodList.map { method ->
            val packageName = method.packageName
            val appInfo = runCatching { context.packageManager.getApplicationInfo(packageName, 0) }.getOrNull()
            val label = runCatching {
                method.loadLabel(context.packageManager).toString()
            }.getOrDefault(packageName)
            val isSystem = appInfo?.let {
                it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0
            } ?: false
            InputMethodProfile(
                id = method.id,
                packageName = packageName,
                label = label,
                active = method.id == activeId,
                systemApp = isSystem
            )
        }.sortedWith(compareByDescending<InputMethodProfile> { it.active }.thenBy { it.label.lowercase() })
        return InputTrustReport(
            currentPackage = activeId?.substringBefore('/'),
            methods = methods
        )
    }
}
