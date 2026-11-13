package com.lunacode.mobilesecurity.intelligence

import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StaticStringIndicatorScannerTest {
    @Test
    fun extractsUrlsAndGroupsCapabilityStringsWithoutExecutingAnything() {
        val apk = createArchive(
            "classes.dex" to "const-string https://example.invalid/api java/lang/ProcessBuilder SmsManager".toByteArray(),
            "assets/readme.txt" to "https://cdn.example.invalid/payload".toByteArray()
        )
        try {
            val result = StaticStringIndicatorScanner.scan(apk)
            assertTrue(result.urls.any { it.startsWith("https://example.invalid/api") })
            assertTrue(result.urls.any { it.startsWith("https://cdn.example.invalid/payload") })
            assertTrue(result.findings.any { it.indicator == "java/lang/ProcessBuilder" })
            assertTrue(result.findings.any { it.indicator == "SmsManager" })
            assertEquals(2, result.entriesScanned)
        } finally {
            apk.delete()
        }
    }

    @Test
    fun ignoresNonCandidateEntriesAndDoesNotTreatPlainTextAsProofOfMalware() {
        val apk = createArchive(
            "assets/image.png" to "java/lang/ProcessBuilder https://evil.invalid".toByteArray(),
            "classes.dex" to "ordinary application code".toByteArray()
        )
        try {
            val result = StaticStringIndicatorScanner.scan(apk)
            assertTrue(result.urls.isEmpty())
            assertTrue(result.findings.isEmpty())
            assertFalse(result.truncated)
        } finally {
            apk.delete()
        }
    }

    @Test
    fun boundedReaderNeverReadsBeyondLimit() {
        val bytes = ByteArray(4096) { 0x41 }
        val read = StaticStringIndicatorScanner.readBounded(ByteArrayInputStream(bytes), 128)
        assertEquals(128, read.size)
    }

    private fun createArchive(vararg entries: Pair<String, ByteArray>): File {
        val file = File.createTempFile("prosec-static-", ".apk")
        ZipOutputStream(file.outputStream()).use { zip ->
            entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return file
    }
}
