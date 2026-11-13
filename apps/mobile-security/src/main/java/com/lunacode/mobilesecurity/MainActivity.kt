package com.lunacode.mobilesecurity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.lunacode.mobilesecurity.data.ProSecDatabase
import com.lunacode.mobilesecurity.data.ScanRecord
import com.lunacode.mobilesecurity.intelligence.AttackChain
import com.lunacode.mobilesecurity.intelligence.AttackChainSignalAdapter
import com.lunacode.mobilesecurity.intelligence.ChangeSentinel
import com.lunacode.mobilesecurity.intelligence.SecurityPosture
import com.lunacode.mobilesecurity.intelligence.SecurityPostureReport
import com.lunacode.mobilesecurity.scanner.AuditFinding
import com.lunacode.mobilesecurity.monitor.DailyAuditWorker
import com.lunacode.mobilesecurity.security.TrustedBaselineStore
import com.lunacode.mobilesecurity.security.LocalEvidenceSeal
import com.lunacode.mobilesecurity.security.HistoryChainRecord
import com.lunacode.mobilesecurity.security.HistoryChainVerifier
import com.lunacode.mobilesecurity.security.LocalPayloadCipher
import com.lunacode.mobilesecurity.security.ReportEnvelope
import com.lunacode.mobilesecurity.security.ReportEnvelopeCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

private val ProBlack = ProSecPalette.Background
private val ProPanel = ProSecPalette.Surface
private val ProWhite = ProSecPalette.White
private val ProMuted = ProSecPalette.Muted
private val ProGreen = ProSecPalette.Accent
private val ProAmber = ProSecPalette.Warning
private val ProRed = ProSecPalette.Danger
private const val MANUAL_SCAN_WORK = "pro-sec-manual-audit"

private data class FindingUi(
    val title: String,
    val detail: String,
    val severity: String,
    val id: String = "",
    val source: String = ""
)
private data class DecodedScanRecord(
    val record: ScanRecord,
    val json: JSONObject,
    val envelope: ReportEnvelope,
    var integrityStatus: String
)

private data class ScanReportUi(
    val scannedAt: Long,
    val findings: List<FindingUi>,
    val changes: List<KillerChangeUi>,
    val attackChains: List<AttackChain>,
    val firstBaseline: Boolean,
    val history: List<KillerHistoryUi>,
    val posture: SecurityPostureReport,
    val integrityStatus: String,
    val scanDurationMs: Long,
    val appCount: Int,
    val trustedBaselineActive: Boolean,
    val trustedBaselineInvalid: Boolean,
    val trustedBaselineSavedAt: Long
) {
    val highCount get() = findings.count { it.severity == "HIGH" }
    val mediumCount get() = findings.count { it.severity == "MEDIUM" }
    val lowCount get() = findings.count { it.severity == "LOW" }
}

class MainActivity : ComponentActivity() {
    private var report by mutableStateOf<ScanReportUi?>(null)
    private var scanning by mutableStateOf(false)
    private var scanMessage by mutableStateOf("Проверка ещё не запускалась")
    private var dailyMonitoringEnabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureSystemBars()
        dailyMonitoringEnabled = getSharedPreferences("pro_sec_preferences", MODE_PRIVATE)
            .getBoolean("daily_monitoring_enabled", false)
        observeManualScan()
        refreshLatest()
        setContent {
            ProSecTheme {
                SecurityHome(
                    report = report,
                    scanning = scanning,
                    scanMessage = scanMessage,
                    dailyMonitoringEnabled = dailyMonitoringEnabled,
                    onDailyMonitoringChange = { enabled -> if (!enabled) setDailyMonitoring(false) },
                    onRefresh = { refreshLatest() },
                    onScan = { enabled -> enqueueScan(enabled) },
                    onSetTrustedBaseline = { saveTrustedBaseline() }
                )
            }
        }
    }

    private fun configureSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }

    private fun observeManualScan() {
        WorkManager.getInstance(this)
            .getWorkInfosForUniqueWorkLiveData(MANUAL_SCAN_WORK)
            .observe(this) { workInfos ->
                val work = workInfos.firstOrNull() ?: return@observe
                when (work.state) {
                    WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED, WorkInfo.State.RUNNING -> {
                        scanning = true
                        scanMessage = "Проверка выполняется в фоновом режиме."
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        scanning = false
                        scanMessage = "Проверка завершена."
                        refreshLatest()
                    }
                    WorkInfo.State.FAILED -> {
                        scanning = false
                        scanMessage = "Не удалось завершить проверку. Повторите попытку."
                    }
                    WorkInfo.State.CANCELLED -> {
                        scanning = false
                        scanMessage = "Проверка отменена."
                    }
                }
            }
    }

    private fun refreshLatest() {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val dao = ProSecDatabase.get(applicationContext).scanDao()
                    val cipher = LocalPayloadCipher()
                    val recentRecords = dao.recent(8)
                    // Keep every retained Room row visible. Unreadable or malformed
                    // rows become explicit FAILED timeline entries instead of disappearing.
                    val decoded = recentRecords.map { record ->
                        runCatching {
                            val envelope = ReportEnvelopeCodec.decode(
                                cipher.decrypt(record.encryptedPayload)
                            )
                            require(envelope.report.isNotBlank()) { "Empty or malformed report envelope" }
                            val integrityStatus = when {
                                !envelope.wrapped -> "LEGACY"
                                envelope.chainTag == null -> "UNSEALED"
                                else -> {
                                    val verification = runCatching {
                                        LocalEvidenceSeal.verify(
                                            ReportEnvelopeCodec.material(envelope.previousTag, envelope.report),
                                            envelope.chainTag
                                        )
                                    }
                                    when {
                                        verification.isFailure -> "UNVERIFIABLE"
                                        verification.getOrNull() == true -> "VERIFIED"
                                        else -> "FAILED"
                                    }
                                }
                            }
                            DecodedScanRecord(
                                record = record,
                                json = JSONObject(envelope.report),
                                envelope = envelope,
                                integrityStatus = integrityStatus
                            )
                        }.getOrElse {
                            DecodedScanRecord(
                                record = record,
                                json = JSONObject().apply {
                                    put("schema", 1)
                                    put("findings", JSONArray())
                                    put("changes", JSONArray())
                                    put("findingCount", 0)
                                    put("changeCount", 0)
                                },
                                envelope = ReportEnvelope("", "", null, true),
                                integrityStatus = "FAILED"
                            )
                        }
                    }
                    val chronological = decoded.asReversed()
                    val chainStatuses = HistoryChainVerifier.verifyChronological(
                        chronological.map { scan ->
                            HistoryChainRecord(
                                id = scan.record.id,
                                previousTag = scan.envelope.previousTag,
                                chainTag = scan.envelope.chainTag,
                                status = scan.integrityStatus
                            )
                        }
                    ).associateBy { it.id }
                    chronological.forEach { scan ->
                        scan.integrityStatus = chainStatuses[scan.record.id]?.status ?: "FAILED"
                    }
                    val latest = decoded.firstOrNull() ?: return@runCatching null
                    val record = latest.record
                    val json = latest.json
                    val schema = json.optInt("schema", 1)
                    require(schema in 1..2) { "Unsupported report schema" }
                    val array = json.optJSONArray("findings")
                        ?: error("Report findings are missing")
                    val findings = buildList {
                        for (i in 0 until array.length()) {
                            val item = array.optJSONObject(i) ?: continue
                            add(
                                FindingUi(
                                    title = item.optString("title", "Без названия"),
                                    detail = item.optString("detail", ""),
                                    severity = item.optString("severity", "INFO"),
                                    id = item.optString("id", ""),
                                    source = item.optString("source", "")
                                )
                            )
                        }
                    }.sortedBy { severityOrder(it.severity) }

                    val postureFindings = findings.map { finding ->
                        AuditFinding(
                            id = finding.id,
                            title = finding.title,
                            detail = finding.detail,
                            severity = finding.severity,
                            source = finding.source
                        )
                    }.toMutableList()
                    if (latest.integrityStatus == "FAILED") {
                        postureFindings += AuditFinding(
                            id = "evidence-chain-failed",
                            title = "Целостность цепочки отчётов не подтверждена",
                            detail = "Одна из проверок HMAC или связности соседних записей не прошла. Это может указывать на изменение истории или смену/потерю локального ключа; проверьте статус и происхождение резервной копии.",
                            severity = "HIGH",
                            source = "LocalEvidenceSeal"
                        )
                    }
                    val posture = SecurityPosture.evaluate(postureFindings)
                    val scanMetrics = json.optJSONObject("scanMetrics")
                    val sentinel = ChangeSentinel.read(json)
                    val changes = sentinel.changes.map {
                        KillerChangeUi(it.kind, it.title, it.detail, it.level)
                    }
                    val attackChains = AttackChainSignalAdapter.detect(
                        findings = findings.map { finding ->
                            AuditFinding(finding.id, finding.title, finding.detail, finding.severity, finding.source)
                        },
                        changes = sentinel.changes,
                        observedAtMillis = record.scannedAt
                    )
                    val history = decoded.map { scan ->
                        KillerHistoryUi(
                            scannedAt = scan.record.scannedAt,
                            findingCount = scan.json.optInt("findingCount"),
                            changeCount = scan.json.optInt("changeCount"),
                            integrityStatus = scan.integrityStatus
                        )
                    }
                    ScanReportUi(
                        scannedAt = record.scannedAt,
                        findings = findings,
                        changes = changes,
                        attackChains = attackChains,
                        firstBaseline = schema < 2 || sentinel.isFirstBaseline,
                        history = history,
                        posture = posture,
                        integrityStatus = latest.integrityStatus,
                        scanDurationMs = scanMetrics?.optLong("durationMs") ?: 0L,
                        appCount = scanMetrics?.optInt("appCount") ?: json.optJSONArray("apps")?.length() ?: 0,
                    trustedBaselineActive = json.optBoolean("trustedBaselineActive"),
                    trustedBaselineInvalid = json.optBoolean("trustedBaselineInvalid"),
                    trustedBaselineSavedAt = json.optLong("trustedBaselineSavedAt")
                    )
                }
            }
            result.onSuccess { latest ->
                report = latest
                if (latest != null && !scanning) {
                    scanMessage = if (latest.integrityStatus == "FAILED" ||
                        latest.integrityStatus == "UNVERIFIABLE"
                    ) {
                        "Последняя запись сохранена, но её целостность не подтверждена. Данные не удалены."
                    } else {
                        "Последняя проверка: " +
                            DateFormat.getDateTimeInstance().format(Date(latest.scannedAt))
                    }
                }
            }.onFailure {
                report = null
                scanMessage = "Не удалось прочитать локальный отчёт. Данные не удалены."
            }
        }
    }

    private fun saveTrustedBaseline() {
        lifecycleScope.launch {
            val savedAt = System.currentTimeMillis()
            val saved = withContext(Dispatchers.IO) {
                runCatching {
                    val record = ProSecDatabase.get(applicationContext).scanDao().latest()
                        ?: return@withContext false
                    val envelope = ReportEnvelopeCodec.decode(LocalPayloadCipher().decrypt(record.encryptedPayload))
                    if (!envelope.wrapped || envelope.chainTag.isNullOrBlank() || envelope.report.isBlank()) return@withContext false
                    if (!LocalEvidenceSeal.verify(ReportEnvelopeCodec.material(envelope.previousTag, envelope.report), envelope.chainTag)) return@withContext false
                    TrustedBaselineStore.save(applicationContext, envelope.report)
                    true
                }.getOrDefault(false)
            }
            if (saved) {
                report = report?.copy(trustedBaselineActive = true, trustedBaselineInvalid = false, trustedBaselineSavedAt = savedAt)
                scanMessage = "Проверенный отчёт сохранён как Trusted Baseline. Он будет применён при следующем аудите."
            } else {
                scanMessage = "Не удалось сохранить Trusted Baseline: нет проверенного локального отчёта."
            }
        }
    }

    private fun setDailyMonitoring(enabled: Boolean) {
        dailyMonitoringEnabled = enabled
        getSharedPreferences("pro_sec_preferences", MODE_PRIVATE).edit()
            .putBoolean("daily_monitoring_enabled", enabled)
            .apply()
        val workManager = WorkManager.getInstance(this)
        if (enabled) {
            val daily = PeriodicWorkRequestBuilder<DailyAuditWorker>(24, TimeUnit.HOURS).build()
            workManager.enqueueUniquePeriodicWork(
                "pro-sec-daily-audit",
                ExistingPeriodicWorkPolicy.KEEP,
                daily
            )
        } else {
            workManager.cancelUniqueWork("pro-sec-daily-audit")
        }
    }

    private fun enqueueScan(enableDailyMonitoring: Boolean) {
        setDailyMonitoring(enableDailyMonitoring)
        val workManager = WorkManager.getInstance(this)
        val manual = OneTimeWorkRequestBuilder<DailyAuditWorker>().build()
        workManager.enqueueUniqueWork(
            MANUAL_SCAN_WORK,
            ExistingWorkPolicy.KEEP,
            manual
        )
    }
}

private fun severityOrder(severity: String): Int = when (severity) {
    "HIGH" -> 0
    "MEDIUM" -> 1
    "LOW" -> 2
    else -> 3
}

@Composable
private fun ProSecTheme(content: @Composable () -> Unit) {
    ProSecMaterialTheme(content)
}

@Composable
private fun SecurityHome(
    report: ScanReportUi?,
    scanning: Boolean,
    scanMessage: String,
    dailyMonitoringEnabled: Boolean,
    onDailyMonitoringChange: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onScan: (Boolean) -> Unit,
    onSetTrustedBaseline: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        containerColor = ProBlack,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(containerColor = ProPanel, contentColor = ProWhite, tonalElevation = 0.dp) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("◉", fontSize = 16.sp) },
                    label = { Text("Обзор") },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ProWhite, selectedTextColor = ProWhite,
                        indicatorColor = ProSecPalette.Elevated,
                        unselectedIconColor = ProMuted, unselectedTextColor = ProMuted
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("✦", fontSize = 16.sp) },
                    label = { Text("Киллер-функции") },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ProWhite, selectedTextColor = ProWhite,
                        indicatorColor = ProSecPalette.Elevated,
                        unselectedIconColor = ProMuted, unselectedTextColor = ProMuted
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ProBlack)
        ) {
            if (selectedTab == 0) {
                SecurityOverview(
                    report = report,
                    scanning = scanning,
                    scanMessage = scanMessage,
                    dailyMonitoringEnabled = dailyMonitoringEnabled,
                    onDailyMonitoringChange = onDailyMonitoringChange,
                    onRefresh = onRefresh,
                    onScan = onScan,
                    onSetTrustedBaseline = onSetTrustedBaseline
                )
            } else {
                KillerFeaturesScreen(
                    changes = report?.changes.orEmpty(),
                    firstBaseline = report?.firstBaseline ?: true,
                    hasReport = report != null,
                    history = report?.history.orEmpty(),
                    postureReport = report?.posture,
                    integrityStatus = report?.integrityStatus ?: "LEGACY"
                )
            }
        }
    }
}

@Composable
private fun SecurityOverview(
    report: ScanReportUi?,
    scanning: Boolean,
    scanMessage: String,
    dailyMonitoringEnabled: Boolean,
    onDailyMonitoringChange: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onScan: (Boolean) -> Unit,
    onSetTrustedBaseline: () -> Unit
) {
    var showConsent by remember { mutableStateOf(false) }
    var requestedDailyMonitoring by remember(dailyMonitoringEnabled) {
        mutableStateOf(dailyMonitoringEnabled)
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ProBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 20.dp,
            vertical = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("PRO SEC", color = ProWhite, fontSize = 25.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
                    Text("АУДИТ ANDROID", color = ProMuted, fontSize = 10.sp, letterSpacing = 2.5.sp)
                }
                Text("● ЛОКАЛЬНО", color = ProGreen, fontSize = 11.sp)
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, ProSecPalette.Border),
                colors = CardDefaults.cardColors(containerColor = ProPanel)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("СОСТОЯНИЕ АУДИТА", color = ProMuted, fontSize = 11.sp, letterSpacing = 2.sp)
                    Text(
                        when {
                            scanning -> "Проверка выполняется"
                            report != null -> "Отчёт готов"
                            else -> "Ожидает проверки"
                        },
                        color = ProWhite,
                        fontSize = 24.sp
                    )
                    if (report != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Metric("ВЫСОКИЙ", report.highCount, ProRed)
                            Metric("СРЕДНИЙ", report.mediumCount, ProAmber)
                            Metric("НИЗКИЙ", report.lowCount, ProMuted)
                        }
                        Text(
                            "Приложений: ${report.appCount} · сигналов: ${report.findings.size} · время: ${report.scanDurationMs} мс",
                            color = ProMuted,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            "PRO SEC проверяет доступные сведения об установленных приложениях, специальных доступах, администраторах и VPN.",
                            color = ProMuted,
                            fontSize = 13.sp
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = requestedDailyMonitoring,
                            onCheckedChange = { enabled ->
                                requestedDailyMonitoring = enabled
                                // Disabling is immediate. Enabling is only scheduled after consent.
                                if (!enabled) onDailyMonitoringChange(false)
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ежедневный мониторинг", color = ProWhite, fontSize = 13.sp)
                            Text(
                                when {
                                    requestedDailyMonitoring && !dailyMonitoringEnabled ->
                                        "Включится после подтверждения следующей проверки."
                                    requestedDailyMonitoring ->
                                        "Плановая проверка примерно раз в 24 часа. Android может задерживать её."
                                    else -> "Выключен. Автоматические проверки не запланированы."
                                },
                                color = ProMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Button(
                        onClick = { showConsent = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !scanning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProGreen,
                            contentColor = ProBlack
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (scanning) "ПРОВЕРКА..." else "ЗАПУСТИТЬ ПРОВЕРКУ")
                    }
                    Text(scanMessage, color = ProMuted, fontSize = 11.sp)
                    Button(
                        onClick = onRefresh,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProPanel,
                            contentColor = ProWhite
                        )
                    ) { Text("ОБНОВИТЬ ОТЧЁТ") }
                }
            }
        }
        item {
            if (report != null) {
                Text("INCIDENT RESPONSE CENTER", color = ProMuted, fontSize = 10.sp, letterSpacing = 1.8.sp)
                SecurityPostureCard(report.posture, compact = true)
            }
        }
        item {
            report?.let { EvidenceSealCard(it.integrityStatus, compact = true) }
        }
        if (report != null) {
            item {
                val granted = report.findings.count { it.id.startsWith("granted-permissions-") }
                val requested = report.findings.count { it.id.startsWith("permissions-") }
                val special = report.findings.count {
                    it.id.startsWith("accessibility-") && it.id != "accessibility-none" ||
                        it.id.startsWith("notification-listener-") && it.id != "notification-listener-none" ||
                        it.id.startsWith("admin-") && it.id != "admin-none" ||
                        it.id.startsWith("input-method-") && it.id != "input-method-none"
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ProSecPalette.Border),
                    colors = CardDefaults.cardColors(containerColor = ProPanel)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("PERMISSION INTELLIGENCE", color = ProMuted, fontSize = 10.sp, letterSpacing = 1.5.sp)
                        Text("Выданные разрешения: $granted · широкие запросы: $requested · специальные доступы: $special", color = ProWhite, fontSize = 13.sp)
                        Text("Запрошенные разрешения не равны выданным. Специальные доступы показываются отдельно; доступность данных зависит от версии Android и производителя.", color = ProMuted, fontSize = 11.sp)
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ProSecPalette.Border),
                    colors = CardDefaults.cardColors(containerColor = ProPanel)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("TRUSTED BASELINE", color = ProMuted, fontSize = 10.sp, letterSpacing = 1.5.sp)
                        Text(
                            when {
                                report.trustedBaselineInvalid -> "Пользовательская базовая точка повреждена или не прошла проверку"
                                report.trustedBaselineActive -> "Сравнение с утверждённой Trusted Baseline активно"
                                report.firstBaseline -> "Базовая точка ещё не создана"
                                else -> "Сравнение с предыдущей проверкой; пользовательская база не утверждена"
                            },
                            color = if (report.trustedBaselineInvalid) ProAmber else ProWhite,
                            fontSize = 14.sp
                        )
                        Text("Изменений относительно точки сравнения: ${report.changes.size}. Данные хранятся локально и защищены шифрованием и HMAC. Отсутствие изменений не доказывает безопасность.", color = ProMuted, fontSize = 11.sp)
                        Button(
                            onClick = onSetTrustedBaseline,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ProSecPalette.Elevated, contentColor = ProWhite)
                        ) { Text(if (report.trustedBaselineActive) "ОБНОВИТЬ TRUSTED BASELINE" else "УТВЕРДИТЬ ТЕКУЩЕЕ СОСТОЯНИЕ") }
                    }
                }
            }
        }
        if (report != null) {
            item {
                Text("ANTI-SPYWARE GUARD · ${report.attackChains.size} СВЯЗАННЫХ ЦЕПОЧЕК", color = ProAmber, fontSize = 11.sp, letterSpacing = 1.5.sp)
            }
            if (report.attackChains.isEmpty()) {
                item {
                    Text("По доступным сигналам связанные цепочки не выявлены. Это не означает, что устройство полностью безопасно: Android ограничивает видимость части системных данных.", color = ProMuted, fontSize = 11.sp)
                }
            } else {
                items(report.attackChains) { chain -> AttackChainCard(chain) }
            }
        }
        if (report != null && report.changes.isNotEmpty()) {
            item {
                Text(
                    "CHANGE SENTINEL · ${report.changes.size} ИЗМЕНЕНИЙ",
                    color = ProAmber,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp
                )
            }
            items(report.changes.take(8)) { change ->
                val color = when (change.level) {
                    "HIGH" -> ProRed
                    "MEDIUM" -> ProAmber
                    else -> ProMuted
                }
                FindingCard(
                    FindingUi(change.title, change.detail, change.level),
                    color
                )
            }
        }
        item {
            Text(
                "РЕЗУЛЬТАТЫ ПОСЛЕДНЕЙ ПРОВЕРКИ",
                color = ProMuted,
                fontSize = 11.sp,
                letterSpacing = 2.sp
            )
        }
        if (report == null) {
            item {
                AuditModule("—", "Отчётов пока нет", "Запустите проверку, чтобы получить локальный отчёт.")
            }
        } else {
            items(report.findings) { finding -> FindingCard(finding) }
        }
        item { Text("МОДУЛИ АУДИТА", color = ProMuted, fontSize = 11.sp, letterSpacing = 2.sp) }
        item {
            AuditModule(
                "01",
                "Приложения",
                "Инвентаризация, известные средства удалённого доступа и признаки риска"
            )
        }
        item {
            AuditModule(
                "02",
                "Специальные доступы",
                "Включённые службы Accessibility и активные администраторы"
            )
        }
        item { AuditModule("03", "Сетевые подключения", "Обнаружение активного VPN-транспорта") }
        item { AuditModule("04", "История проверок", "Локальное сохранение зашифрованных результатов") }
        item {
            Text(
                "Данные не отправляются на сервер. Автоматическая проверка планируется примерно раз в 24 часа; Android может сдвигать время выполнения. Индикаторы риска требуют ручной проверки и не являются доказательством заражения.",
                color = ProMuted,
                fontSize = 11.sp
            )
        }
    }

    if (showConsent) {
        AlertDialog(
            onDismissRequest = { showConsent = false; requestedDailyMonitoring = dailyMonitoringEnabled },
            containerColor = ProPanel,
            title = { Text("Локальная проверка PRO SEC", color = ProWhite) },
            text = {
                Text(
                    "PRO SEC прочитает доступные сведения об установленных приложениях, чувствительных разрешениях, специальных доступах, администраторах, VPN, блокировке экрана и метаданных устройства. Отчёт останется на устройстве в зашифрованном виде; сетевой передачи нет. ${if (requestedDailyMonitoring) "Вы также разрешаете плановые локальные проверки примерно раз в 24 часа. Их время зависит от Android и режима энергосбережения." else "Ежедневный мониторинг выключен; будет выполнена только разовая проверка."} Продолжить?",
                    color = ProMuted
                )
            },
            confirmButton = {
                Button(onClick = { showConsent = false; onScan(requestedDailyMonitoring) }) {
                    Text("СОГЛАСЕН И ПРОВЕРИТЬ")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConsent = false; requestedDailyMonitoring = dailyMonitoringEnabled },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProPanel,
                        contentColor = ProWhite
                    )
                ) { Text("ОТМЕНА") }
            }
        )
    }
}

@Composable
private fun Metric(label: String, value: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value.toString(), color = color, fontSize = 23.sp)
        Text(label, color = ProMuted, fontSize = 9.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun AttackChainCard(chain: AttackChain) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ProSecPalette.Warning.copy(alpha = 0.45f)),
        colors = CardDefaults.cardColors(containerColor = ProPanel)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("СВЯЗАННАЯ ПОСЛЕДОВАТЕЛЬНОСТЬ · ${chain.severity}", color = ProAmber, fontSize = 10.sp, letterSpacing = 1.2.sp)
            Text(chain.title, color = ProWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(chain.packageName, color = ProMuted, fontSize = 11.sp)
            Text(chain.explanation, color = ProMuted, fontSize = 12.sp)
            Text("Сила сочетания признаков: ${chain.confidenceScore}/100 · событий: ${chain.signals.size}", color = ProWhite, fontSize = 11.sp)
        }
    }
}

@Composable
private fun FindingCard(finding: FindingUi, colorOverride: Color? = null) {
    val color = colorOverride ?: when (finding.severity) {
        "HIGH" -> ProRed
        "MEDIUM" -> ProAmber
        "LOW" -> ProMuted
        else -> ProGreen
    }
    val label = when (finding.severity) {
        "HIGH" -> "ВЫСОКИЙ РИСК"
        "MEDIUM" -> "ТРЕБУЕТ ПРОВЕРКИ"
        "LOW" -> "НИЗКИЙ РИСК"
        else -> "ИНФОРМАЦИЯ"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = ProPanel)
    ) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(label, color = color, fontSize = 10.sp, letterSpacing = 1.5.sp)
            Text(finding.title, color = ProWhite, fontSize = 14.sp)
            Text(finding.detail, color = ProMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AuditModule(number: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ProPanel, RoundedCornerShape(16.dp))
            .padding(15.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(number, color = ProGreen, fontSize = 14.sp)
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = ProWhite, fontSize = 14.sp)
            Text(description, color = ProMuted, fontSize = 11.sp)
        }
    }
}
