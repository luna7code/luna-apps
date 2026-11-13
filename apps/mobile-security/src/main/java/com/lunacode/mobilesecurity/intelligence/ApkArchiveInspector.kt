package com.lunacode.mobilesecurity.intelligence

import java.io.File
import java.util.zip.ZipFile

data class NativeLibraryInfo(
    val path: String,
    val abi: String,
    val elfClass: Int,
    val machine: Int,
    val machineName: String,
    val hasExecutableStack: Boolean?,
    val hasTextRelocations: Boolean?,
    val dynamicLibraryNames: List<String>,
    val warnings: List<String>
)

data class ApkArchiveInspection(
    val nativeLibraries: List<NativeLibraryInfo>,
    val nestedApkEntries: List<String>,
    val entryCount: Int,
    val totalUncompressedBytes: Long,
    val suspiciousArchiveEntries: List<String>,
    val truncated: Boolean
)

/**
 * Inert, bounded ZIP/ELF metadata reader. Never extracts files to disk or loads native code.
 * APK signing-block metadata and binary XML are intentionally left to Android's package parser.
 */
object ApkArchiveInspector {
    private const val MAX_ENTRIES = 1200
    private const val MAX_TOTAL_UNCOMPRESSED = 256L * 1024 * 1024
    private const val MAX_NATIVE_LIBRARIES = 80
    private const val MAX_NATIVE_BYTES = 16L * 1024 * 1024
    private const val MAX_STRING_BYTES = 512 * 1024
    private const val MAX_RATIO = 200L

    fun inspect(file: File): ApkArchiveInspection {
        val native = mutableListOf<NativeLibraryInfo>()
        val nested = mutableListOf<String>()
        val suspicious = mutableListOf<String>()
        var count = 0
        var total = 0L
        var truncated = false
        ZipFile(file).use { zip ->
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                count++
                if (count > MAX_ENTRIES) { truncated = true; break }
                val normalized = entry.name.replace('\\\\', '/')
                if (normalized.startsWith("/") || normalized.split('/').any { it == ".." } ||
                    normalized.contains("\u0000")
                ) suspicious += "Unsafe archive path: ${entry.name.take(180)}"
                if (entry.size > 0 && entry.compressedSize > 0 &&
                    entry.size / entry.compressedSize.coerceAtLeast(1) > MAX_RATIO
                ) suspicious += "High compression ratio: ${entry.name.take(180)}"
                if (entry.size > 0 && total + entry.size > MAX_TOTAL_UNCOMPRESSED) {
                    truncated = true
                    break
                }
                total += entry.size.coerceAtLeast(0)
                if (normalized.endsWith(".apk", ignoreCase = true) && nested.size < 40) nested += normalized
                if (normalized.startsWith("lib/") && normalized.endsWith(".so", ignoreCase = true)) {
                    if (native.size >= MAX_NATIVE_LIBRARIES || entry.size > MAX_NATIVE_BYTES) {
                        truncated = true
                        continue
                    }
                    val bytes = zip.getInputStream(entry).use { input ->
                        StaticStringIndicatorScanner.readBounded(input, MAX_NATIVE_BYTES)
                    }
                    if (entry.size >= 0 && bytes.size.toLong() < entry.size) truncated = true
                    native += parseElf(normalized, bytes)
                }
            }
        }
        return ApkArchiveInspection(native, nested, count.coerceAtMost(MAX_ENTRIES), total, suspicious.distinct().take(80), truncated)
    }

    internal fun parseElf(path: String, bytes: ByteArray): NativeLibraryInfo {
        val warnings = mutableListOf<String>()
        val valid = bytes.size >= 20 && bytes[0] == 0x7f.toByte() &&
            bytes[1] == 'E'.code.toByte() && bytes[2] == 'L'.code.toByte() && bytes[3] == 'F'.code.toByte()
        if (!valid) {
            return NativeLibraryInfo(path, path.split('/').getOrNull(1) ?: "unknown", 0, 0,
                "Invalid ELF", null, null, emptyList(), listOf("Invalid or truncated ELF header"))
        }
        val elfClass = bytes[4].toInt() and 0xff
        val data = bytes[5].toInt() and 0xff
        if (elfClass !in 1..2) warnings += "Unknown ELF class"
        if (data !in 1..2) warnings += "Unknown ELF byte order"
        fun u16(offset: Int): Int {
            if (offset + 1 >= bytes.size) return 0
            val a = bytes[offset].toInt() and 0xff
            val b = bytes[offset + 1].toInt() and 0xff
            return if (data == 2) (a shl 8) or b else a or (b shl 8)
        }
        val machine = u16(18)
        val machineName = when (machine) {
            3 -> "x86"
            40 -> "ARM"
            62 -> "x86_64"
            183 -> "AArch64"
            243 -> "RISC-V"
            else -> "Unknown ($machine)"
        }
        if (elfClass == 1 && machine in setOf(62, 183)) warnings += "ELF class and machine mismatch"
        if (elfClass == 2 && machine in setOf(3, 40)) warnings += "ELF class and machine mismatch"
        if (machineName == "Unknown ($machine)") warnings += "Unrecognized ELF machine"
        // Bounded string scan only. It does not parse or resolve relocations or execute code.
        val text = bytes.copyOfRange(0, minOf(bytes.size, MAX_STRING_BYTES))
            .toString(Charsets.ISO_8859_1)
        val sonames = Regex("""lib[A-Za-z0-9_.+-]{1,100}\\.so(?:\\.[0-9]+)*""")
            .findAll(text).map { it.value }.distinct().take(40).toList()
        if (sonames.any { it.contains("frida", true) || it.contains("substrate", true) }) {
            warnings += "Instrumentation-related library name string"
        }
        if (text.contains("android_dlopen_ext") || text.contains("dlopen")) {
            warnings += "Dynamic native library loading API string"
        }
        return NativeLibraryInfo(
            path, path.split('/').getOrNull(1) ?: "unknown", elfClass, machine, machineName,
            null, null, sonames, warnings
        )
    }
}
