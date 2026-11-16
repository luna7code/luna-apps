package com.luna.secretnotes.ui.theme

import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CosmicThemeTest {
    @Test
    fun paletteUsesDeepSpaceFoundationsAndStarlightAccents() {
        assertEquals(0xFF03050A.toInt(), CosmicTheme.Background.toArgb())
        assertEquals(0xFFE5EDF8.toInt(), CosmicTheme.Starlight.toArgb())
        assertEquals(0xFF9CCBFF.toInt(), CosmicTheme.PulsarBlue.toArgb())
        assertNotEquals(CosmicTheme.Background, CosmicTheme.Surface)
    }
}
