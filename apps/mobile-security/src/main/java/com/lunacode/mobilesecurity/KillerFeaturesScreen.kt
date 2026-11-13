package com.lunacode.mobilesecurity

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.lunacode.mobilesecurity.intelligence.ApkRisk
import com.lunacode.mobilesecurity.intelligence.ApkXRay
import com.lunacode.mobilesecurity.intelligence.ApkXRayReport
import com.lunacode.mobilesecurity.intelligence.ControlSurface
import com.lunacode.mobilesecurity.intelligence.ControlSurfaceInspector
import com.lunacode.mobilesecurity.intelligence.LinkInspection
import com.lunacode.mobilesecurity.intelligence.OfflineLinkInspector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private val KillerBlack = Color(0xFF080808)
private val KillerPanel = Color(0xFF1D1D1D)
private val KillerWhite = Color(0xFFFFFFFF)
private val KillerMuted = Color(0xFFC2C2C2)
private val KillerGreen = Color(0xFF43D17A)
private val KillerAmber = Color(0xFFFFC15A)
private val KillerRed = Color(0xFFFF6B6B)

data class KillerChangeUi(
    val kind: String,
    val title: String,
    val detail: String,
    val level: String
)

@Composable
fun KillerFeaturesScreen(
    changes: List<KillerChangeUi>,
    firstBaseline: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    var controlSurfaces by remember { mutableStateOf<List<ControlSurface>?>(null) }
    var controlLoading by remember { mutableStateOf(false) }
    var controlError by remember { mutableStateOf<String?>(null) }

    var linkInput by remember { mutableStateOf("") }
    var linkResult by remember { mutableStateOf<LinkInspection?>(null) }
    var linkError by remember { mutableStateOf<String?>(null) }

    var apkReport by remember { mutableStateOf<ApkXRayReport?>(null) }
    var apkLoading by remember { mutableStateOf(false) }
    var apkError by remember { mutableStateOf<String?>(null) }

    val apkPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            apkLoading = true
            apkError = null
            apkReport = null
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching { ApkXRay.inspect(context, uri) }
                }
                result.onSuccess { apkReport = it }
                    .onFailure { apkError = it.message ?: "Не удалось проанализировать APK." }
                apkLoading = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(KillerBlack),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 18.dp,
            vertical = 18.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("KILLER FEATURES", color = KillerGreen, fontSize = 10.sp, letterSpacing = 3.sp)
                Text("Инструменты PRO SEC", color = KillerWhite, fontSize = 26.sp)
                Text(
                    "Локальные инструменты, которые помогают увидеть не только вирусы, но и риски контроля, подмены и изменений устройства.",
                    color = KillerMuted,
                    fontSize = 13.sp
                )
                Text("Все проверки выполняются на устройстве. Сетевых запросов нет.", color = KillerGreen, fontSize = 11.sp)
            }
        }

        item {
            FeatureHeader(
                "01 · CONTROL MAP",
                "Карта доступа к устройству",
                "Показывает активные системные механизмы, через которые приложения могут читать уведомления, взаимодействовать с экраном, администрировать устройство или направлять сеть."
            )
            Button(
                onClick = {
                    controlLoading = true
                    controlError = null
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            runCatching { ControlSurfaceInspector.inspect(context) }
                        }
                        result.onSuccess { controlSurfaces = it }
                            .onFailure { controlError = it.message ?: "Не удалось получить системные доступы." }
                        controlLoading = false
                    }
                },
                enabled = !controlLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = KillerGreen, contentColor = KillerBlack)
            ) { Text(if (controlLoading) "АНАЛИЗ..." else "ПОСТРОИТЬ КАРТУ ДОСТУПА") }
            controlError?.let { Text(it, color = KillerRed, fontSize = 12.sp) }
            controlSurfaces?.let { surfaces ->
                if (surfaces.isEmpty()) {
                    InfoCard("Активные поверхности контроля не обнаружены", "Сейчас Android не сообщил включённых Accessibility/notification listener/admin/VPN механизмов.")
                } else {
                    Text("НАЙДЕНО МЕХАНИЗМОВ: ${surfaces.size}", color = KillerAmber, fontSize = 11.sp)
                    items(surfaces) { surface ->
                        FeatureCard(
                            surface.category.uppercase(Locale.ROOT),
                            surface.appLabel,
                            "${surface.capability}\n${surface.explanation}",
                            KillerAmber
                        )
                    }
                }
            }
        }

        item {
            FeatureHeader(
                "02 · CHANGE SENTINEL",
                "Память изменений устройства",
                "Сравнивает текущий снимок с предыдущим: новые и удалённые приложения, обновления и смену сертификата подписи. Данные остаются зашифрованными в локальной истории."
            )
            when {
                firstBaseline -> InfoCard("Базовый снимок создан", "Первый отчёт становится точкой отсчёта. После следующего сканирования появятся реальные различия.")
                changes.isEmpty() -> InfoCard("Изменения не обнаружены", "В последнем сравнении нет отличий в составе приложений или новых индикаторов.")
                else -> {
                    Text("ИЗМЕНЕНИЙ: ${changes.size}", color = KillerAmber, fontSize = 11.sp)
                    changes.forEach { change ->
                        val tone = when (change.level) {
                            "HIGH" -> KillerRed
                            "MEDIUM" -> KillerAmber
                            else -> KillerMuted
                        }
                        FeatureCard(change.level, change.title, change.detail, tone)
                    }
                }
            }
            Text(
                "Примечание: новая установка сама по себе не является заражением. Смена сертификата может быть легитимной ротацией ключа издателя.",
                color = KillerMuted,
                fontSize = 11.sp
            )
        }

        item {
            FeatureHeader(
                "03 · APK X-RAY",
                "Рентген APK до установки",
                "Выберите APK-файл. PRO SEC прочитает манифест, разрешения, экспортируемые компоненты, debug/backup/cleartext-флаги, SHA-256 файла и отпечатки сертификатов. Файл не устанавливается."
            )
            Button(
                onClick = { apkPicker.launch(arrayOf("application/vnd.android.package-archive", "application/octet-stream")) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !apkLoading,
                colors = ButtonDefaults.buttonColors(containerColor = KillerGreen, contentColor = KillerBlack)
            ) { Text(if (apkLoading) "АНАЛИЗ APK..." else "ВЫБРАТЬ APK ДЛЯ АНАЛИЗА") }
            apkError?.let { Text(it, color = KillerRed, fontSize = 12.sp) }
            apkReport?.let { ApkReportCard(it, onCopy = { value -> clipboard.setText(AnnotatedString(value)) }) }
        }

        item {
            FeatureHeader(
                "04 · LINK MIRROR",
                "Проверка ссылки без интернета",
                "Анализирует структуру адреса: HTTP, маскировку через user-info, Punycode, короткие ссылки, IP вместо домена и нестандартные порты. Не открывает ссылку."
            )
            OutlinedTextField(
                value = linkInput,
                onValueChange = { linkInput = it; linkResult = null; linkError = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Вставьте подозрительную ссылку") },
                placeholder = { Text("https://example.com/path") },
                singleLine = false,
                minLines = 2,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )
            Button(
                onClick = {
                    val result = runCatching { OfflineLinkInspector.inspect(linkInput) }
                    result.onSuccess { linkResult = it; linkError = null }
                        .onFailure { linkError = it.message ?: "Ошибка анализа ссылки." }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = KillerGreen, contentColor = KillerBlack)
            ) { Text("ПРОВЕРИТЬ СТРУКТУРУ ССЫЛКИ") }
            linkError?.let { Text(it, color = KillerRed, fontSize = 12.sp) }
            linkResult?.let { result ->
                FeatureCard(
                    if (result.indicators.isEmpty()) "СТРУКТУРНЫЕ ИНДИКАТОРЫ НЕ НАЙДЕНЫ" else "НАЙДЕНО: ${result.indicators.size}",
                    result.host,
                    (if (result.indicators.isEmpty()) "В доступной структуре не найдено проверяемых признаков." else result.indicators.joinToString("\n• ", prefix = "• ")) +
                        "\n\n${result.note}",
                    if (result.indicators.isEmpty()) KillerGreen else KillerAmber
                )
            }
        }

        item {
            FeatureHeader(
                "05 · PURA GUARD",
                "Режим устойчивой защиты Huawei",
                "Профиль настройки фоновой работы для Pura 70. Он не обходит ограничения батареи и не включает скрытые разрешения — только открывает системные экраны и даёт проверяемые шаги."
            )
            InfoCard(
                "Рекомендуемая настройка",
                "1. Настройки → Приложения → Запуск приложений → PRO SEC.\n2. Отключите «Управлять автоматически».\n3. Разрешите автозапуск, вторичный запуск и работу в фоне.\n4. В оптимизации батареи разрешите PRO SEC работать без оптимизации.\n5. Закрепите PRO SEC в списке недавних приложений."
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        openSystemSettings(
                            context,
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = KillerGreen, contentColor = KillerBlack)
                ) { Text("НАСТРОЙКИ PRO SEC") }
                Button(
                    onClick = {
                        openSystemSettings(context, Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = KillerPanel, contentColor = KillerWhite)
                ) { Text("БАТАРЕЯ") }
            }
            Text(
                "Даже после настройки EMUI может задержать фоновые задачи. PRO SEC показывает фактическое состояние WorkManager и не обещает точный запуск по часам.",
                color = KillerMuted,
                fontSize = 11.sp
            )
        }

        item {
            Text(
                "ГРАНИЦЫ ВОЗМОЖНОСТЕЙ",
                color = KillerMuted,
                fontSize = 10.sp,
                letterSpacing = 2.sp
            )
            Text(
                "APK X-RAY — статический анализ манифеста и сертификатов, а не запуск в песочнице. LINK MIRROR — эвристика URL без проверки репутации домена. CONTROL MAP показывает доступные Android механизмы, а не доказывает наличие шпионского ПО.",
                color = KillerMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun FeatureHeader(code: String, title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(code, color = KillerGreen, fontSize = 10.sp, letterSpacing = 2.sp)
        Text(title, color = KillerWhite, fontSize = 18.sp)
        Text(description, color = KillerMuted, fontSize = 12.sp)
    }
}

@Composable
private fun FeatureCard(label: String, title: String, detail: String, accent: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KillerPanel)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(label, color = accent, fontSize = 9.sp, letterSpacing = 1.5.sp)
            Text(title, color = KillerWhite, fontSize = 14.sp)
            Text(detail, color = KillerMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun InfoCard(title: String, detail: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = KillerPanel)
    ) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = KillerGreen, fontSize = 13.sp)
            Text(detail, color = KillerMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ApkReportCard(report: ApkXRayReport, onCopy: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoCard(
            report.packageName,
            "Версия: ${report.versionName} (${report.versionCode})\nTarget SDK: ${report.targetSdk} · Min SDK: ${report.minSdk}\nРазмер: ${report.fileSizeBytes / 1024} KB\nЭкспортируемых компонентов: ${report.exportedComponents}\nРазрешений в манифесте: ${report.requestedPermissions.size}"
        )
        InfoCard("SHA-256 APK", report.apkSha256)
        Button(
            onClick = { onCopy(report.apkSha256) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = KillerPanel, contentColor = KillerWhite)
        ) { Text("КОПИРОВАТЬ SHA-256") }
        InfoCard(
            "Отпечатки сертификатов",
            if (report.signerSha256.isEmpty()) "Сертификат подписи не прочитан." else report.signerSha256.joinToString("\n")
        )
        if (report.risks.isEmpty()) {
            InfoCard("Явные статические индикаторы не найдены", "Это не подтверждает безопасность APK. Анализ ограничен метаданными и манифестом.")
        } else {
            report.risks.forEach { risk ->
                val color = when (risk.level) {
                    "HIGH" -> KillerRed
                    "MEDIUM" -> KillerAmber
                    else -> KillerMuted
                }
                FeatureCard(risk.level, risk.title, risk.detail, color)
            }
        }
    }
}

private fun openSystemSettings(context: android.content.Context, intent: Intent) {
    runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        .onFailure {
            runCatching {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
}
