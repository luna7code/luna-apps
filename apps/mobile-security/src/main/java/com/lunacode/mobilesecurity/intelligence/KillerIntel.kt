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
        val explicitScheme = Regex("^[A-Za-z][A-Za-z0-9+.-]*:").containsMatchIn(raw)
        val normalized = if (explicitScheme || "://" in raw) raw else "https://$raw"
        val uri = runCatching { URI(normalized) }.getOrElse {
            throw IllegalArgumentException("Не удалось разобрать структуру ссылки.")
        }
        val authority = uri.rawAuthority.orEmpty()
        val hostRaw = uri.host ?: authority.substringAfterLast('@').substringBefore(':')
        val host = runCatching { IDN.toASCII(hostRaw.lowercase()) }.getOrDefault(hostRaw.lowercase())
        val indicators = mutableListOf<String>()
        val scheme = uri.scheme?.lowercase().orEmpty()

        if (scheme == "http") indicators += "Используется незашифрованный HTTP вместо HTTPS."
        if (scheme !in setOf("http", "https")) indicators += "Необычная схема ссылки: $scheme."
        if (host.isBlank()) {
            indicators += "В адресе отсутствует обычное доменное имя."
            return LinkInspection(
                normalizedUrl = normalized,
                host = "нет домена",
                indicators = indicators.distinct(),
                note = "Проверена только строка адреса. Содержимое URI-схемы не исполнялось и не открывалось."
            )
        }
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

data class ExportedComponentExposure(
    val type: String,
    val name: String,
    val permission: String
)

data class RequestedPermissionAssessment(
    val name: String,
    val protectionLevel: String,
    val ownerPackage: String?
)

data class ApkXRayReport(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val targetSdk: Int,
    val minSdk: Int,
    val apkSha256: String,
    val signerSha256: List<String>,
    val signerHistorySha256: List<String>,
    val signerRotationResult: String,
    val signerRotationExplanation: String,
    val installedVersionName: String?,
    val installedSignerMatch: Boolean?,
    val requestedPermissions: List<String>,
    val requestedPermissionAssessments: List<RequestedPermissionAssessment>,
    val exportedComponents: Int,
    val exportedWithoutPermission: Int,
    val exportedComponentDetails: List<ExportedComponentExposure>,
    val dexSignals: List<String>,
    val dexFilesScanned: Int,
    val dexBytesScanned: Long,
    val dexScanTruncated: Boolean,
    val dexScanDurationMs: Long,
    val dexLimitReason: String?,
    val extractedUrls: List<String>,
    val stringFindings: List<StaticStringFinding>,
    val stringScanEntries: Int,
    val stringScanBytes: Long,
    val stringScanTruncated: Boolean,
    val nativeLibraries: List<NativeLibraryInfo>,
    val nestedApkEntries: List<String>,
    val archiveWarnings: List<String>,
    val archiveTruncated: Boolean,
    val binaryManifest: BinaryManifestInspection?,
    val threatIndicators: List<ThreatIndicator>,
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
        val permissionAssessments = resolveRequestedPermissions(pm, requested)
        val signerProfile = readSigningLineage(info)
        val installedInfo = readInstalledPackageInfo(pm, packageName)
        val installedProfile = installedInfo?.let(::readSigningLineage)
        val lineageResult = SigningLineageComparator.compare(installedProfile, signerProfile)
        val signerMatch = when (lineageResult) {
            SigningLineageResult.EXACT_MATCH,
            SigningLineageResult.VERIFIED_FORWARD_ROTATION,
            SigningLineageResult.MULTI_SIGNER_MATCH -> true
            SigningLineageResult.MISMATCH,
            SigningLineageResult.HISTORICAL_SIGNER,
            SigningLineageResult.MULTI_SIGNER_MISMATCH -> false
            SigningLineageResult.UNKNOWN -> null
        }
        val signers = signerProfile?.currentSigners.orEmpty().sorted()
        val signerHistory = signerProfile?.history.orEmpty()
        val exportedDetails = exportedComponentDetails(info)
        val exported = exportedDetails.size
        val exportedWithoutPermission = exportedDetails.count { it.permission.isBlank() }
        val apkDigest = sha256(file)
        val dexScan = runCatching { DexIndicatorScanner.scan(file) }.getOrNull()
        val stringScan = runCatching { StaticStringIndicatorScanner.scan(file) }.getOrNull()
        val archiveScan = runCatching { ApkArchiveInspector.inspect(file) }.getOrNull()
        val binaryManifest = runCatching {
            java.util.zip.ZipFile(file).use { zip ->
                val entry = zip.getEntry("AndroidManifest.xml") ?: return@use null
                if (entry.size !in 1..(2L * 1024L * 1024L)) return@use null
                val bytes = zip.getInputStream(entry).use {
                    StaticStringIndicatorScanner.readBounded(it, 2L * 1024L * 1024L)
                }
                AndroidBinaryManifestInspector.inspect(bytes)
            }
        }.getOrNull()
        val feedIndicators = ThreatDatabaseStore(context).loadVerified()
            ?.byPackageName?.get(packageName).orEmpty()
        val threatMatches = ThreatIndicatorMatcher.match(
            packageName = packageName,
            signerDigests = signers.toSet(),
            indicators = feedIndicators
        ) { apkDigest }

        val risks = mutableListOf<ApkRisk>()
        if (app.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            risks += ApkRisk("Отладочная сборка", "APK помечен как debuggable. В пользовательском релизе это обычно не требуется.", "HIGH")
        }
        if (app.flags and android.content.pm.ApplicationInfo.FLAG_TEST_ONLY != 0) {
            risks += ApkRisk("Тестовая сборка", "Манифест помечает APK как testOnly. Это нетипично для обычной пользовательской установки.", "MEDIUM")
        }
        if (app.flags and android.content.pm.ApplicationInfo.FLAG_USES_CLEARTEXT_TRAFFIC != 0) {
            risks += ApkRisk("Разрешён незашифрованный трафик", "Манифест допускает cleartext traffic. Это не доказывает передачу секретов, но требует проверки сетевого кода.", "MEDIUM")
        }
        if (app.flags and android.content.pm.ApplicationInfo.FLAG_ALLOW_BACKUP != 0) {
            risks += ApkRisk("Разрешено резервное копирование", "Манифест разрешает Android backup. Проверьте backup rules и типы хранимых данных.", "LOW")
        }
        if (app.minSdkVersion <= 23) {
            risks += ApkRisk("Низкий минимальный Android API", "APK заявляет поддержку Android API 23 или ниже. Оценивайте это вместе с target SDK и назначением приложения.", "LOW")
        }
        if (exported > 0) {
            risks += ApkRisk(
                "Экспортируемые компоненты: $exported",
                "Без заданного permission: $exportedWithoutPermission. Проверьте назначение каждого экспортируемого компонента и требуемую защиту.",
                if (exportedWithoutPermission > 0) "MEDIUM" else "LOW"
            )
        }
        val sensitivePermissions = requested.filter { it in SENSITIVE_PERMISSIONS }
        if (sensitivePermissions.isNotEmpty()) {
            risks += ApkRisk("Чувствительные разрешения: ${sensitivePermissions.size}", sensitivePermissions.joinToString("\n"), "LOW")
        }
        if (stringScan?.urls.orEmpty().isNotEmpty()) {
            risks += ApkRisk(
                "URL-адреса найдены в статических строках: ${stringScan?.urls?.size ?: 0}",
                "Найденные адреса — только извлечённые строки из APK. Это не подтверждает сетевые обращения; список приведён ниже для ручной проверки.",
                "INFO"
            )
        }
        stringScan?.findings.orEmpty().forEach { finding ->
            risks += ApkRisk(
                "Строковый индикатор: ${finding.category}",
                "Токен «${finding.indicator}» найден ${finding.occurrences} раз(а) в ограниченно просмотренных строках. Наличие строки не доказывает вызов API или вредоносное действие.",
                "INFO"
            )
        }
        if (stringScan?.truncated == true) {
            risks += ApkRisk(
                "Строковый анализ ограничен",
                "Достигнут лимит архива или объёма данных. Отсутствие индикаторов в непроверенной части не может быть подтверждено.",
                "INFO"
            )
        }
        archiveScan?.nativeLibraries.orEmpty().forEach { library ->
            if (library.warnings.isNotEmpty()) risks += ApkRisk(
                "Native-библиотека: ${library.path}",
                "ABI: ${library.abi}; ELF: ${library.machineName}. ${library.warnings.joinToString("; ")}. Строковые совпадения не доказывают выполнение соответствующего кода.",
                "INFO"
            )
        }
        archiveScan?.suspiciousArchiveEntries.orEmpty().forEach { warning ->
            risks += ApkRisk("Подозрительная структура ZIP", warning, "MEDIUM")
        }
        binaryManifest?.intentFilters.orEmpty().filter { it.autoVerify }.forEach { filter ->
            risks += ApkRisk(
                "Android App Link · autoVerify",
                "Компонент ${filter.component}; host: ${filter.hosts.joinToString().ifBlank { "не указан" }}; schemes: ${filter.schemes.joinToString()}. Это декларация в манифесте, не доказательство успешной верификации домена Android.",
                "INFO"
            )
        }
        if (binaryManifest?.parseWarnings.orEmpty().isNotEmpty()) {
            risks += ApkRisk("Binary manifest прочитан частично", binaryManifest!!.parseWarnings.joinToString("; "), "INFO")
        }
        if (archiveScan?.truncated == true) {
            risks += ApkRisk("Архивный анализ ограничен", "Достигнут лимит числа записей, распакованного размера или размера native-библиотек. Непроверенные части неизвестны.", "INFO")
        }
        if (signers.isEmpty()) {
            risks += ApkRisk("Сертификат подписи не прочитан", "Не удалось извлечь сертификат APK. Сравнение издателя недоступно.", "MEDIUM")
        }
        if (installedInfo != null && signerMatch == false) {
            val detail = when (lineageResult) {
                SigningLineageResult.HISTORICAL_SIGNER ->
                    "APK подписан историческим сертификатом из цепочки установленного пакета, но не текущим ключом. Возможен downgrade или старый пакет."
                SigningLineageResult.MULTI_SIGNER_MISMATCH ->
                    "Для multi-signer пакета набор сертификатов отличается. Модель ротации ключей для него не применяется."
                else ->
                    "Не найдена Android-подтверждённая цепочка ротации от установленного ключа к этому APK. Не устанавливайте файл до ручной проверки издателя."
            }
            risks += ApkRisk("Подпись не совпала с текущей цепочкой", detail, "HIGH")
        }
        if (lineageResult == SigningLineageResult.VERIFIED_FORWARD_ROTATION) {
            risks += ApkRisk(
                "Подтверждённая ротация ключа",
                "Android предоставил proof-of-rotation, связывающий сертификат установленной версии с новым ключом APK. Это не является проверкой репутации издателя.",
                "INFO"
            )
        }
        threatMatches.forEach { indicator ->
            risks += ApkRisk(
                indicator.title,
                "${indicator.detail}\nСовпадение подписанного индикатора: ${indicator.id}; confidence ${indicator.confidence}/100. Это не самостоятельное доказательство вредоносного поведения.",
                indicator.severity
            )
        }
        return ApkXRayReport(
            packageName = packageName,
            versionName = info.versionName.orEmpty(),
            versionCode = readVersionCode(info),
            targetSdk = app.targetSdkVersion,
            minSdk = app.minSdkVersion,
            apkSha256 = apkDigest,
            signerSha256 = signers,
            signerHistorySha256 = signerHistory,
            signerRotationResult = lineageResult.name,
            signerRotationExplanation = signerExplanation(lineageResult),
            installedVersionName = installedInfo?.versionName,
            installedSignerMatch = signerMatch,
            requestedPermissions = requested,
            requestedPermissionAssessments = permissionAssessments,
            exportedComponents = exported,
            exportedWithoutPermission = exportedWithoutPermission,
            exportedComponentDetails = exportedDetails.take(30),
            dexSignals = dexScan?.signals.orEmpty(),
            dexFilesScanned = dexScan?.dexFilesScanned ?: 0,
            dexBytesScanned = dexScan?.bytesScanned ?: 0L,
            dexScanTruncated = dexScan?.truncated ?: true,
            dexScanDurationMs = dexScan?.durationMs ?: 0L,
            dexLimitReason = dexScan?.limitReason,
            extractedUrls = stringScan?.urls.orEmpty(),
            stringFindings = stringScan?.findings.orEmpty(),
            stringScanEntries = stringScan?.entriesScanned ?: 0,
            stringScanBytes = stringScan?.bytesScanned ?: 0L,
            stringScanTruncated = stringScan?.truncated ?: true,
            nativeLibraries = archiveScan?.nativeLibraries.orEmpty(),
            nestedApkEntries = archiveScan?.nestedApkEntries.orEmpty(),
            archiveWarnings = archiveScan?.suspiciousArchiveEntries.orEmpty(),
            archiveTruncated = archiveScan?.truncated ?: true,
            binaryManifest = binaryManifest,
            threatIndicators = threatMatches,
            risks = risks,
            fileSizeBytes = file.length()
        )
    }

    private fun resolveRequestedPermissions(
        pm: PackageManager,
        permissions: List<String>
    ): List<RequestedPermissionAssessment> = permissions.distinct().take(200).map { name ->
        val info = runCatching {
            if (Build.VERSION.SDK_INT >= 33) {
                @Suppress("DEPRECATION")
                pm.getPermissionInfo(name, 0)
            } else {
                @Suppress("DEPRECATION")
                pm.getPermissionInfo(name, 0)
            }
        }.getOrNull()
        if (info == null) {
            RequestedPermissionAssessment(name, "UNRESOLVED · отсутствует в реестре устройства", null)
        } else {
            val base = when (info.protectionLevel and 0x0f) {
                0 -> "NORMAL"
                1 -> "DANGEROUS"
                2 -> "SIGNATURE"
                3 -> "SIGNATURE_OR_SYSTEM (legacy)"
                else -> "UNKNOWN"
            }
            val flags = mutableListOf<String>()
            val flagNames = listOf(
                0x10 to "PRIVILEGED", 0x20 to "DEVELOPMENT", 0x40 to "APPOP",
                0x80 to "PRE23", 0x100 to "INSTALLER", 0x200 to "VERIFIER",
                0x400 to "PREINSTALLED", 0x800 to "SETUP",
                0x1000 to "INSTANT", 0x2000 to "RUNTIME_ONLY",
                0x4000 to "OEM", 0x8000 to "VENDOR_PRIVILEGED",
                0x10000 to "TEXT_CLASSIFIER", 0x20000 to "CONFIGURATOR",
                0x40000 to "INCIDENT_REPORT_APPROVER", 0x80000 to "APPROVER",
                0x100000 to "ROLE", 0x800000 to "KNOWN_SIGNER"
            )
            flagNames.forEach { (mask, label) ->
                if (info.protectionLevel and mask != 0) flags += label
            }
            RequestedPermissionAssessment(
                name,
                if (flags.isEmpty()) base else "$base | ${flags.joinToString()}",
                info.packageName
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun readInstalledPackageInfo(pm: PackageManager, packageName: String): PackageInfo? {
        return runCatching {
            val flags = if (Build.VERSION.SDK_INT >= 28) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                PackageManager.GET_SIGNATURES
            }
            if (Build.VERSION.SDK_INT >= 33) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                pm.getPackageInfo(packageName, flags)
            }
        }.getOrNull()
    }

    @Suppress("DEPRECATION")
    private fun readVersionCode(info: PackageInfo): Long =
        if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()

    private fun readSigningLineage(info: PackageInfo): SigningLineage? {
        if (Build.VERSION.SDK_INT >= 28) {
            val signingInfo = info.signingInfo ?: return null
            val current = signingInfo.apkContentsSigners.orEmpty()
                .map { sha256(it.toByteArray()) }.toSet()
            if (current.isEmpty()) return null
            val multiple = signingInfo.hasMultipleSigners()
            val history = if (multiple) current.toList() else {
                signingInfo.signingCertificateHistory.orEmpty()
                    .map { sha256(it.toByteArray()) }
            }
            return SigningLineage(current, history, multiple)
        }
        @Suppress("DEPRECATION")
        val current = info.signatures.orEmpty().map { sha256(it.toByteArray()) }.toSet()
        return current.takeIf { it.isNotEmpty() }?.let { SigningLineage(it, it.toList(), false) }
    }

    private fun signerExplanation(result: SigningLineageResult): String = when (result) {
        SigningLineageResult.EXACT_MATCH -> "Текущий набор сертификатов совпадает."
        SigningLineageResult.VERIFIED_FORWARD_ROTATION -> "Android подтвердил цепочку ротации от текущего ключа установленной версии к ключу выбранного APK."
        SigningLineageResult.HISTORICAL_SIGNER -> "Выбранный APK использует исторический сертификат из цепочки установленного приложения. Проверьте возможное понижение версии."
        SigningLineageResult.MULTI_SIGNER_MATCH -> "Полный набор подписантов совпадает; используется точное сравнение."
        SigningLineageResult.MULTI_SIGNER_MISMATCH -> "Набор подписантов отличается. Ротация ключей не применяется к multi-signer пакетам."
        SigningLineageResult.MISMATCH -> "Цепочки сертификатов не совпадают."
        SigningLineageResult.UNKNOWN -> "Android не предоставил достаточно данных для достоверного сравнения."
    }

    private fun exportedComponentDetails(info: PackageInfo): List<ExportedComponentExposure> {
        val result = mutableListOf<ExportedComponentExposure>()
        info.activities.orEmpty().filter { it.exported }.forEach {
            result += ExportedComponentExposure("Activity", it.name.orEmpty(), it.permission.orEmpty())
        }
        info.services.orEmpty().filter { it.exported }.forEach {
            result += ExportedComponentExposure("Service", it.name.orEmpty(), it.permission.orEmpty())
        }
        info.receivers.orEmpty().filter { it.exported }.forEach {
            result += ExportedComponentExposure("Receiver", it.name.orEmpty(), it.permission.orEmpty())
        }
        info.providers.orEmpty().filter { it.exported }.forEach {
            val permissions = listOfNotNull(it.readPermission, it.writePermission)
                .filter { value -> value.isNotBlank() }.distinct().joinToString(", ")
            result += ExportedComponentExposure("Provider", it.name.orEmpty(), permissions)
        }
        return result
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
            "enabled_notification_listeners"
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
