package com.lunacode.mobilesecurity.intelligence

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import java.io.File
import java.io.FileOutputStream
import java.net.IDN
import java.net.URI
import java.security.MessageDigest

data class ControlSurface(
    val category: String,
    val packageName: String,
    val appLabel: String,
    val capability: String,
    val explanation: String
)

data class LinkInspection(
    val normalizedUrl: String,
    val host: String,
    val indicators: List<String>,
    val note: String
)

object OfflineLinkInspector {
    private val shorteners = setOf(
        "bit.ly", "tinyurl.com", "t.co", "goo.gl", "is.gd", "cutt.ly", "rb.gy",
        "ow.ly", "buff.ly", "shorturl.at", "rebrand.ly"
    )
    private val ipv4 = Regex("""^(?:\d{1,3}\.){3}\d{1,3}$""")

    fun inspect(input: String): LinkInspection {
        val raw = input.trim()
        require(raw.isNotEmpty()) { "Вставьте ссылку для проверки." }
        require(raw.length <= 4096) { "Ссылка слишком длинная для локального анализа." }
        val normalized = if ("://" in raw) raw else "https://$raw"
        val uri = runCatching { URI(normalized) }.getOrElse {
            throw IllegalArgumentException("Не удалось разобрать структуру ссылки.")
        }
        val authority = uri.rawAuthority.orEmpty()
        val hostRaw = uri.host ?: authority.substringAfterLast('@').substringBefore(':')
        require(hostRaw.isNotBlank()) { "В ссылке не найден домен." }
        val host = runCatching { IDN.toASCII(hostRaw.lowercase()) }.getOrDefault(hostRaw.lowercase())
        val indicators = mutableListOf<String>()
        val scheme = uri.scheme?.lowercase().orEmpty()

        if (scheme == "http") indicators += "Используется незашифрованный HTTP вместо HTTPS."
        if (scheme !in setOf("http", "https")) indicators += "Необычная схема ссылки: $scheme."
        if (uri.rawUserInfo != null || '@' in authority) {
            indicators += "В адресе есть user-info перед доменом: возможна маскировка настоящего назначения."
        }
        if (host.startsWith("xn--") || host.split('.').any { it.startsWith("xn--") }) {
            indicators += "Домен содержит Punycode; проверьте возможную визуальную подмену символов."
        }
        if (ipv4.matches(host)) indicators += "Вместо доменного имени используется IPv4-адрес."
        if (host in shorteners) indicators += "Используется сокращатель ссылок; конечный адрес скрыт."
        if (host.split('.').size >= 5) indicators += "У домена необычно много поддоменов."
        if (uri.port != -1 && uri.port !in setOf(80, 443)) {
            indicators += "Используется нестандартный сетевой порт: ${uri.port}."
        }
        if (uri.rawPath.orEmpty().contains("%2f", ignoreCase = true) ||
            uri.rawPath.orEmpty().contains("%5c", ignoreCase = true)
        ) {
            indicators += "Путь содержит закодированные разделители, затрудняющие визуальную проверку."
        }
        if (host.length > 70) indicators += "Домен необычно длинный."
        val canonical = runCatching { URI(scheme, uri.rawUserInfo, host, uri.port, uri.rawPath, uri.rawQuery, uri.rawFragment).toASCIIString() }
            .getOrDefault(normalized)
        return LinkInspection(
            normalizedUrl = canonical,
            host = host,
            indicators = indicators.distinct(),
            note = "Проверена только структура адреса. Репутация домена, DNS, содержимое сайта и перенаправления офлайн не проверялись."
        )
    }
}

data class ApkRisk(
    val title: String,
    val detail: String,
    val level: String
)

data class ApkXRayReport(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val targetSdk: Int,
    val minSdk: Int,
    val apkSha256: String,
    val signerSha256: List<String>,
    val requestedPermissions: List<String>,
    val exportedComponents: Int,
    val risks: List<ApkRisk>,
    val fileSizeBytes: Long
)

object ApkXRay {
    private const val MAX_APK_BYTES = 150L * 1024L * 1024L
    private const val FLAG_GET_ALL_COMPONENTS =
        PackageManager.GET_PERMISSIONS or
            PackageManager.GET_ACTIVITIES or
            PackageManager.GET_SERVICES or
            PackageManager.GET_RECEIVERS or
            PackageManager.GET_PROVIDERS

    fun inspect(context: Context, uri: Uri): ApkXRayReport {
        val file = File.createTempFile("prosec-xray-", ".apk", context.cacheDir)
        try {
            var total = 0L
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        require(total <= MAX_APK_BYTES) { "APK больше допустимых 150 МБ." }
                        output.write(buffer, 0, read)
                    }
                }
            } ?: throw IllegalArgumentException("Не удалось открыть выбранный APK.")
            require(total > 0L) { "Выбранный файл пуст." }
            return inspectFile(context, file)
        } finally {
            file.delete()
        }
    }

    private fun inspectFile(context: Context, file: File): ApkXRayReport {
        val pm = context.packageManager
        val flags = FLAG_GET_ALL_COMPONENTS.toLong() or
            if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES.toLong()
            else PackageManager.GET_SIGNATURES.toLong()
        val info = if (Build.VERSION.SDK_INT >= 33) {
            pm.getPackageArchiveInfo(file.absolutePath, PackageManager.PackageInfoFlags.of(flags))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageArchiveInfo(file.absolutePath, flags.toInt())
        } ?: throw IllegalArgumentException("Android не смог прочитать манифест APK.")

        val app = info.applicationInfo ?: throw IllegalArgumentException("В APK отсутствует ApplicationInfo.")
        val packageName = info.packageName.orEmpty()
        val requested = info.requestedPermissions.orEmpty().toList()
        val signers = getSigners(info).map { sha256(it.toByteArray()) }
        val exported = (info.activities.orEmpty().count { it.exported } +
            info.services.orEmpty().count { it.exported } +
            info.receivers.orEmpty().count { it.exported } +
            info.providers.orEmpty().count { it.exported })

        val risks = mutableListOf<ApkRisk>()
        if (app.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            risks += ApkRisk("Отладочная сборка", "APK помечен как debuggable. В пользовательском релизе это обычно не требуется.", "HIGH")
        }
        if (app.flags and android.content.pm.ApplicationInfo.FLAG_USES_CLEARTEXT_TRAFFIC != 0) {
            risks += ApkRisk("Разрешён незашифрованный трафик", "Манифест допускает cleartext traffic. Это не доказывает передачу секретов, но требует проверки сетевого кода.", "MEDIUM")
        }
        if (app.flags and android.content.pm.ApplicationInfo.FLAG_ALLOW_BACKUP != 0) {
            risks += ApkRisk("Разрешено резервное копирование", "Манифест разрешает Android backup. Проверьте backup rules и типы хранимых данных.", "LOW")
        }
        if (exported > 0) {
            risks += ApkRisk("Экспортируемые компоненты: $exported", "Экспортируемые Activity/Service/Receiver/Provider могут быть доступны другим приложениям. Проверьте permissions и intent filters.", "MEDIUM")
        }
        val sensitivePermissions = requested.filter { permission ->
            permission in SENSITIVE_PERMISSIONS
        }
        if (sensitivePermissions.isNotEmpty()) {
            risks += ApkRisk(
                "Чувствительные разрешения: ${sensitivePermissions.size}",
                sensitivePermissions.joinToString("\n"),
                "LOW"
            )
        }
        if (signers.isEmpty()) {
            risks += ApkRisk("Сертификат подписи не прочитан", "Не удалось извлечь сертификат APK. Сравнение издателя недоступно.", "MEDIUM")
        }
        return ApkXRayReport(
            packageName = packageName,
            versionName = info.versionName.orEmpty(),
            versionCode = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else @Suppress("DEPRECATION") info.versionCode.toLong(),
            targetSdk = app.targetSdkVersion,
            minSdk = app.minSdkVersion,
            apkSha256 = sha256(file),
            signerSha256 = signers,
            requestedPermissions = requested,
            exportedComponents = exported,
            risks = risks,
            fileSizeBytes = file.length()
        )
    }

    @Suppress("DEPRECATION")
    private fun getSigners(info: PackageInfo): List<android.content.pm.Signature> {
        return if (Build.VERSION.SDK_INT >= 28) {
            info.signingInfo?.apkContentsSigners?.toList().orEmpty()
        } else {
            info.signatures?.toList().orEmpty()
        }
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02X".format(it) }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02X".format(it) }
    }

    private val SENSITIVE_PERMISSIONS = setOf(
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.SEND_SMS",
        "android.permission.READ_CALL_LOG",
        "android.permission.WRITE_CALL_LOG",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_BACKGROUND_LOCATION",
        "android.permission.READ_CONTACTS",
        "android.permission.READ_PHONE_STATE",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.MANAGE_EXTERNAL_STORAGE",
        "android.permission.SYSTEM_ALERT_WINDOW",
        "android.permission.REQUEST_INSTALL_PACKAGES"
    )
}

object ControlSurfaceInspector {
    fun inspect(context: Context): List<ControlSurface> {
        val pm = context.packageManager
        val packageLabels = mutableMapOf<String, String>()
        fun label(packageName: String): String = packageLabels.getOrPut(packageName) {
            runCatching {
                pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
            }.getOrDefault(packageName)
        }

        val result = mutableListOf<ControlSurface>()
        val accessibility = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        accessibility.split(':').filter { it.isNotBlank() }.forEach { component ->
            val packageName = component.substringBefore('/')
            result += ControlSurface(
                "Accessibility", packageName, label(packageName),
                "Может читать элементы экрана и выполнять действия от имени пользователя",
                "Проверьте, включали ли вы этот сервис намеренно."
            )
        }

        val notificationListeners = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_NOTIFICATION_LISTENERS
        ).orEmpty()
        notificationListeners.split(':').filter { it.isNotBlank() }.forEach { component ->
            val packageName = component.substringBefore('/')
            result += ControlSurface(
                "Доступ к уведомлениям", packageName, label(packageName),
                "Может получать содержимое уведомлений, включая часть личных сообщений и кодов",
                "Проверьте доверие к приложению и необходимость этого доступа."
            )
        }

        val policy = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        policy.activeAdmins.orEmpty().forEach { component ->
            val packageName = component.packageName
            result += ControlSurface(
                "Администратор устройства", packageName, label(packageName),
                "Может применять разрешённые Android политики администратора",
                "Проверьте, узнаёте ли вы приложение и зачем оно получило этот статус."
            )
        }

        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivity.activeNetwork
        val vpnActive = activeNetwork?.let {
            connectivity.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } == true
        if (vpnActive) {
            result += ControlSurface(
                "VPN-транспорт", "system.network", "Активный VPN",
                "Сетевой трафик может проходить через VPN-приложение или профиль",
                "Проверьте выбранный VPN и его издателя в системных настройках."
            )
        }
        return result
    }
}
