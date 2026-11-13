package com.lunacode.mobilesecurity.intelligence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThreatIndicatorMatcherTest {
    private val sha = "A".repeat(64)

    private fun indicator(
        id: String,
        packageName: String = "com.example.target",
        signer: String? = null,
        apk: String? = null
    ) = ThreatIndicator(
        id = id,
        packageName = packageName,
        apkSha256 = apk,
        signerSha256 = signer,
        severity = "MEDIUM",
        confidence = 80,
        title = id,
        detail = "test indicator"
    )

    @Test fun packageMismatchDoesNotRequestApkDigest() {
        var calls = 0
        val result = ThreatIndicatorMatcher.match(
            "com.example.other", setOf("CERT-A"), listOf(indicator("i1", apk = sha))
        ) { calls++; sha }
        assertTrue(result.isEmpty())
        assertEquals(0, calls)
    }

    @Test fun signerMismatchDoesNotRequestApkDigest() {
        var calls = 0
        val result = ThreatIndicatorMatcher.match(
            "com.example.target", setOf("CERT-A"), listOf(indicator("i1", signer = "CERT-B", apk = sha))
        ) { calls++; sha }
        assertTrue(result.isEmpty())
        assertEquals(0, calls)
    }

    @Test fun packageOnlyIndicatorDoesNotRequestApkDigest() {
        var calls = 0
        val result = ThreatIndicatorMatcher.match(
            "com.example.target", setOf("CERT-A"), listOf(indicator("i1"))
        ) { calls++; sha }
        assertEquals(listOf("i1"), result.map { it.id })
        assertEquals(0, calls)
    }

    @Test fun severalHashIndicatorsShareExactlyOneDigestCalculation() {
        var calls = 0
        val result = ThreatIndicatorMatcher.match(
            "com.example.target",
            setOf("CERT-A"),
            listOf(indicator("i1", signer = "CERT-A", apk = sha), indicator("i2", apk = sha))
        ) { calls++; sha.lowercase() }
        assertEquals(listOf("i1", "i2"), result.map { it.id })
        assertEquals(1, calls)
    }

    @Test fun computesDigestOnlyAfterSignerFilter() {
        var calls = 0
        val result = ThreatIndicatorMatcher.match(
            "com.example.target",
            setOf("CERT-A"),
            listOf(indicator("wrong", signer = "CERT-B", apk = sha), indicator("right", signer = "CERT-A", apk = sha))
        ) { calls++; sha }
        assertEquals(listOf("right"), result.map { it.id })
        assertEquals(1, calls)
    }
}
