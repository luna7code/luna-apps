package com.lunacode.mobilesecurity.scanner

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import org.json.JSONArray
import org.json.JSONObject

class DeviceAuditScanner(private val context: Context) {
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
        val packages = if (Build.VERSION.SDK_INT >= 33) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
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
        packages.forEach { info ->
            val packageName = info.packageName ?: return@forEach
            val appInfo = info.applicationInfo ?: return@forEach
            val label = runCatching { pm.getApplicationLabel(appInfo).toString() }.getOrDefault(packageName)
            val isSystem = appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0
            val knownRemote = knownRemoteTools[packageName]
            if (knownRemote != null) {
                findings += AuditFinding(
                    id = "remote-$packageName",
                    title = "Приложение удалённого доступа: $label",
                    detail = "$knownRemote обнаружено среди доступных приложений. Само наличие не означает заражение: проверьте, устанавливали ли вы его и кому предоставляли доступ.",
                    severity = "MEDIUM",
                    source = packageName
                )
            }
            val requested = info.requestedPermissions.orEmpty().toSet()
            val sensitive = requested.intersect(SENSITIVE_PERMISSIONS)
            if (sensitive.size >= 3 && !isSystem) {
                findings += AuditFinding(
                    id = "permissions-$packageName",
                    title = "Широкий набор чувствительных разрешений: $label",
                    detail = "Запрошено чувствительных разрешений: ${sensitive.size}. Требуется ручная проверка назначения приложения. Запрошенное разрешение не означает, что доступ сейчас выдан.",
                    severity = "LOW",
                    source = packageName
                )
            }
            if (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0 && !isSystem) {
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
            detail = "Видимых приложений: ${packages.size}. Список обрабатывается только локально.",
            severity = "INFO",
            source = "PackageManager"
        )
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
            put("schema", 1)
            put("product", "PRO SEC")
            put("scannedAt", System.currentTimeMillis())
            put("findingCount", findings.size)
            put("findings", array)
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
