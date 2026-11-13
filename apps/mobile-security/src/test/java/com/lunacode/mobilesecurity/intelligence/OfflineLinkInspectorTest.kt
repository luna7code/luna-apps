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

    @Test
    fun flagsPunycodeDomain() {
        val result = OfflineLinkInspector.inspect("https://xn--80ak6aa92e.com")
        assertTrue(result.indicators.any { it.contains("Punycode") })
    }

    @Test
    fun flagsIpAddress() {
        val result = OfflineLinkInspector.inspect("https://192.0.2.10/login")
        assertTrue(result.indicators.any { it.contains("IPv4") })
    }

    @Test
    fun flagsNonWebSchemesWithoutOpeningThem() {
        val result = OfflineLinkInspector.inspect("javascript:alert(1)")
        assertTrue(result.indicators.any { it.contains("Необычная схема") })
        assertTrue(result.host.contains("нет домена"))
    }
}
