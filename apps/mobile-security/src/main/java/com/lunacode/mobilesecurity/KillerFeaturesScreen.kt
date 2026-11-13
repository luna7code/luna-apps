package com.lunacode.mobilesecurity

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.lunacode.mobilesecurity.intelligence.ApkXRay
import com.lunacode.mobilesecurity.intelligence.ApkXRayReport
import com.lunacode.mobilesecurity.intelligence.ControlSurface
import com.lunacode.mobilesecurity.intelligence.ControlSurfaceInspector
import com.lunacode.mobilesecurity.intelligence.DeviceIntegrityInspector
import com.lunacode.mobilesecurity.intelligence.DeviceIntegritySignal
import com.lunacode.mobilesecurity.intelligence.InputTrustInspector
import com.lunacode.mobilesecurity.intelligence.InputTrustReport
import com.lunacode.mobilesecurity.intelligence.LinkInspection
import com.lunacode.mobilesecurity.intelligence.SecurityPostureReport
import com.lunacode.mobilesecurity.intelligence.OfflineLinkInspector
import com.lunacode.mobilesecurity.intelligence.ThreatDatabaseStore
import com.lunacode.mobilesecurity.intelligence.ThreatKeyTrustStore
import com.lunacode.mobilesecurity.intelligence.ThreatPublicKeyCandidate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private val KillerBlack = ProSecPalette.Background
private val KillerPanel = ProSecPalette.Surface
private val KillerWhite = ProSecPalette.White
private val KillerMuted = ProSecPalette.Muted
private val KillerGreen = ProSecPalette.Accent
private val KillerAmber = ProSecPalette.Warning
private val KillerRed = ProSecPalette.Danger

data class KillerChangeUi(
    val kind: String,
    val title: String,
    val detail: String,
    val level: String
)

data class KillerHistoryUi(
    val scannedAt: Long,
    val findingCount: Int,
    val changeCount: Int,
    val integrityStatus: String
)

@Composable
fun KillerFeaturesScreen(
    changes: List<KillerChangeUi>,
    firstBaseline: Boolean,
    hasReport: Boolean,
    history: List<KillerHistoryUi>,
    postureReport: SecurityPostureReport?,
    integrityStatus: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    var controlSurfaces by remember { mutableStateOf<List<ControlSurface>?>(null) }
    var controlLoading by remember { mutableStateOf(false) }
    var controlError by remember { mutableStateOf<String?>(null) }
    var deviceSignals by remember { mutableStateOf<List<DeviceIntegritySignal>?>(null) }
    var inputTrust by remember { mutableStateOf<InputTrustReport?>(null) }
    var deviceTrustError by remember { mutableStateOf<String?>(null) }
    var inputTrustError by remember { mutableStateOf<String?>(null) }

    var linkInput by remember { mutableStateOf("") }
    var linkResult by remember { mutableStateOf<LinkInspection?>(null) }
    var linkError by remember { mutableStateOf<String?>(null) }

    var apkReport by remember { mutableStateOf<ApkXRayReport?>(null) }
    var apkLoading by remember { mutableStateOf(false) }
    var apkError by remember { mutableStateOf<String?>(null) }
    var threatStatus by remember { mutableStateOf("Проверка статуса локальной базы...") }
    var trustedThreatKeyAvailable by remember { mutableStateOf(false) }
    var threatKeyCandidate by remember { mutableStateOf<ThreatPublicKeyCandidate?>(null) }
    var threatKeyIdInput by remember { mutableStateOf("luna-code-primary") }
    var threatKeyFingerprintConfirmed by remember { mutableStateOf(false) }
    var threatKeyImporting by remember { mutableStateOf(false) }
    var replacingThreatKey by remember { mutableStateOf(false) }
    var threatError by remember { mutableStateOf<String?>(null) }
    var threatImporting by remember { mutableStateOf(false) }

    LaunchedEffect(context) {
        val state = withContext(Dispatchers.IO) {
            ThreatDatabaseStore(context).statusDescription() to
                ThreatKeyTrustStore(context).publicKeys().isNotEmpty()
        }
        threatStatus = state.first
        trustedThreatKeyAvailable = state.second
    }

    val threatKeyPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            threatKeyImporting = true
            threatError = null
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching { ThreatKeyTrustStore(context).readCandidate(uri, threatKeyIdInput) }
                }
                result.onSuccess {
                    threatKeyCandidate = it
                    threatKeyFingerprintConfirmed = false
                }.onFailure {
                    threatError = it.message ?: "Не удалось прочитать публичный ключ."
                }
                threatKeyImporting = false
            }
        }
    }

    val threatPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            threatImporting = true
            threatError = null
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching { ThreatDatabaseStore(context).importFrom(uri) }
                }
                result.onSuccess {
                    threatStatus = "Подпись проверена · версия ${it.version} · индикаторов ${it.indicators.size}"
                }.onFailure {
                    threatError = it.message ?: "Не удалось импортировать базу."
                }
                threatImporting = false
            }
        }
    }

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
        modifier = Modifier
            .fillMaxSize()
            .background(KillerBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 18.dp,
            vertical = 18.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("ИНСТРУМЕНТЫ PRO SEC", color = KillerGreen, fontSize = 10.sp, letterSpacing = 2.5.sp)
                Text("Центр анализа защиты", color = KillerWhite, fontSize = 26.sp)
                Text(
                    "Локальные инструменты, которые помогают увидеть не только вирусы, но и риски контроля, подмены и изменений устройства.",
                    color = KillerMuted,
                    fontSize = 13.sp
                )
                Text("Все проверки выполняются на устройстве. Сетевых запросов нет.", color = KillerGreen, fontSize = 11.sp)
            }
        }

        if (postureReport != null) {
            item { SecurityPostureCard(postureReport, compact = false) }
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
                    val clusters = surfaces
                        .filter { it.packageName != "system.network" }
                        .groupBy { it.packageName }
                        .filterValues { entries -> entries.map { it.category }.distinct().size >= 2 }
                    clusters.values.forEach { combined ->
                        val categories = combined.map { it.category }.distinct()
                        FeatureCard(
                            "СОВМЕЩЕНИЕ ПРИВИЛЕГИЙ",
                            combined.first().appLabel,
                            "Одному приложению одновременно выданы механизмы: ${categories.joinToString()}. Это увеличивает потенциальные последствия компрометации, но не является доказательством вредоносного поведения.",
                            KillerRed
                        )
                    }
                    surfaces.forEach { surface ->
                        FeatureCard(
                            surface.category.uppercase(Locale.ROOT),
                            surface.appLabel,
                            "${surface.capability}\n${surface.explanation}",
                            KillerAmber
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { openSystemSettings(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = KillerPanel, contentColor = KillerWhite)
                ) { Text("ACCESSIBILITY") }
                Button(
                    onClick = { openSystemSettings(context, Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = KillerPanel, contentColor = KillerWhite)
                ) { Text("УВЕДОМЛЕНИЯ") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { openSystemSettings(context, Intent(Settings.ACTION_VPN_SETTINGS)) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = KillerPanel, contentColor = KillerWhite)
                ) { Text("VPN") }
                Button(
                    onClick = { openSystemSettings(context, Intent(Settings.ACTION_SECURITY_SETTINGS)) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = KillerPanel, contentColor = KillerWhite)
                ) { Text("АДМИНИСТРАТОРЫ") }
            }
        }

        item {
            FeatureHeader(
                "03 · DEVICE INTEGRITY",
                "Целостность и базовая защита устройства",
                "Проверяет признаки test-keys и наличие защищённой блокировки экрана. Состояние ADB не имитируется: Android скрывает его от сторонних приложений."
            )
            Button(
                onClick = {
                    deviceTrustError = null
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            runCatching { DeviceIntegrityInspector.inspect(context) }
                        }
                        result.onSuccess { deviceSignals = it }
                            .onFailure { deviceTrustError = it.message ?: "Не удалось проверить устройство." }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = KillerGreen, contentColor = KillerBlack)
            ) { Text("ПРОВЕРИТЬ ЦЕЛОСТНОСТЬ УСТРОЙСТВА") }
            deviceTrustError?.let { Text(it, color = KillerRed, fontSize = 12.sp) }
            deviceSignals?.forEach { signal ->
                val tone = when (signal.severity) {
                    "HIGH" -> KillerRed
                    "MEDIUM" -> KillerAmber
                    else -> KillerMuted
                }
                FeatureCard(signal.severity, signal.title, signal.detail, tone)
            }
        }

        item {
            FeatureHeader(
                "04 · INPUT TRUST",
                "Проверка доверия к клавиатуре",
                "Показывает текущую выбранную и все включённые клавиатуры. Клавиатура обрабатывает вводимый текст; её наличие само по себе не означает слежку."
            )
            Button(
                onClick = {
                    inputTrustError = null
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            runCatching { InputTrustInspector.inspect(context) }
                        }
                        result.onSuccess { inputTrust = it }
                            .onFailure { inputTrustError = it.message ?: "Не удалось прочитать список клавиатур." }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = KillerGreen, contentColor = KillerBlack)
            ) { Text("ПРОВЕРИТЬ КЛАВИАТУРЫ") }
            inputTrustError?.let { Text(it, color = KillerRed, fontSize = 12.sp) }
            inputTrust?.let { trust ->
                val active = trust.methods.firstOrNull { it.active }
                InfoCard(
                    "Текущая клавиатура",
                    active?.let { "${it.label}\n${it.packageName}\n${if (it.systemApp) "Системное приложение" else "Стороннее приложение"}" }
                        ?: "Android не сообщил активную клавиатуру."
                )
                Text("ВКЛЮЧЕНО КЛАВИАТУР: ${trust.methods.size}", color = KillerAmber, fontSize = 11.sp)
                trust.methods.forEach { method ->
                    FeatureCard(
                        if (method.active) "ТЕКУЩАЯ КЛАВИАТУРА" else if (method.systemApp) "СИСТЕМНАЯ · ВКЛЮЧЕНА" else "СТОРОННЯЯ · ВКЛЮЧЕНА",
                        method.label,
                        method.packageName,
                        if (method.active && !method.systemApp) KillerAmber else KillerMuted
                    )
                }
            }
            Button(
                onClick = { openSystemSettings(context, Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = KillerPanel, contentColor = KillerWhite)
            ) { Text("ОТКРЫТЬ НАСТРОЙКИ КЛАВИАТУР") }
        }

        item {
            FeatureHeader(
                "05 · CHANGE SENTINEL",
                "Память изменений устройства",
                "Сравнивает текущий снимок с предыдущим: новые и удалённые приложения, обновления и смену сертификата подписи. Данные остаются зашифрованными в локальной истории."
            )
            when {
                !hasReport -> InfoCard("Нет базового снимка", "Сначала запустите проверку на вкладке «Обзор». Первый отчёт создаст исходную точку для сравнения.")
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
                "06 · TRUST TIMELINE",
                "Хронология состояния",
                "Последние локальные снимки помогают сопоставить, когда появились индикаторы и когда менялся состав приложений."
            )
            if (history.isEmpty()) {
                InfoCard("История пока пуста", "Запустите первый аудит. Каждый последующий отчёт сохранится локально; хранится не более 90 последних записей.")
            } else {
                history.forEach { item ->
                    val sealLabel = when (item.integrityStatus) {
                        "VERIFIED" -> "HMAC VERIFIED"
                        "FAILED" -> "HMAC FAILED"
                        "LEGACY" -> "LEGACY · БЕЗ ПЕЧАТИ"
                        "UNSEALED" -> "UNSEALED"
                        else -> "UNVERIFIABLE"
                    }
                    FeatureCard(
                        sealLabel,
                        java.text.DateFormat.getDateTimeInstance().format(java.util.Date(item.scannedAt)),
                        "Сигналов аудита: ${item.findingCount} · Изменений с прошлого снимка: ${item.changeCount}",
                        when (item.integrityStatus) {
                            "VERIFIED" -> KillerGreen
                            "FAILED" -> KillerRed
                            else -> KillerMuted
                        }
                    )
                }
            }
        }

        item {
            FeatureHeader(
                "07 · FORENSIC SEAL",
                "Криптографическая целостность истории",
                "Каждый новый отчёт подписывается HMAC-ключом Android Keystore и связывается с предыдущим. Это помогает обнаружить изменение содержимого или нарушение порядка записей в локальной истории."
            )
            EvidenceSealCard(integrityStatus)
        }

        item {
            FeatureHeader(
                "08 · APK X-RAY",
                "Рентген APK до установки",
                "Выберите APK-файл. PRO SEC прочитает манифест, разрешения, экспортируемые компоненты, debug/backup/cleartext-флаги, SHA-256, отпечатки сертификатов и ограниченный набор DEX API-сигналов. Файл не устанавливается."
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
                "09 · LINK MIRROR",
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
                "10 · PURA GUARD",
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
            FeatureHeader(
                "11 · ОФЛАЙН-БАЗА ИНДИКАТОРОВ",
                "Подписанная база индикаторов",
                "Обновления импортируются вручную из локального файла. PRO SEC проверяет подпись RSA, издателя, структуру записей и номер версии. Сетевых запросов нет."
            )
            InfoCard("Статус доверенной базы", threatStatus)
            if (!trustedThreatKeyAvailable || replacingThreatKey) {
                OutlinedTextField(
                    value = threatKeyIdInput,
                    onValueChange = {
                        threatKeyIdInput = it
                        threatKeyCandidate = null
                        threatKeyFingerprintConfirmed = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("ID издателя в подписанной базе") },
                    placeholder = { Text("luna-code-primary") },
                    singleLine = true
                )
                Button(
                    onClick = { threatKeyPicker.launch(arrayOf("application/x-pem-file", "text/plain", "application/octet-stream")) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !threatKeyImporting,
                    colors = ButtonDefaults.buttonColors(containerColor = KillerPanel, contentColor = KillerWhite)
                ) { Text(if (threatKeyImporting) "ЧТЕНИЕ КЛЮЧА..." else "ВЫБРАТЬ PEM-ПУБЛИЧНЫЙ КЛЮЧ") }
                threatKeyCandidate?.let { candidate ->
                    InfoCard("SHA-256 ОТПЕЧАТОК КАНДИДАТА", candidate.sha256Fingerprint)
                    Text(
                        "Сверьте отпечаток с опубликованным издателем значением через независимый доверенный канал. Не подтверждайте ключ, полученный только вместе с файлом базы.",
                        color = KillerMuted,
                        fontSize = 11.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = threatKeyFingerprintConfirmed,
                            onCheckedChange = { threatKeyFingerprintConfirmed = it }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Я независимо сверил SHA-256 отпечаток", color = KillerWhite, fontSize = 12.sp)
                            Text("Закрепление ключа разрешается только после этого подтверждения.", color = KillerMuted, fontSize = 10.sp)
                        }
                    }
                    Button(
                        onClick = {
                            val selected = candidate
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    runCatching {
                                        ThreatKeyTrustStore(context).pin(
                                            selected,
                                            threatKeyFingerprintConfirmed
                                        )
                                    }
                                }
                                if (result.isSuccess) {
                                    trustedThreatKeyAvailable = true
                                    replacingThreatKey = false
                                    threatKeyCandidate = null
                                    threatKeyFingerprintConfirmed = false
                                    threatStatus = withContext(Dispatchers.IO) {
                                        ThreatDatabaseStore(context).statusDescription()
                                    }
                                } else {
                                    threatError = result.exceptionOrNull()?.message
                                        ?: "Не удалось закрепить публичный ключ."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = threatKeyFingerprintConfirmed,
                        colors = ButtonDefaults.buttonColors(containerColor = KillerGreen, contentColor = KillerBlack)
                    ) { Text("ЗАКРЕПИТЬ ДОВЕРЕННЫЙ КЛЮЧ") }
                }
            } else {
                InfoCard(
                    "ЗАКРЕПЛЁННЫЙ ПУБЛИЧНЫЙ КЛЮЧ",
                    ThreatKeyTrustStore(context).trustedKeySummary()
                )
                Button(
                    onClick = {
                        replacingThreatKey = true
                        threatKeyCandidate = null
                        threatKeyFingerprintConfirmed = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = KillerPanel, contentColor = KillerWhite)
                ) { Text("ИЗМЕНИТЬ КЛЮЧ ИЗДАТЕЛЯ") }
            }
            Button(
                onClick = { threatPicker.launch(arrayOf("text/plain", "application/octet-stream")) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !threatImporting && trustedThreatKeyAvailable,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KillerGreen, contentColor = KillerBlack,
                    disabledContainerColor = KillerPanel, disabledContentColor = KillerMuted
                )
            ) { Text(if (threatImporting) "ПРОВЕРКА ПОДПИСИ..." else "ИМПОРТИРОВАТЬ ПОДПИСАННУЮ БАЗУ") }
            threatError?.let { Text(it, color = KillerRed, fontSize = 12.sp) }
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
        border = BorderStroke(1.dp, ProSecPalette.Border),
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
        border = BorderStroke(1.dp, ProSecPalette.Border),
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
            "Версия: ${report.versionName} (${report.versionCode})\nTarget SDK: ${report.targetSdk} · Min SDK: ${report.minSdk}\nРазмер: ${report.fileSizeBytes / 1024} KB\nЭкспортируемых компонентов: ${report.exportedComponents} · без permission: ${report.exportedWithoutPermission}\nРазрешений в манифесте: ${report.requestedPermissions.size}"
        )
        InfoCard(
            "Проверка цепочки сертификатов: ${report.signerRotationResult}",
            "${report.signerRotationExplanation}\n" + when (report.installedSignerMatch) {
                true -> "Пакет уже установлен (${report.installedVersionName.orEmpty()}); совпадение или Android-подтверждённая ротация."
                false -> "Пакет уже установлен (${report.installedVersionName.orEmpty()}); выбранный ключ не подтверждён как текущий. Проверьте APK до установки."
                null -> "Установленная версия не найдена или Android не предоставил достаточно данных."
            }
        )
        InfoCard("SHA-256 APK", report.apkSha256)
        Button(
            onClick = { onCopy(report.apkSha256) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = KillerPanel, contentColor = KillerWhite)
        ) { Text("КОПИРОВАТЬ SHA-256") }
        InfoCard(
            "Текущие сертификаты",
            if (report.signerSha256.isEmpty()) "Сертификат подписи не прочитан." else report.signerSha256.joinToString("\n")
        )
        InfoCard(
            "История сертификатов (старый → текущий)",
            if (report.signerHistorySha256.isEmpty()) "История подписи недоступна." else report.signerHistorySha256.joinToString("\n")
        )
        if (report.exportedComponentDetails.isNotEmpty()) {
            InfoCard(
                "Экспортируемые компоненты",
                report.exportedComponentDetails.joinToString("\n") {
                    "${it.type}: ${it.name}\nЗаданное permission: ${it.permission.ifBlank { "не указано" }}"
                } + if (report.exportedComponents > report.exportedComponentDetails.size) {
                    "\nПоказаны первые ${report.exportedComponentDetails.size} компонентов."
                } else ""
            )
        }
        InfoCard(
            "DEX-анализ возможностей",
            "Просмотрено DEX-файлов: ${report.dexFilesScanned} · данных: ${report.dexBytesScanned / 1024} KB · время: ${report.dexScanDurationMs} мс" +
                if (report.dexScanTruncated) "\n${report.dexLimitReason ?: "Анализ ограничен лимитом размера или числа файлов."}" else ""
        )
        if (report.dexSignals.isEmpty()) {
            InfoCard(
                "DEX API-индикаторы не найдены",
                "В просмотренных DEX-данных не обнаружены проверяемые API-ссылки. Это не доказывает отсутствие возможностей: код может быть обфусцирован, упакован или загружаться динамически."
            )
        } else {
            report.dexSignals.forEach { signal ->
                FeatureCard(
                    "ССЫЛКА НА API",
                    signal,
                    "В DEX обнаружена ссылка на эту системную возможность. Легитимные приложения тоже могут её использовать; нужен контекст кода и назначение приложения.",
                    KillerAmber
                )
            }
        }
        if (report.threatIndicators.isNotEmpty()) {
            report.threatIndicators.forEach { indicator ->
                FeatureCard(
                    "СОВПАДЕНИЕ ПОДПИСАННОЙ БАЗЫ · ${indicator.confidence}/100",
                    indicator.title,
                    "${indicator.detail}\nID: ${indicator.id}. Совпадение индикатора само по себе не доказывает вредоносное поведение.",
                    when (indicator.severity) {
                        "HIGH" -> KillerRed
                        "MEDIUM" -> KillerAmber
                        else -> KillerMuted
                    }
                )
            }
        }
        if (report.risks.isEmpty()) {
            InfoCard("Явные статические индикаторы не найдены", "Это не подтверждает безопасность APK. Анализ ограничен метаданными, сертификатами и статическими DEX-признаками.")
        } else {
            report.risks.forEach { risk ->
                val color = when (risk.level) {
                    "HIGH" -> KillerRed
                    "MEDIUM" -> KillerAmber
                    "INFO" -> KillerGreen
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
