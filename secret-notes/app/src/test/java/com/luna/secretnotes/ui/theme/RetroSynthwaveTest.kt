package com.luna.secretnotes.ui.theme

import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Test

class RetroSynthwaveTest {
    @Test
    fun darkRetroSynthwavePaletteTokensStayStable() {
        assertEquals(0xFF090611.toInt(), RetroSynthwave.Background.toArgb())
        assertEquals(0xFF211538.toInt(), RetroSynthwave.Card.toArgb())
        assertEquals(0xFFFF4FD8.toInt(), RetroSynthwave.NeonPink.toArgb())
        assertEquals(0xFF47E8FF.toInt(), RetroSynthwave.NeonCyan.toArgb())
        assertEquals(0xFF65F0B4.toInt(), RetroSynthwave.Success.toArgb())
    }

    @Test
    fun materialSchemeUsesSynthwaveSemanticColors() {
        assertEquals(RetroSynthwave.Background, RetroSynthwave.ColorScheme.background)
        assertEquals(RetroSynthwave.NeonPink, RetroSynthwave.ColorScheme.primary)
        assertEquals(RetroSynthwave.NeonCyan, RetroSynthwave.ColorScheme.secondary)
        assertEquals(RetroSynthwave.Error, RetroSynthwave.ColorScheme.error)
    }
}
