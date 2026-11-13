package com.lunacode.mobilesecurity.scanner

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import com.lunacode.mobilesecurity.intelligence.DeviceIntegrityInspector
import com.lunacode.mobilesecurity.intelligence.VerifiedThreatDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

class DeviceAuditScanner(
    private val context: Context,
    private val threatDatabase: VerifiedThreatDatabase? = null
) {
    private var appInventory: JSONArray = JSONArray()
    private var lastAppCount: Int = 0
    private var lastDurationMs: Long = 0L

    fun scan(): List<AuditFinding> {
        val startedAt = SystemClock.elapsedRealtime()
        val findings = mutableListOf<AuditFinding>()
        scanInstalledApps(findings)
        scanAccessibility(findings)
        scanNotificationListeners(findings)
        scanDeviceAdmins(findings)
        scanVpn(findings)
        scanDeviceIntegrity(findings)
        findings += AuditFinding(
            id = "scope-root",
            title = "Границы проверки без root",
            detail = "Сканирование не имеет доступа к памяти других приложений, скрытым системным процессам и защищённым данным Android. Эти области не входят в охват проверки.",
            severity = "INFO",
            source = "Ограничения Android"
        )
        lastDurationMs = (SystemClock.elapsedRealtime() - startedAt).coerceAtLeast(0L)
        return findings
    }

    private fun scanInstalledApps(findings: MutableList<AuditFinding>) {
        val pm = context.packageManager
        val flags = PackageManager.GET_PERMISSIONS.toLong() or
            PackageManager.GET_SERVICES.toLong() or
            if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES.toLong()
            else PackageManager.GET_SIGNATURES.toLong()
        val packages = if (Build.VERSION.SDK_INT >= 33) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(flags))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(flags.toInt())
        }
        lastAppCount = packages.size
        appInventory = JSONArray()

        val knownRemoteTools = mapOf(
            "com.anydesk.anydeskandroid" to "AnyDesk",
            "com.teamviewer.host.market" to "TeamViewer Host",
            "com.teamviewer.quicksupport.market" to "TeamViewer QuickSupport",
            "com.sand.airdroid" to "AirDroid",
            "com.sand.airmirror" to "AirMirror",
            "com.rsupport.mvagent" to "Remote support agent",
            "com.realvnc.viewer.android" to "VNC Viewer"
        )

        packages.forEach { info ->
            val packageName = info.packageName ?: return@forEach
            val appInfo = info.applicationInfo ?: return@forEach
            val label = runCatching { pm.getApplicationLabel(appInfo).toString() }.getOrDefault(packageName)
            val systemApp = appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0
            val signerDigests = signerFingerprints(info)
            val permissionNames = info.requestedPermissions.orEmpty()
            val permissionState = permissionNames.mapIndexed { index, permission ->
                val granted = (info.requestedPermissionsFlags?.getOrNull(index)
                    ?.and(PackageInfo.REQUESTED_PERMISSION_GRANTED) ?: 0) != 0
                "$permission:$granted"
            }.sorted().joinToString("|")
            val permissionDigest = sha256(permissionState.toByteArray(Charsets.UTF_8))
            val installer = runCatching {
                if (Build.VERSION.SDK_INT >= 30) {
                    pm.getInstallSourceInfo(packageName).installingPackageName
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstallerPackageName(packageName)
                }
            }.getOrNull().orEmpty()

            appInventory.put(JSONObject().apply {
                put("packageName", packageName)
                put("label", label)
                put("version", "${info.versionName.orEmpty()}:${versionCode(info)}")
                put("signerSha256", signerDigests.sorted().joinToString(","))
                put("permissionsSha256", permissionDigest)
                put("installerPackage", installer)
                put("lastUpdateTime", info.lastUpdateTime)
                put("isSystemApp", systemApp)
            })

            knownRemoteTools[packageName]?.let { remoteName ->
                findings += AuditFinding(
                    id = "remote-$packageName",
                    title = "Приложение удалённого доступа: $label",
                    detail = "$remoteName обнаружено среди доступных приложений. Само наличие не означает заражение: проверьте, устанавливали ли вы его и кому предоставляли доступ.",
                    severity = "MEDIUM",
                    source = packageName
                )
            }

            val requestedSensitive = permissionNames.toSet().intersect(SENSITIVE_PERMISSIONS)
            val grantedSensitive = permissionNames.mapIndexedNotNull { index, permission ->
                val granted = (info.requestedPermissionsFlags?.getOrNull(index)
                    ?.and(PackageInfo.REQUESTED_PERMISSION_GRANTED) ?: 0) != 0
                permission.takeIf { granted && it in SENSITIVE_PERMISSIONS }
            }.toSet()
            if (grantedSensitive.size >= 2 && !systemApp) {
                findings += AuditFinding(
                    id = "granted-permissions-$packageName",
                    title = "Выданы чувствительные разрешения: $label",
                    detail = "Сейчас отмечены как выданные: ${grantedSensitive.sorted().joinToString()}. Проверьте, нужны ли приложению эти возможности.",
                    severity = "LOW",
                    source = packageName
                )
            }
            if (requestedSensitive.size >= 3 && !systemApp) {
                findings += AuditFinding(
                    id = "permissions-$packageName",
                    title = "Широкий набор чувствительных разрешений: $label",
                    detail = "Запрошено чувствительных разрешений: ${requestedSensitive.size}. Запрошенное разрешение не означает, что доступ сейчас выдан.",
                    severity = "LOW",
                    source = packageName
                )
            }
            if (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0 && !systemApp) {
                findings += AuditFinding(
                    id = "debuggable-$packageName",
                    title = "Отладочная сборка приложения: $label",
                    detail = "Приложение помечено как debuggable. Для обычной пользовательской версии это нетипично; проверьте источник установки.",
                    severity = "LOW",
                    source = packageName
                )
            }

            val specialServices = info.services.orEmpty().filter {
                it.permission == "android.permission.BIND_ACCESSIBILITY_SERVICE" ||
                    it.permission == "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
            }
            if (specialServices.isNotEmpty()) {
                findings += AuditFinding(
                    id = "declared-control-service-$packageName",
                    title = "Приложение объявляет специальный системный сервис: $label",
                    detail = "В манифесте найдено служб: ${specialServices.size}, связанных с Accessibility или чтением уведомлений. Само объявление не означает, что доступ включён; активные службы проверяются отдельно.",
                    severity = "INFO",
                    source = packageName
                )
            }

            val packageIndicators = threatDatabase?.byPackageName?.get(packageName).orEmpty()
            val apkHashForIndicators = if (packageIndicators.any { it.apkSha256 != null }) {
                runCatching { sha256(File(appInfo.sourceDir)) }.getOrNull()
            } else null
            packageIndicators.forEach { indicator ->
                val signerMatches = indicator.signerSha256 == null ||
                    signerDigests.any { it.equals(indicator.signerSha256, ignoreCase = true) }
                val apkMatches = indicator.apkSha256 == null ||
                    apkHashForIndicators?.equals(indicator.apkSha256, ignoreCase = true) == true
                if (signerMatches && apkMatches) {
                    findings += AuditFinding(
                        id = "offline-threat-${indicator.id}",
                        title = indicator.title,
                        detail = "${indicator.detail}\nСовпали признаки подписанной офлайн-базы: пакет $packageName; confidence ${indicator.confidence}/100. Это совпадение индикатора, а не доказательство исполнения вредоносного кода.",
                        severity = indicator.severity,
                        source = "OfflineThreatDatabase:${indicator.id}"
                    )
                }
            }
        }

        findings += AuditFinding(
            id = "apps-count",
            title = "Анализ приложений завершён",
            detail = "Видимых приложений: ${packages.size}. Подписи, разрешения и локальные индикаторы обрабатываются только на устройстве.",
            severity = "INFO",
            source = "PackageManager"
        )
        if (threatDatabase != null) {
            findings += AuditFinding(
                id = "offline-threat-db-status",
                title = "Подписанная офлайн-база активна",
                detail = "Проверена подпись издателя ${threatDatabase.keyId}; версия ${threatDatabase.version}; записей ${threatDatabase.indicators.size}.",
                severity = "INFO",
                source = "OfflineThreatDatabase"
            )
        } else {
            findings += AuditFinding(
                id = "offline-threat-db-not-configured",
                title = "Подписанная база индикаторов не активна",
                detail = "Нет доступной проверенной базы. PRO SEC продолжает локальный аудит без внешних сигнатур.",
                severity = "INFO",
                source = "OfflineThreatDatabase"
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun versionCode(info: PackageInfo): Long =
        if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()

    @Suppress("DEPRECATION")
    private fun signerFingerprints(info: PackageInfo): List<String> {
        val signatures = if (Build.VERSION.SDK_INT >= 28) {
            info.signingInfo?.apkContentsSigners?.toList().orEmpty()
        } else {
            info.signatures?.toList().orEmpty()
        }
        return signatures.map { sha256(it.toByteArray()) }
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

    private fun scanAccessibility(findings: MutableList<AuditFinding>) {
        val raw = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        val services = raw.split(':').filter { it.isNotBlank() }
        services.forEach { component ->
            val packageName = component.substringBefore('/')
            findings += AuditFinding(
                id = "accessibility-$packageName",
                title = "Включён сервис специальных возможностей",
                detail = "Пакет $packageName имеет активный Accessibility-сервис. Это может быть необходимо для специальных возможностей или автоматизации, но также требует проверки доверия.",
                severity = "MEDIUM",
                source = component
            )
        }
        if (services.isEmpty()) findings += AuditFinding(
            "accessibility-none", "Сервисы Accessibility не обнаружены",
            "Системная настройка не содержит включённых сервисов специальных возможностей.",
            "INFO", "Settings.Secure"
        )
    }

    private fun scanNotificationListeners(findings: MutableList<AuditFinding>) {
        val raw = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners").orEmpty()
        val listeners = raw.split(':').filter { it.isNotBlank() }
        listeners.forEach { component ->
            val packageName = component.substringBefore('/')
            val label = runCatching {
                context.packageManager.getApplicationLabel(
                    context.packageManager.getApplicationInfo(packageName, 0)
                ).toString()
            }.getOrDefault(packageName)
            findings += AuditFinding(
                id = "notification-listener-$packageName",
                title = "Приложение читает уведомления: $label",
                detail = "Включён Notification Listener. Он может получать содержимое уведомлений, включая личные сообщения и одноразовые коды. Проверьте, доверяете ли вы этому приложению.",
                severity = "MEDIUM",
                source = component
            )
        }
        if (listeners.isEmpty()) findings += AuditFinding(
            "notification-listener-none", "Слушатели уведомлений не обнаружены",
            "Android не сообщил включённых сторонних Notification Listener.",
            "INFO", "Settings.Secure"
        )
    }

    private fun scanDeviceIntegrity(findings: MutableList<AuditFinding>) {
        DeviceIntegrityInspector.inspect(context).forEach { signal ->
            findings += AuditFinding(signal.id, signal.title, signal.detail, signal.severity, "DeviceIntegrityInspector")
        }
    }

    private fun scanDeviceAdmins(findings: MutableList<AuditFinding>) {
        val manager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admins = manager.activeAdmins.orEmpty()
        admins.forEach { component ->
            findings += AuditFinding(
                id = "admin-${component.packageName}",
                title = "Активный администратор устройства",
                detail = "Пакет ${component.packageName} зарегистрирован как администратор устройства. Проверьте, узнаёте ли вы это приложение.",
                severity = "MEDIUM",
                source = component.flattenToShortString()
            )
        }
        if (admins.isEmpty()) findings += AuditFinding(
            "admin-none", "Активные администраторы не обнаружены",
            "Android не сообщил активных администраторов устройства.", "INFO", "DevicePolicyManager"
        )
    }

    private fun scanVpn(findings: MutableList<AuditFinding>) {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = manager.activeNetwork
        val vpnActive = activeNetwork?.let {
            manager.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } == true
        findings += AuditFinding(
            "vpn-state",
            if (vpnActive) "Обнаружено VPN-подключение" else "Активное VPN-подключение не обнаружено",
            if (vpnActive) "Система сообщает об активном VPN-транспорте. Это может быть ваш VPN; проверьте, какой сервис вы включили."
            else "На момент проверки Android не сообщил активный VPN-транспорт.",
            "INFO", "ConnectivityManager"
        )
    }

    fun toJson(findings: List<AuditFinding>): String {
        val array = JSONArray()
        findings.forEach { finding ->
            array.put(JSONObject().apply {
                put("id", finding.id); put("title", finding.title)
                put("detail", finding.detail); put("severity", finding.severity)
                put("source", finding.source)
            })
        }
        return JSONObject().apply {
            put("schema", 2)
            put("product", "PRO SEC")
            put("scannedAt", System.currentTimeMillis())
            put("findingCount", findings.size)
            put("findings", array)
            put("apps", appInventory)
            put("scanMetrics", JSONObject().apply {
                put("appCount", lastAppCount)
                put("durationMs", lastDurationMs)
                put("threatDatabaseVersion", threatDatabase?.version ?: 0L)
            })
        }.toString()
    }

    companion object {
        private val SENSITIVE_PERMISSIONS = setOf(
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.READ_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.SEND_SMS",
            "android.permission.READ_CALL_LOG",
            "android.permission.WRITE_CALL_LOG",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_BACKGROUND_LOCATION",
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
            "android.permission.READ_PHONE_STATE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.MANAGE_EXTERNAL_STORAGE",
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.REQUEST_INSTALL_PACKAGES"
        )
    }
}
