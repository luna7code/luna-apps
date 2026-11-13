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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.lunacode.mobilesecurity.monitor.DailyAuditWorker

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
        WorkManager.getInstance(this).enqueue(
            OneTimeWorkRequestBuilder<DailyAuditWorker>().build()
        )
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
                        onClick = {
                            scanState = "Проверка добавлена в очередь. Результат сохранится локально."
                            onScan()
                        },
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
