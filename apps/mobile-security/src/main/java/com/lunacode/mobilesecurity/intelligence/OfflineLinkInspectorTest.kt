package com.lunacode.mobilesecurity.intelligence

import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineLinkInspectorTest {
    @Test
    fun flagsPlainHttp() {
        val result = OfflineLinkInspector.inspect("http://example.com/login")
        assertTrue(result.indicators.any { it.contains("HTTP") })
    }

    @Test
    fun flagsShortener() {
        val result = OfflineLinkInspector.inspect("https://bit.ly/abc123")
        assertTrue(result.indicators.any { it.contains("сократ") })
    }

    @Test
    fun flagsUserInfoSpoofing() {
        val result = OfflineLinkInspector.inspect("https://trusted.example@evil.example/path")
        assertTrue(result.indicators.any { it.contains("user-info") })
        assertTrue(result.host.contains("evil.example"))
    }
}
