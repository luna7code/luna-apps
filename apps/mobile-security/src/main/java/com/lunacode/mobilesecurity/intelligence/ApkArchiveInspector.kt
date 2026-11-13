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
    val warnings: List<String>,
    val dynamicSymbols: List<String> = emptyList(),
    val programHeaderCount: Int = 0
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
        val seenPaths = mutableSetOf<String>()
        ZipFile(file).use { zip ->
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                count++
                if (count > MAX_ENTRIES) { truncated = true; break }
                val normalized = entry.name.replace('\\', '/')
                if (normalized.startsWith("/") || normalized.split('/').any { it == ".." } ||
                    normalized.contains("\u0000")
                ) suspicious += "Unsafe archive path: ${entry.name.take(180)}"
                val canonical = canonicalPath(normalized)
                if (!seenPaths.add(canonical)) {
                    suspicious += "Duplicate ZIP path: ${canonical.take(180)}"
                }
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

    private fun canonicalPath(path: String): String {
        val parts = mutableListOf<String>()
        path.replace(92.toChar(), '/').split('/').forEach { part ->
            when (part) {
                "", "." -> Unit
                ".." -> if (parts.isNotEmpty()) parts.removeAt(parts.lastIndex) else parts += ".."
                else -> parts += part
            }
        }
        return parts.joinToString("/")
    }

    internal fun parseElf(path: String, bytes: ByteArray): NativeLibraryInfo {
        val parsed = ElfMetadataParser.inspect(bytes)
        val validHeader = bytes.size >= 20 && bytes[0] == 0x7f.toByte() &&
            bytes[1] == 'E'.code.toByte() && bytes[2] == 'L'.code.toByte() && bytes[3] == 'F'.code.toByte()
        if (!validHeader) {
            return NativeLibraryInfo(
                path, path.split('/').getOrNull(1) ?: "unknown", 0, 0,
                "Invalid ELF", null, null, emptyList(),
                parsed.warnings.ifEmpty { listOf("Invalid or truncated ELF header") }
            )
        }
        val elfClass = bytes[4].toInt() and 0xff
        val endian = bytes[5].toInt() and 0xff
        val machine = if (bytes.size >= 20) {
            val a = bytes[18].toInt() and 0xff
            val b = bytes[19].toInt() and 0xff
            if (endian == 2) (a shl 8) or b else a or (b shl 8)
        } else 0
        val machineName = when (machine) {
            3 -> "x86"
            40 -> "ARM"
            62 -> "x86_64"
            183 -> "AArch64"
            243 -> "RISC-V"
            else -> "Unknown ($machine)"
        }
        val warnings = parsed.warnings.toMutableList()
        if (elfClass !in 1..2) warnings += "Unknown ELF class"
        if (endian !in 1..2) warnings += "Unknown ELF byte order"
        if (elfClass == 1 && machine in setOf(62, 183)) warnings += "ELF class and machine mismatch"
        if (elfClass == 2 && machine in setOf(3, 40)) warnings += "ELF class and machine mismatch"
        if (machineName == "Unknown ($machine)") warnings += "Unrecognized ELF machine"
        if (parsed.textRelocations == true) warnings += "ELF contains text relocations"
        if (parsed.executableStack == true) warnings += "PT_GNU_STACK requests an executable stack"
        val text = bytes.copyOfRange(0, minOf(bytes.size, MAX_STRING_BYTES)).toString(Charsets.ISO_8859_1)
        val sonames = Regex("""lib[A-Za-z0-9_.+-]{1,100}\.so(?:\.[0-9]+)*""")
            .findAll(text).map { it.value }.distinct().take(40).toList()
        if (sonames.any { it.contains("frida", true) || it.contains("substrate", true) }) {
            warnings += "Instrumentation-related library name string"
        }
        if (text.contains("android_dlopen_ext") || text.contains("dlopen")) {
            warnings += "Dynamic native library loading API string"
        }
        return NativeLibraryInfo(
            path, path.split('/').getOrNull(1) ?: "unknown", elfClass, machine, machineName,
            parsed.executableStack, parsed.textRelocations,
            (parsed.neededLibraries + sonames).distinct().take(80),
            warnings.distinct().take(40),
            parsed.dynamicSymbols,
            parsed.programHeaderCount
        )
    }
}
