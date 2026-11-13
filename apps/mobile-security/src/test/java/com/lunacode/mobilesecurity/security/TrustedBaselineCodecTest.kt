package com.lunacode.mobilesecurity.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrustedBaselineCodecTest {
    private val validReport = """{"schema":2,"apps":[]}"""
    private val validate: (String) -> Boolean = {
        it.startsWith("{") && it.contains("\"schema\":2") && it.contains("\"apps\"")
    }
    private val sign: (String) -> String = { "test-signature:" + it.hashCode() }
    private val verify: (String, String) -> Boolean = { material, tag -> sign(material) == tag }

    @Test fun roundTripsOnlyValidatedReportAndTimestamp() {
        val encoded = TrustedBaselineCodec.encode(validReport, 1234L, validate, sign)
        val decoded = TrustedBaselineCodec.decode(encoded, 1234L, validate, verify)
        assertTrue(decoded is TrustedBaselineRead.Valid)
        decoded as TrustedBaselineRead.Valid
        assertEquals(validReport, decoded.report)
        assertEquals(1234L, decoded.savedAt)
    }

    @Test fun rejectsTamperedReportEvenWhenEnvelopeStillParses() {
        val tampered = ReportEnvelopeCodec.encode("", """{"schema":2,"apps":[{"name":"changed"}]}""", "old-signature")
        assertTrue(TrustedBaselineCodec.decode(tampered, 1234L, validate, verify) is TrustedBaselineRead.Invalid)
    }

    @Test fun rejectsMissingOrInvalidTimestamp() {
        val encoded = TrustedBaselineCodec.encode(validReport, 1234L, validate, sign)
        assertTrue(TrustedBaselineCodec.decode(encoded, 0L, validate, verify) is TrustedBaselineRead.Invalid)
        assertTrue(runCatching { TrustedBaselineCodec.encode(validReport, -1L, validate, sign) }.isFailure)
    }

    @Test fun rejectsInvalidSchemaBeforeSigning() {
        assertTrue(runCatching {
            TrustedBaselineCodec.encode("""{"schema":1,"apps":[]}""", 1234L, validate, sign)
        }.isFailure)
    }

    @Test fun rejectsInvalidSignatureAndMalformedEnvelope() {
        val encoded = TrustedBaselineCodec.encode(validReport, 1234L, validate, sign)
        assertTrue(TrustedBaselineCodec.decode(encoded, 1234L, validate) { _, _ -> false } is TrustedBaselineRead.Invalid)
        assertTrue(TrustedBaselineCodec.decode("PROSEC-CHAIN-1\ninvalid", 1234L, validate, verify) is TrustedBaselineRead.Invalid)
    }

    @Test fun rejectsEmptyReport() {
        assertTrue(runCatching { TrustedBaselineCodec.encode("", 1234L, validate, sign) }.isFailure)
    }
}
