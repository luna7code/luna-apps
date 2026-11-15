package com.luna.secretnotes.ui.theme

import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Test

class PlumShadowTest {
    @Test
    fun approvedPaletteHexValuesRemainStable() {
        assertEquals(0xFF100D12.toInt(), PlumShadow.DarkBackground.toArgb())
        assertEquals(0xFF261D2B.toInt(), PlumShadow.DarkCard.toArgb())
        assertEquals(0xFFD6A8E2.toInt(), PlumShadow.DarkAccent.toArgb())
        assertEquals(0xFFFAF7FB.toInt(), PlumShadow.LightBackground.toArgb())
        assertEquals(0xFF70427C.toInt(), PlumShadow.LightAccent.toArgb())
    }

    @Test
    fun lightAndDarkSchemesUseTheirApprovedBackgroundAndAccent() {
        assertEquals(PlumShadow.DarkBackground, PlumShadow.DarkColorScheme.background)
        assertEquals(PlumShadow.DarkAccent, PlumShadow.DarkColorScheme.primary)
        assertEquals(PlumShadow.LightBackground, PlumShadow.LightColorScheme.background)
        assertEquals(PlumShadow.LightAccent, PlumShadow.LightColorScheme.primary)
    }
}
