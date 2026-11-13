package com.lunacode.mobilesecurity.scanner

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

class DeviceAuditScanner(private val context: Context) {
    private var appInventory: JSONArray = JSONArray()

    fun scan(): List<AuditFinding> {
        val findings = mutableListOf<AuditFinding>()
        scanInstalledApps(findings)
        scanAccessibility(findings)
        scanDeviceAdmins(findings)
        scanVpn(findings)
        findings += AuditFinding(
            id = "scope-root",
            title = "Границы проверки без root",
            detail = "Сканирование не имеет доступа к памяти других приложений, скрытым системным процессам и защищённым данным Android. Эти области не входят в охват проверки.",
            severity = "INFO",
            source = "Ограничения Android"
        )
        return findings
    }

    private fun scanInstalledApps(findings: MutableList<AuditFinding>) {
        val pm = context.packageManager
        val flags = PackageManager.GET_PERMISSIONS.toLong() or
            if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES.toLong()
            else PackageManager.GET_SIGNATURES.toLong()
        val packages = if (Build.VERSION.SDK_INT >= 33) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(flags))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(flags.toInt())
        }

        val knownRemoteTools = mapOf(
            "com.anydesk.anydeskandroid" to "AnyDesk",
            "com.teamviewer.host.market" to "TeamViewer Host",
            "com.teamviewer.quicksupport.market" to "TeamViewer QuickSupport",
            "com.sand.airdroid" to "AirDroid",
            "com.sand.airmirror" to "AirMirror",
            "com.rsupport.mvagent" to "Remote support agent",
            "com.realvnc.viewer.android" to "VNC Viewer"
        )

        appInventory = JSONArray()
        packages.forEach { info ->
            val packageName = info.packageName ?: return@forEach
            val appInfo = info.applicationInfo ?: return@forEach
            val label = runCatching { pm.getApplicationLabel(appInfo).toString() }.getOrDefault(packageName)
            val systemApp = appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0
            val signer = signerFingerprints(info).joinToString(",")
            val permissionState = info.requestedPermissions.orEmpty().mapIndexed { index, permission ->
                val granted = (info.requestedPermissionsFlags
                    ?.getOrNull(index)
                    ?.and(PackageInfo.REQUESTED_PERMISSION_GRANTED) ?: 0) != 0
                "$permission:$granted"
            }.sorted().joinToString("|")
            val permissionDigest = MessageDigest.getInstance("SHA-256")
                .digest(permissionState.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02X".format(it) }
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
                put("signerSha256", signer)
                put("permissionsSha256", permissionDigest)
                put("installerPackage", installer)
                put("lastUpdateTime", info.lastUpdateTime)
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

            val permissionNames = info.requestedPermissions.orEmpty()
            val requested = permissionNames.toSet()
            val sensitive = requested.intersect(SENSITIVE_PERMISSIONS)
            val grantedSensitive = permissionNames.mapIndexedNotNull { index, permission ->
                val granted = (info.requestedPermissionsFlags
                    ?.getOrNull(index)
                    ?.and(PackageInfo.REQUESTED_PERMISSION_GRANTED) ?: 0) != 0
                permission.takeIf { granted && it in SENSITIVE_PERMISSIONS }
            }.toSet()
            if (grantedSensitive.size >= 2 && !systemApp) {
                findings += AuditFinding(
                    id = "granted-permissions-$packageName",
                    title = "Выданы чувствительные разрешения: $label",
                    detail = "Сейчас отмечены как выданные: ${grantedSensitive.joinToString()}. Проверьте, нужны ли приложению эти возможности.",
                    severity = "LOW",
                    source = packageName
                )
            }
            if (sensitive.size >= 3 && !systemApp) {
                findings += AuditFinding(
                    id = "permissions-$packageName",
                    title = "Широкий набор чувствительных разрешений: $label",
                    detail = "Запрошено чувствительных разрешений: ${sensitive.size}. Требуется ручная проверка назначения приложения. Запрошенное разрешение не означает, что доступ сейчас выдан.",
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
        }
        findings += AuditFinding(
            id = "apps-count",
            title = "Анализ приложений завершён",
            detail = "Видимых приложений: ${packages.size}. Список и отпечатки подписи обрабатываются только локально.",
            severity = "INFO",
            source = "PackageManager"
        )
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
        return signatures.map { signature ->
            MessageDigest.getInstance("SHA-256")
                .digest(signature.toByteArray())
                .joinToString("") { "%02X".format(it) }
        }
    }

    private fun scanAccessibility(findings: MutableList<AuditFinding>) {
        val raw = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
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
        if (services.isEmpty()) {
            findings += AuditFinding(
                id = "accessibility-none",
                title = "Сервисы Accessibility не обнаружены",
                detail = "Системная настройка не содержит включённых сервисов специальных возможностей.",
                severity = "INFO",
                source = "Settings.Secure"
            )
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
        if (admins.isEmpty()) {
            findings += AuditFinding(
                id = "admin-none",
                title = "Активные администраторы не обнаружены",
                detail = "Android не сообщил активных администраторов устройства.",
                severity = "INFO",
                source = "DevicePolicyManager"
            )
        }
    }

    private fun scanVpn(findings: MutableList<AuditFinding>) {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = manager.activeNetwork
        val vpnActive = activeNetwork?.let { network ->
            manager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } == true
        findings += AuditFinding(
            id = "vpn-state",
            title = if (vpnActive) "Обнаружено VPN-подключение" else "Активное VPN-подключение не обнаружено",
            detail = if (vpnActive) "Система сообщает об активном VPN-транспорте. Это может быть ваш VPN; проверьте, какой сервис вы включили." else "На момент проверки Android не сообщил активный VPN-транспорт.",
            severity = "INFO",
            source = "ConnectivityManager"
        )
    }

    fun toJson(findings: List<AuditFinding>): String {
        val array = JSONArray()
        findings.forEach { finding ->
            array.put(JSONObject().apply {
                put("id", finding.id)
                put("title", finding.title)
                put("detail", finding.detail)
                put("severity", finding.severity)
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
        }.toString()
    }

    companion object {
        private val SENSITIVE_PERMISSIONS = setOf(
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.READ_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.READ_CALL_LOG",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.READ_CONTACTS",
            "android.permission.READ_PHONE_STATE"
        )
    }
}
