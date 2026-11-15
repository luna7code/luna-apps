package com.luna.secretnotes.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

/**
 * SECRET NOTES — Plum Shadow.
 *
 * Semantic colors are intentionally explicit so cards and custom surfaces do not
 * depend on Material defaults. Hex values are part of the approved design system.
 */
object PlumShadow {
    // Dark surfaces
    val DarkBackground = Color(0xFF100D12)
    val DarkSurface = Color(0xFF19141D)
    val DarkSurfaceElevated = Color(0xFF211A26)
    val DarkCard = Color(0xFF261D2B)
    val DarkCardPressed = Color(0xFF302438)

    // Dark content
    val DarkTextPrimary = Color(0xFFF7F1F8)
    val DarkTextSecondary = Color(0xFFC8BACD)
    val DarkTextMuted = Color(0xFF9D8FA3)
    val DarkBorder = Color(0xFF514456)
    val DarkBorderSubtle = Color(0xFF392F3D)

    // Dark accent and status
    val DarkAccent = Color(0xFFD6A8E2)
    val DarkOnAccent = Color(0xFF2B1731)
    val DarkAccentContainer = Color(0xFF4A3152)
    val DarkOnAccentContainer = Color(0xFFF5DDF9)
    val DarkError = Color(0xFFFFB4AB)
    val DarkOnError = Color(0xFF690005)
    val DarkErrorContainer = Color(0xFF93000A)
    val DarkSuccess = Color(0xFFA6D7B5)
    val DarkOnSuccess = Color(0xFF103822)
    val DarkSuccessContainer = Color(0xFF1D3A2B)

    // Light surfaces
    val LightBackground = Color(0xFFFAF7FB)
    val LightSurface = Color(0xFFFFFBFF)
    val LightSurfaceElevated = Color(0xFFF7F0F8)
    val LightCard = Color(0xFFF2EAF5)
    val LightCardPressed = Color(0xFFE9DDEC)

    // Light content
    val LightTextPrimary = Color(0xFF211825)
    val LightTextSecondary = Color(0xFF514657)
    val LightTextMuted = Color(0xFF6D6072)
    val LightBorder = Color(0xFF817487)
    val LightBorderSubtle = Color(0xFFD8CBDC)

    // Light accent and status
    val LightAccent = Color(0xFF70427C)
    val LightOnAccent = Color(0xFFFFFFFF)
    val LightAccentContainer = Color(0xFFF2DDF7)
    val LightOnAccentContainer = Color(0xFF2B1233)
    val LightError = Color(0xFFBA1A1A)
    val LightOnError = Color(0xFFFFFFFF)
    val LightErrorContainer = Color(0xFFFFDAD6)
    val LightSuccess = Color(0xFF216A43)
    val LightOnSuccess = Color(0xFFFFFFFF)
    val LightSuccessContainer = Color(0xFFD2F0DC)

    val DarkColorScheme: ColorScheme = darkColorScheme(
        primary = DarkAccent,
        onPrimary = DarkOnAccent,
        primaryContainer = DarkAccentContainer,
        onPrimaryContainer = DarkOnAccentContainer,
        secondary = DarkTextSecondary,
        onSecondary = DarkBackground,
        secondaryContainer = DarkCard,
        onSecondaryContainer = DarkTextPrimary,
        background = DarkBackground,
        onBackground = DarkTextPrimary,
        surface = DarkSurface,
        onSurface = DarkTextPrimary,
        surfaceVariant = DarkSurfaceElevated,
        onSurfaceVariant = DarkTextSecondary,
        outline = DarkBorder,
        outlineVariant = DarkBorderSubtle,
        error = DarkError,
        onError = DarkOnError,
        errorContainer = DarkErrorContainer,
        onErrorContainer = DarkError,
        inverseSurface = LightTextPrimary,
        inverseOnSurface = LightSurface,
        inversePrimary = LightAccent
    )

    val LightColorScheme: ColorScheme = lightColorScheme(
        primary = LightAccent,
        onPrimary = LightOnAccent,
        primaryContainer = LightAccentContainer,
        onPrimaryContainer = LightOnAccentContainer,
        secondary = LightTextSecondary,
        onSecondary = LightSurface,
        secondaryContainer = LightCard,
        onSecondaryContainer = LightTextPrimary,
        background = LightBackground,
        onBackground = LightTextPrimary,
        surface = LightSurface,
        onSurface = LightTextPrimary,
        surfaceVariant = LightSurfaceElevated,
        onSurfaceVariant = LightTextSecondary,
        outline = LightBorder,
        outlineVariant = LightBorderSubtle,
        error = LightError,
        onError = LightOnError,
        errorContainer = LightErrorContainer,
        onErrorContainer = LightError,
        inverseSurface = DarkTextPrimary,
        inverseOnSurface = DarkSurface,
        inversePrimary = DarkAccent
    )
}

@Composable
fun SecretNotesTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) PlumShadow.DarkColorScheme else PlumShadow.LightColorScheme,
        content = content
    )
}
