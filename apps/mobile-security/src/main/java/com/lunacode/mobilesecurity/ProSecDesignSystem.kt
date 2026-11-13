package com.lunacode.mobilesecurity

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object ProSecPalette {
    val Background = Color(0xFF080808)
    val Surface = Color(0xFF141414)
    val Elevated = Color(0xFF1B1B1B)
    val Border = Color(0xFF303030)
    val White = Color(0xFFF7F7F7)
    val Muted = Color(0xFFA5A5A5)
    val Accent = Color(0xFFFFFFFF)
    val Positive = Color(0xFFB8C7BE)
    val Warning = Color(0xFFD0B27A)
    val Danger = Color(0xFFD27E7E)
}

@Composable
fun ProSecMaterialTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = ProSecPalette.Accent,
            onPrimary = ProSecPalette.Background,
            secondary = ProSecPalette.Positive,
            onSecondary = ProSecPalette.Background,
            background = ProSecPalette.Background,
            onBackground = ProSecPalette.White,
            surface = ProSecPalette.Surface,
            onSurface = ProSecPalette.White,
            surfaceVariant = ProSecPalette.Elevated,
            onSurfaceVariant = ProSecPalette.Muted,
            outline = ProSecPalette.Border,
            error = ProSecPalette.Danger
        ),
        content = content
    )
}
