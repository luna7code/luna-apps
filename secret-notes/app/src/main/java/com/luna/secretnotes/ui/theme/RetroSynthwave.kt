package com.luna.secretnotes.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * SECRET NOTES — Dark Retro Synthwave.
 *
 * Night-drive atmosphere with restrained neon pink and cyan accents.
 * Keep large surfaces dark; reserve glow accents for focused and selected UI.
 */
object RetroSynthwave {
    // Deep-night surfaces
    val Background = Color(0xFF090611)
    val Surface = Color(0xFF120D1E)
    val SurfaceElevated = Color(0xFF1B1230)
    val Card = Color(0xFF211538)
    val CardPressed = Color(0xFF2D1B48)

    // Text
    val TextPrimary = Color(0xFFF8F4FF)
    val TextSecondary = Color(0xFFC9BEDF)
    val TextMuted = Color(0xFFA99BBF)

    // Borders and focus
    val Border = Color(0xFF624A7E)
    val BorderSubtle = Color(0xFF38284B)
    val FocusRing = Color(0xFF47E8FF)

    // Synthwave accents
    val NeonPink = Color(0xFFFF4FD8)
    val NeonPinkPressed = Color(0xFFE63BBF)
    val OnNeonPink = Color(0xFF24091F)
    val NeonPinkContainer = Color(0xFF4A1643)
    val OnNeonPinkContainer = Color(0xFFFFD7F6)
    val NeonCyan = Color(0xFF47E8FF)
    val OnNeonCyan = Color(0xFF00232A)
    val NeonViolet = Color(0xFFB68CFF)

    // Semantic states
    val Error = Color(0xFFFF6B8A)
    val OnError = Color(0xFF3B0010)
    val ErrorContainer = Color(0xFF5C1428)
    val Success = Color(0xFF65F0B4)
    val OnSuccess = Color(0xFF003824)
    val SuccessContainer = Color(0xFF124431)

    val ColorScheme: ColorScheme = darkColorScheme(
        primary = NeonPink,
        onPrimary = OnNeonPink,
        primaryContainer = NeonPinkContainer,
        onPrimaryContainer = OnNeonPinkContainer,
        secondary = NeonCyan,
        onSecondary = OnNeonCyan,
        secondaryContainer = Color(0xFF10343D),
        onSecondaryContainer = Color(0xFFC8F8FF),
        tertiary = NeonViolet,
        onTertiary = Color(0xFF271044),
        background = Background,
        onBackground = TextPrimary,
        surface = Surface,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceElevated,
        onSurfaceVariant = TextSecondary,
        outline = Border,
        outlineVariant = BorderSubtle,
        error = Error,
        onError = OnError,
        errorContainer = ErrorContainer,
        onErrorContainer = Color(0xFFFFD9E0),
        inverseSurface = TextPrimary,
        inverseOnSurface = Background,
        inversePrimary = NeonPinkPressed
    )
}

@Composable
fun SecretNotesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RetroSynthwave.ColorScheme,
        content = content
    )
}
