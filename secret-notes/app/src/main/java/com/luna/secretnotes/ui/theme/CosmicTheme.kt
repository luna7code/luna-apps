package com.luna.secretnotes.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** A quiet deep-space palette: event-horizon black, cold starlight and restrained nebula accents. */
object CosmicTheme {
    val Background = Color(0xFF03050A)
    val Surface = Color(0xFF090D16)
    val SurfaceElevated = Color(0xFF111827)
    val Card = Color(0xFF151E2D)
    val CardPressed = Color(0xFF202C3D)

    val TextPrimary = Color(0xFFF4F7FC)
    val TextSecondary = Color(0xFFB9C4D4)
    val TextMuted = Color(0xFF77869A)

    val Border = Color(0xFF526176)
    val BorderSubtle = Color(0xFF263346)
    val FocusRing = Color(0xFF9CCBFF)

    // Stellar accents are intentionally low-saturation; no synthwave neon.
    val Starlight = Color(0xFFE5EDF8)
    val PulsarBlue = Color(0xFF9CCBFF)
    val NebulaViolet = Color(0xFFB5A8D8)
    val EventHorizon = Color(0xFF05070D)

    val Error = Color(0xFFFF8B8B)
    val OnError = Color(0xFF3B0808)
    val ErrorContainer = Color(0xFF5B2025)
    val Success = Color(0xFF9AD8B5)
    val OnSuccess = Color(0xFF082C1A)
    val SuccessContainer = Color(0xFF173D2B)

    val ColorScheme: ColorScheme = darkColorScheme(
        primary = Starlight,
        onPrimary = Color(0xFF101722),
        primaryContainer = Color(0xFF29384B),
        onPrimaryContainer = Color(0xFFEAF2FF),
        secondary = PulsarBlue,
        onSecondary = Color(0xFF071525),
        secondaryContainer = Color(0xFF1A3048),
        onSecondaryContainer = Color(0xFFD8E9FF),
        tertiary = NebulaViolet,
        onTertiary = Color(0xFF201A30),
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
        onErrorContainer = Color(0xFFFFDADA),
        inverseSurface = TextPrimary,
        inverseOnSurface = Background,
        inversePrimary = Color(0xFF42546C)
    )
}

@Composable
fun SecretNotesTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CosmicTheme.ColorScheme, content = content)
}
