package com.lunacode.mobilesecurity

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
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

private val PosturePanel = Color(0xFF1D1D1D)
private val PostureWhite = Color(0xFFFFFFFF)
private val PostureMuted = Color(0xFFC2C2C2)
private val PostureGreen = Color(0xFF43D17A)
private val PostureAmber = Color(0xFFFFC15A)
private val PostureRed = Color(0xFFFF6B6B)
private val PostureBlack = Color(0xFF080808)

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
                                val intent = if (item.packageName != null) {
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", item.packageName, null)
                                    )
                                } else {
                                    Intent(Settings.ACTION_SECURITY_SETTINGS)
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
