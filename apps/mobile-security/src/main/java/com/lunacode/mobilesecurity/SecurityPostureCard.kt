package com.lunacode.mobilesecurity

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lunacode.mobilesecurity.intelligence.SecurityPostureReport

private val PosturePanel = ProSecPalette.Surface
private val PostureWhite = ProSecPalette.White
private val PostureMuted = ProSecPalette.Muted
private val PostureGreen = ProSecPalette.Positive
private val PostureAmber = ProSecPalette.Warning
private val PostureRed = ProSecPalette.Danger
private val PostureBlack = ProSecPalette.Background

@Composable
fun SecurityPostureCard(
    report: SecurityPostureReport,
    compact: Boolean = false
) {
    val context = LocalContext.current
    val accent = when {
        report.score >= 90 -> PostureGreen
        report.score >= 75 -> PostureAmber
        report.score >= 55 -> PostureAmber
        else -> PostureRed
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ProSecPalette.Border),
        colors = CardDefaults.cardColors(containerColor = PosturePanel)
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("PRO SEC · EXPOSURE INDEX", color = PostureMuted, fontSize = 10.sp, letterSpacing = 2.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "${report.score}",
                    color = accent,
                    fontSize = if (compact) 34.sp else 42.sp
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(report.grade, color = PostureWhite, fontSize = 16.sp)
                    Text(
                        "HIGH ${report.highCount}  ·  MED ${report.mediumCount}  ·  LOW ${report.lowCount}",
                        color = PostureMuted,
                        fontSize = 10.sp
                    )
                }
            }
            Text(
                report.explanation,
                color = PostureMuted,
                fontSize = 11.sp
            )
            Text(
                "Действие считается проверенным только после повторного аудита. Открытие настроек само по себе не означает, что доступ был отключён.",
                color = PostureMuted,
                fontSize = 10.sp
            )
            if (report.recommendations.isNotEmpty()) {
                Text("ПРИОРИТЕТНЫЕ ДЕЙСТВИЯ", color = PostureWhite, fontSize = 10.sp, letterSpacing = 1.5.sp)
                report.recommendations.take(if (compact) 2 else 5).forEach { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PostureBlack, RoundedCornerShape(13.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(item.priority, color = if (item.priority == "HIGH") PostureRed else PostureAmber, fontSize = 9.sp)
                        Text(item.title, color = PostureWhite, fontSize = 13.sp)
                        Text(item.detail, color = PostureMuted, fontSize = 11.sp)
                        Button(
                            onClick = {
                                val packageUri = item.packageName?.let { Uri.fromParts("package", it, null) }
                                val intent = when (item.settingsAction) {
                                    "overlay" -> Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, packageUri)
                                    "usage" -> Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                    "write-settings" -> Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, packageUri)
                                    "install-packages" -> Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri)
                                    "manage-storage" -> if (android.os.Build.VERSION.SDK_INT >= 30) {
                                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, packageUri)
                                    } else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
                                    else -> if (packageUri != null) {
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
                                    } else Intent(Settings.ACTION_SECURITY_SETTINGS)
                                }
                                runCatching {
                                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                }.onFailure {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Settings.ACTION_SECURITY_SETTINGS)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PosturePanel,
                                contentColor = PostureWhite
                            )
                        ) {
                            Text(if (item.packageName != null) "ОТКРЫТЬ ПРИЛОЖЕНИЕ" else "ОТКРЫТЬ БЕЗОПАСНОСТЬ")
                        }
                    }
                }
            } else {
                Text(
                    "Для проверенных PRO SEC областей действий не требуется. Это не означает, что устройство полностью защищено.",
                    color = PostureMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}


@Composable
fun EvidenceSealCard(
    status: String,
    compact: Boolean = false
) {
    val (label, detail, accent) = when (status) {
        "VERIFIED" -> Triple(
            "HMAC ПОДТВЕРЖДЁН",
            "Подпись текущего отчёта проверена. Связи между доступными соседними записями истории совпадают.",
            PostureGreen
        )
        "FAILED" -> Triple(
            "ПРОВЕРКА НЕ ПРОЙДЕНА",
            "Подпись или связь между записями не совпала. Возможны изменение истории или смена/потеря ключа устройства. Это не позволяет автоматически утверждать, что данные подделаны.",
            PostureRed
        )
        "LEGACY" -> Triple(
            "СТАРЫЙ ОТЧЁТ",
            "Отчёт создан до внедрения Forensic Seal и не содержит HMAC-подписи.",
            PostureMuted
        )
        "UNSEALED" -> Triple(
            "НЕТ КРИПТОГРАФИЧЕСКОЙ ПЕЧАТИ",
            "Запись содержит формат цепочки, но не содержит корректной HMAC-подписи.",
            PostureAmber
        )
        else -> Triple(
            "НЕ УДАЛОСЬ ПРОВЕРИТЬ",
            "Android Keystore не смог подтвердить HMAC. Проверьте состояние локального ключа и не считайте запись проверенной.",
            PostureAmber
        )
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ProSecPalette.Border),
        colors = CardDefaults.cardColors(containerColor = PosturePanel)
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 14.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("FORENSIC SEAL", color = PostureMuted, fontSize = 10.sp, letterSpacing = 2.sp)
            Text(label, color = accent, fontSize = 12.sp, letterSpacing = 1.sp)
            Text(detail, color = PostureMuted, fontSize = 11.sp)
        }
    }
}
