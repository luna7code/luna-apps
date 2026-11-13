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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lunacode.mobilesecurity.monitor.DailyAuditWorker
import java.util.concurrent.TimeUnit

private val ProBlack = Color(0xFF080808)
private val ProPanel = Color(0xFF1D1D1D)
private val ProWhite = Color(0xFFFFFFFF)
private val ProMuted = Color(0xFFC2C2C2)
private val ProGreen = Color(0xFF43D17A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ProSecTheme { SecurityHome(onScan = { enqueueScan() }) } }
    }

    private fun enqueueScan() {
        val workManager = WorkManager.getInstance(this)
        val daily = PeriodicWorkRequestBuilder<DailyAuditWorker>(24, TimeUnit.HOURS).build()
        workManager.enqueueUniquePeriodicWork(
            "pro-sec-daily-audit",
            ExistingPeriodicWorkPolicy.KEEP,
            daily
        )
        workManager.enqueue(OneTimeWorkRequestBuilder<DailyAuditWorker>().build())
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
private fun SecurityHome(onScan: () -> Unit) {
    var scanState by remember { mutableStateOf("Проверка ещё не запускалась") }
    var showConsent by remember { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxSize(), color = ProBlack) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("PRO SEC", color = ProWhite, fontSize = 25.sp, letterSpacing = 2.sp)
                    Text("ANDROID SECURITY", color = ProMuted, fontSize = 10.sp, letterSpacing = 3.sp)
                }
                Text("● ЛОКАЛЬНО", color = ProGreen, fontSize = 11.sp)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ProPanel)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("СОСТОЯНИЕ ЗАЩИТЫ", color = ProMuted, fontSize = 11.sp, letterSpacing = 2.sp)
                    Text("Ожидает проверки", color = ProWhite, fontSize = 24.sp)
                    Text(
                        "PRO SEC проверит доступные сведения об установленных приложениях, специальных доступах, администраторах и VPN.",
                        color = ProMuted,
                        fontSize = 13.sp
                    )
                    Button(
                        onClick = { showConsent = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProGreen,
                            contentColor = ProBlack
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("НАЧАТЬ ПОЛНУЮ ПРОВЕРКУ")
                    }
                    Text(scanState, color = ProMuted, fontSize = 11.sp)
                }
            }

            Text("МОДУЛИ АУДИТА", color = ProMuted, fontSize = 11.sp, letterSpacing = 2.sp)
            AuditModule("01", "Приложения", "Инвентаризация, известные средства удалённого доступа и признаки риска")
            AuditModule("02", "Специальные доступы", "Включённые службы Accessibility и активные администраторы")
            AuditModule("03", "Сетевые подключения", "Обнаружение активного VPN-транспорта")
            AuditModule("04", "История проверок", "Локальное сохранение зашифрованных результатов")
            Spacer(Modifier.height(2.dp))
            Text(
                "Данные не отправляются на сервер. Автоматическая проверка планируется примерно раз в 24 часа; Android может сдвигать время выполнения.",
                color = ProMuted,
                fontSize = 11.sp
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
                Button(onClick = {
                    showConsent = false
                    scanState = "Проверка добавлена в очередь. Результат сохранится локально."
                    onScan()
                }) { Text("СОГЛАСЕН И ПРОВЕРИТЬ") }
            },
            dismissButton = {
                Button(
                    onClick = { showConsent = false },
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
private fun AuditModule(number: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth()
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
