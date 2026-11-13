package com.lunacode.mobilesecurity.intelligence

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
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
        assertTrue(result.warnings.isEmpty())
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

    private fun elf64Arm64(): ByteArray = ByteArray(64).also {
        it[0] = 0x7f; it[1] = 69; it[2] = 76; it[3] = 70
        it[4] = 2; it[5] = 1; it[18] = 183.toByte()
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
