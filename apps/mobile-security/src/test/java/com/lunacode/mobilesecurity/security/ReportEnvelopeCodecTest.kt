package com.lunacode.mobilesecurity.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportEnvelopeCodecTest {
    @Test
    fun sealedEnvelopeRoundTripsPayloadAndChain() {
        val report = """{"schema":2,"findings":[]}"""
        val wrapped = ReportEnvelopeCodec.encode("previous-tag", report, "signed-tag")
        val decoded = ReportEnvelopeCodec.decode(wrapped)

        assertEquals(report, decoded.report)
        assertEquals("previous-tag", decoded.previousTag)
        assertEquals("signed-tag", decoded.chainTag)
        assertTrue(decoded.wrapped)
    }

    @Test
    fun oldUnwrappedReportsRemainReadableAsLegacy() {
        val report = """{"schema":2,"findings":[]}"""
        val decoded = ReportEnvelopeCodec.decode(report)

        assertEquals(report, decoded.report)
        assertEquals("", decoded.previousTag)
        assertNull(decoded.chainTag)
        assertFalse(decoded.wrapped)
    }

    @Test
    fun malformedSealedEnvelopeDoesNotDowngradeToLegacy() {
        val decoded = ReportEnvelopeCodec.decode("PROSEC-CHAIN-1\nnot-base64\n\ninvalid")
        assertTrue(decoded.wrapped)
        assertNull(decoded.chainTag)
        assertEquals("", decoded.report)
    }

    @Test
    fun alteredChainPrefixDoesNotDowngradeToLegacy() {
        val decoded = ReportEnvelopeCodec.decode("PROSEC-CHAIN-2\nnot-a-valid-envelope")
        assertTrue(decoded.wrapped)
        assertNull(decoded.chainTag)
        assertEquals("", decoded.report)
    }

    @Test
    fun nonJsonPayloadDoesNotDowngradeToLegacy() {
        val decoded = ReportEnvelopeCodec.decode("XROSEC-CHAIN-1\ncorrupted")
        assertTrue(decoded.wrapped)
        assertNull(decoded.chainTag)
        assertEquals("", decoded.report)
    }

    @Test
    fun signatureMaterialBindsPreviousTagAndReport() {
        val report = """{"schema":2}"""
        val first = ReportEnvelopeCodec.material("tag-a", report)
        val second = ReportEnvelopeCodec.material("tag-b", report)
        assertFalse(first == second)
    }
}
