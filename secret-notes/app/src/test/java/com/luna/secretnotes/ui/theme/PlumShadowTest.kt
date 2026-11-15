package com.luna.secretnotes.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class PlumShadowTest {
    @Test
    fun approvedPaletteHexValuesRemainStable() {
        assertEquals(0xFF100D12.toInt(), PlumShadow.DarkBackground.value.toLong().toInt())
        assertEquals(0xFF261D2B.toInt(), PlumShadow.DarkCard.value.toLong().toInt())
        assertEquals(0xFFD6A8E2.toInt(), PlumShadow.DarkAccent.value.toLong().toInt())
        assertEquals(0xFFFAF7FB.toInt(), PlumShadow.LightBackground.value.toLong().toInt())
        assertEquals(0xFF70427C.toInt(), PlumShadow.LightAccent.value.toLong().toInt())
    }

    @Test
    fun lightAndDarkSchemesUseTheirApprovedBackgroundAndAccent() {
        assertEquals(PlumShadow.DarkBackground, PlumShadow.DarkColorScheme.background)
        assertEquals(PlumShadow.DarkAccent, PlumShadow.DarkColorScheme.primary)
        assertEquals(PlumShadow.LightBackground, PlumShadow.LightColorScheme.background)
        assertEquals(PlumShadow.LightAccent, PlumShadow.LightColorScheme.primary)
    }
}
