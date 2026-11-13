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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LunaBlack = Color(0xFF080808)
private val LunaPanel = Color(0xFF1D1D1D)
private val LunaWhite = Color(0xFFFFFFFF)
private val LunaMuted = Color(0xFFC2C2C2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LunaSecurityTheme { SecurityHome() } }
    }
}

@Composable
private fun LunaSecurityTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(
        background = LunaBlack,
        surface = LunaPanel,
        primary = LunaWhite,
        onPrimary = LunaBlack,
        onBackground = LunaWhite,
        onSurface = LunaWhite
    ), content = content)
}

@Composable
private fun SecurityHome() {
    Surface(modifier = Modifier.fillMaxSize(), color = LunaBlack) {
        Column(
            modifier = Modifier.fillMaxSize().padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("LUNA", color = LunaMuted, fontSize = 13.sp, letterSpacing = 5.sp)
            Text("MOBILE SECURITY", color = LunaWhite, fontSize = 27.sp)
            Text("Защита устройства под контролем", color = LunaMuted)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = LunaPanel)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Состояние устройства", color = LunaMuted)
                    Text("Ожидает первой проверки", color = LunaWhite, fontSize = 20.sp)
                    Text("Приложение проверяет доступные Android-сигналы. Проверки, недоступные без root или системных прав, будут отмечены отдельно.", color = LunaMuted)
                    Button(onClick = { /* Scan flow is added in the scanner milestone. */ }) {
                        Text("Начать проверку")
                    }
                }
            }
            Text("МОДУЛИ БЕЗОПАСНОСТИ", color = LunaMuted, fontSize = 12.sp, letterSpacing = 2.sp)
            ModuleRow("Анализ приложений", "Список, источники установки и признаки риска")
            ModuleRow("Контроль разрешений", "Особые доступы, администраторы и Accessibility")
            ModuleRow("VPN и сеть", "Активные VPN-подключения и сетевые индикаторы")
            ModuleRow("История изменений", "Локальное сравнение результатов проверок")
            Spacer(Modifier.height(4.dp))
            Text("Результаты хранятся на устройстве. Облачная отправка не используется.", color = LunaMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ModuleRow(title: String, description: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f).background(LunaPanel, RoundedCornerShape(14.dp)).padding(15.dp)) {
            Text(title, color = LunaWhite)
            Text(description, color = LunaMuted, fontSize = 12.sp)
        }
    }
}
