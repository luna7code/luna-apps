package com.lunacode.mobilesecurity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lunacode.mobilesecurity.data.ProSecDatabase
import com.lunacode.mobilesecurity.monitor.DailyAuditWorker
import com.lunacode.mobilesecurity.security.LocalPayloadCipher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

private val ProBlack = Color(0xFF080808)
private val ProPanel = Color(0xFF1D1D1D)
private val ProWhite = Color(0xFFFFFFFF)
private val ProMuted = Color(0xFFC2C2C2)
private val ProGreen = Color(0xFF43D17A)
private val ProAmber = Color(0xFFFFC15A)
private val ProRed = Color(0xFFFF6B6B)

private data class FindingUi(val title: String, val detail: String, val severity: String)
private data class ScanReportUi(val scannedAt: Long, val findings: List<FindingUi>) {
    val highCount get() = findings.count { it.severity == "HIGH" }
    val mediumCount get() = findings.count { it.severity == "MEDIUM" }
    val lowCount get() = findings.count { it.severity == "LOW" }
}

class MainActivity : ComponentActivity() {
    private var report by mutableStateOf<ScanReportUi?>(null)
    private var scanning by mutableStateOf(false)
    private var scanMessage by mutableStateOf("Проверка ещё не запускалась")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshLatest()
        setContent {
            ProSecTheme {
                SecurityHome(
                    report = report,
                    scanning = scanning,
                    scanMessage = scanMessage,
                    onRefresh = { refreshLatest() },
                    onScan = { enqueueScan() }
                )
            }
        }
    }

    private fun refreshLatest() {
        lifecycleScope.launch {
            val latest = withContext(Dispatchers.IO) {
                runCatching {
                    val record = ProSecDatabase.get(applicationContext).scanDao().latest()
                        ?: return@runCatching null
                    val json = JSONObject(LocalPayloadCipher().decrypt(record.encryptedPayload))
                    val array = json.optJSONArray("findings")
                    val findings = buildList {
                        if (array != null) for (i in 0 until array.length()) {
                            val item = array.optJSONObject(i) ?: continue
                            add(FindingUi(
                                title = item.optString("title", "Без названия"),
                                detail = item.optString("detail", ""),
                                severity = item.optString("severity", "INFO")
                            ))
                        }
                    }
                    ScanReportUi(record.scannedAt, findings)
                }.getOrNull()
            }
            report = latest
            if (latest != null && !scanning) {
                scanMessage = "Последняя проверка: " + DateFormat.getDateTimeInstance().format(Date(latest.scannedAt))
            }
        }
    }

    private fun enqueueScan() {
        scanning = true
        scanMessage = "Проверка выполняется. Результат останется на устройстве."
        val workManager = WorkManager.getInstance(this)
        val daily = PeriodicWorkRequestBuilder<DailyAuditWorker>(24, TimeUnit.HOURS).build()
        workManager.enqueueUniquePeriodicWork(
            "pro-sec-daily-audit",
            ExistingPeriodicWorkPolicy.KEEP,
            daily
        )
        workManager.enqueue(OneTimeWorkRequestBuilder<DailyAuditWorker>().build())
        lifecycleScope.launch {
            repeat(45) {
                delay(1_000)
                val latest = withContext(Dispatchers.IO) {
                    runCatching {
                        val record = ProSecDatabase.get(applicationContext).scanDao().latest()
                            ?: return@runCatching null
                        val json = JSONObject(LocalPayloadCipher().decrypt(record.encryptedPayload))
                        val array = json.optJSONArray("findings")
                        val findings = buildList {
                            if (array != null) for (i in 0 until array.length()) {
                                val item = array.optJSONObject(i) ?: continue
                                add(FindingUi(item.optString("title"), item.optString("detail"), item.optString("severity", "INFO")))
                            }
                        }
                        ScanReportUi(record.scannedAt, findings)
                    }.getOrNull()
                }
                if (latest != null && (report == null || latest.scannedAt > report!!.scannedAt)) {
                    report = latest
                    scanning = false
                    scanMessage = "Проверка завершена: " + DateFormat.getDateTimeInstance().format(Date(latest.scannedAt))
                    return@launch
                }
            }
            scanning = false
            scanMessage = "Задача передана Android. Обновите отчёт чуть позже."
        }
    }
}

@Composable
private fun ProSecTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = ProBlack,
            surface = ProPanel,
            primary = ProGreen,
            onPrimary = ProBlack,
            onBackground = ProWhite,
            onSurface = ProWhite
        ),
        content = content
    )
}

@Composable
private fun SecurityHome(
    report: ScanReportUi?,
    scanning: Boolean,
    scanMessage: String,
    onRefresh: () -> Unit,
    onScan: () -> Unit
) {
    var showConsent by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(ProBlack),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("PRO SEC", color = ProWhite, fontSize = 25.sp, letterSpacing = 2.sp)
                    Text("ANDROID SECURITY", color = ProMuted, fontSize = 10.sp, letterSpacing = 3.sp)
                }
                Text("● ЛОКАЛЬНО", color = ProGreen, fontSize = 11.sp)
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ProPanel)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("СОСТОЯНИЕ ЗАЩИТЫ", color = ProMuted, fontSize = 11.sp, letterSpacing = 2.sp)
                    Text(
                        when { scanning -> "Проверка выполняется"; report != null -> "Отчёт готов"; else -> "Ожидает проверки" },
                        color = ProWhite, fontSize = 24.sp
                    )
                    if (report != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Metric("ВЫСОКИЙ", report.highCount, ProRed)
                            Metric("СРЕДНИЙ", report.mediumCount, ProAmber)
                            Metric("НИЗКИЙ", report.lowCount, ProMuted)
                        }
                        Text("Всего записей: ${report.findings.size}", color = ProMuted, fontSize = 12.sp)
                    } else {
                        Text(
                            "PRO SEC проверяет доступные сведения об установленных приложениях, специальных доступах, администраторах и VPN.",
                            color = ProMuted, fontSize = 13.sp
                        )
                    }
                    Button(
                        onClick = { showConsent = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !scanning,
                        colors = ButtonDefaults.buttonColors(containerColor = ProGreen, contentColor = ProBlack),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text(if (scanning) "ПРОВЕРКА..." else "НАЧАТЬ ПОЛНУЮ ПРОВЕРКУ") }
                    Text(scanMessage, color = ProMuted, fontSize = 11.sp)
                    Button(
                        onClick = onRefresh,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ProPanel, contentColor = ProWhite)
                    ) { Text("ОБНОВИТЬ ОТЧЁТ") }
                }
            }
        }
        item { Text("РЕЗУЛЬТАТЫ ПОСЛЕДНЕЙ ПРОВЕРКИ", color = ProMuted, fontSize = 11.sp, letterSpacing = 2.sp) }
        if (report == null) {
            item { AuditModule("—", "Отчётов пока нет", "Запустите проверку, чтобы получить локальный отчёт.") }
        } else {
            items(report.findings) { finding -> FindingCard(finding) }
        }
        item { Text("МОДУЛИ АУДИТА", color = ProMuted, fontSize = 11.sp, letterSpacing = 2.sp) }
        item { AuditModule("01", "Приложения", "Инвентаризация, известные средства удалённого доступа и признаки риска") }
        item { AuditModule("02", "Специальные доступы", "Включённые службы Accessibility и активные администраторы") }
        item { AuditModule("03", "Сетевые подключения", "Обнаружение активного VPN-транспорта") }
        item { AuditModule("04", "История проверок", "Локальное сохранение зашифрованных результатов") }
        item {
            Text(
                "Данные не отправляются на сервер. Автоматическая проверка планируется примерно раз в 24 часа; Android может сдвигать время выполнения. Индикаторы риска требуют ручной проверки и не являются доказательством заражения.",
                color = ProMuted, fontSize = 11.sp
            )
        }
    }

    if (showConsent) {
        AlertDialog(
            onDismissRequest = { showConsent = false },
            containerColor = ProPanel,
            title = { Text("Локальная проверка PRO SEC", color = ProWhite) },
            text = {
                Text(
                    "Для проверки PRO SEC прочитает список видимых установленных приложений, их запрошенные разрешения, включённые Accessibility-сервисы, активных администраторов и состояние VPN. Эти сведения останутся на устройстве и будут сохранены в зашифрованном виде. Ничего не отправляется в сеть. Продолжить?",
                    color = ProMuted
                )
            },
            confirmButton = {
                Button(onClick = { showConsent = false; onScan() }) { Text("СОГЛАСЕН И ПРОВЕРИТЬ") }
            },
            dismissButton = {
                Button(
                    onClick = { showConsent = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ProPanel, contentColor = ProWhite)
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
private fun FindingCard(finding: FindingUi) {
    val color = when (finding.severity) {
        "HIGH" -> ProRed
        "MEDIUM" -> ProAmber
        "LOW" -> ProMuted
        else -> ProGreen
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = ProPanel)
    ) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(finding.severity, color = color, fontSize = 10.sp, letterSpacing = 1.5.sp)
            Text(finding.title, color = ProWhite, fontSize = 14.sp)
            Text(finding.detail, color = ProMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AuditModule(number: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(ProPanel, RoundedCornerShape(16.dp)).padding(15.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(number, color = ProGreen, fontSize = 14.sp)
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = ProWhite, fontSize = 14.sp)
            Text(description, color = ProMuted, fontSize = 11.sp)
        }
    }
}
