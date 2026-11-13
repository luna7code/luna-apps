package com.lunacode.mobilesecurity.intelligence

import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DexIndicatorScannerTest {
    @Test
    fun detectsCapabilityDescriptors() {
        val input = "Landroid/accessibilityservice/AccessibilityService;".toByteArray()
        val (signals, bytes) = DexIndicatorScanner.scanStream(ByteArrayInputStream(input), 1024)
        assertTrue(signals.contains("Accessibility service API"))
        assertEquals(input.size.toLong(), bytes)
    }

    @Test
    fun detectsSignatureSplitAcrossReadBoundary() {
        val signature = "Landroid/net/VpnService;"
        val padding = ByteArray(16 * 1024 - 7) { 'x'.code.toByte() }
        val payload = padding + signature.toByteArray()
        val (signals, _) = DexIndicatorScanner.scanStream(ByteArrayInputStream(payload), 64 * 1024)
        assertTrue(signals.contains("VPN service API"))
    }

    @Test
    fun stopsAtConfiguredByteLimit() {
        val payload = ByteArray(4096) { 0x41 }
        val (_, bytes) = DexIndicatorScanner.scanStream(ByteArrayInputStream(payload), 512)
        assertEquals(512L, bytes)
    }
}
