package com.lunacode.mobilesecurity.intelligence

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class ApkArchiveInspectorTest {
    @Test
    fun parsesElfMachineAndClassWithoutLoadingNativeCode() {
        val bytes = ByteArray(64)
        bytes[0] = 0x7f
        bytes[1] = 'E'.code.toByte()
        bytes[2] = 'L'.code.toByte()
        bytes[3] = 'F'.code.toByte()
        bytes[4] = 2
        bytes[5] = 1
        bytes[18] = 183.toByte()
        val result = ApkArchiveInspector.parseElf("lib/arm64-v8a/libsample.so", bytes)
        assertEquals("AArch64", result.machineName)
        assertEquals(2, result.elfClass)
        assertNull(result.hasExecutableStack)
        assertNull(result.hasTextRelocations)
        assertTrue(result.warnings.any { it.contains("PT_GNU_STACK is absent") })
    }

    @Test
    fun rejectsTruncatedAndNonElfNativeEntriesAsInvalidMetadata() {
        val result = ApkArchiveInspector.parseElf("lib/armeabi-v7a/libbad.so", byteArrayOf(1, 2, 3))
        assertEquals("Invalid ELF", result.machineName)
        assertTrue(result.warnings.any { it.contains("truncated") })
    }

    @Test
    fun detectsNestedApkAndNativeLibraryMetadata() {
        val apk = archive(
            "splits/base-master.apk" to byteArrayOf(1, 2, 3),
            "lib/arm64-v8a/libx.so" to elf64Arm64()
        )
        try {
            val report = ApkArchiveInspector.inspect(apk)
            assertTrue(report.nestedApkEntries.contains("splits/base-master.apk"))
            assertEquals(1, report.nativeLibraries.size)
            assertEquals("AArch64", report.nativeLibraries.single().machineName)
            assertFalse(report.truncated)
        } finally { apk.delete() }
    }

    @Test
    fun handlesCorruptZipAsFailureWithoutExtractingAnything() {
        val file = File.createTempFile("prosec-corrupt-", ".apk")
        try {
            file.writeBytes("not a zip".toByteArray())
            assertThrows(Exception::class.java) { ApkArchiveInspector.inspect(file) }
        } finally { file.delete() }
    }

    @Test
    fun malformedElfInputsNeverEscapeBoundsChecks() {
        val random = Random(0x50524f)
        repeat(512) { iteration ->
            val bytes = ByteArray(random.nextInt(0, 4096))
            random.nextBytes(bytes)
            if (bytes.size >= 4) {
                bytes[0] = 0x7f
                bytes[1] = 0x45
                bytes[2] = 0x4c
                bytes[3] = 0x46
            }
            val result = ApkArchiveInspector.parseElf("lib/arm64-v8a/fuzz$iteration.so", bytes)
            assertNotNull(result)
        }
    }

    @Test
    fun reportsZipBombRatioAndCanonicalDuplicatePaths() {
        val repeated = ByteArray(2 * 1024 * 1024)
        val apk = archive(
            "a/../payload.bin" to repeated,
            "payload.bin" to byteArrayOf(1, 2, 3)
        )
        try {
            val report = ApkArchiveInspector.inspect(apk)
            assertTrue(report.suspiciousArchiveEntries.any { it.contains("High compression ratio") })
            assertTrue(report.suspiciousArchiveEntries.any { it.contains("Duplicate ZIP path") })
            assertTrue(report.suspiciousArchiveEntries.any { it.contains("Unsafe archive path") })
        } finally { apk.delete() }
    }

    private fun elf64Arm64(): ByteArray = ByteArray(64).also {
        it[0] = 0x7f; it[1] = 69; it[2] = 76; it[3] = 70
        it[4] = 2; it[5] = 1; it[18] = 183.toByte()
        it[52] = 64; it[54] = 56
    }

    private fun archive(vararg entries: Pair<String, ByteArray>): File {
        val file = File.createTempFile("prosec-archive-", ".apk")
        ZipOutputStream(file.outputStream()).use { zip ->
            entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name)); zip.write(bytes); zip.closeEntry()
            }
        }
        return file
    }
}
