package com.lunacode.mobilesecurity.intelligence

/**
 * Bounded, inert ELF metadata parser. It reads bytes only; it never maps, loads,
 * relocates, or executes the inspected object.
 */
internal object ElfMetadataParser {
    data class Result(
        val executableStack: Boolean?,
        val textRelocations: Boolean?,
        val neededLibraries: List<String>,
        val dynamicSymbols: List<String>,
        val programHeaderCount: Int,
        val warnings: List<String>
    )

    private data class Segment(val type: Long, val flags: Long, val offset: Long, val vaddr: Long, val fileSize: Long)

    private const val PT_LOAD = 1L
    private const val PT_DYNAMIC = 2L
    private const val PT_GNU_STACK = 0x6474e551L
    private const val PF_X = 1L
    private const val DT_NULL = 0L
    private const val DT_NEEDED = 1L
    private const val DT_HASH = 4L
    private const val DT_STRTAB = 5L
    private const val DT_SYMTAB = 6L
    private const val DT_STRSZ = 10L
    private const val DT_SYMENT = 11L
    private const val DT_TEXTREL = 22L
    private const val DT_FLAGS = 30L
    private const val DF_TEXTREL = 4L
    private const val MAX_PROGRAM_HEADERS = 256
    private const val MAX_DYNAMIC_ENTRIES = 8192
    private const val MAX_SYMBOLS = 4096
    private const val MAX_REPORTED_SYMBOLS = 80
    private const val MAX_NEEDED = 80
    private const val MAX_STRING_TABLE = 2L * 1024 * 1024

    fun inspect(bytes: ByteArray): Result {
        val warnings = mutableListOf<String>()
        if (bytes.size < 52 || bytes[0] != 0x7f.toByte() ||
            bytes[1] != 0x45.toByte() || bytes[2] != 0x4c.toByte() || bytes[3] != 0x46.toByte()
        ) return Result(null, null, emptyList(), emptyList(), 0, listOf("Invalid or truncated ELF header"))

        val elfClass = u8(bytes, 4)
        val encoding = u8(bytes, 5)
        if (elfClass !in 1..2 || encoding !in 1..2) {
            return Result(null, null, emptyList(), emptyList(), 0, listOf("Unsupported ELF class or byte order"))
        }
        val is64 = elfClass == 2
        val endian = encoding == 2
        val headerSize = if (is64) 64 else 52
        if (bytes.size < headerSize) return Result(null, null, emptyList(), emptyList(), 0, listOf("Truncated ELF header"))

        val phoff = (if (is64) u64(bytes, 32, endian) else u32(bytes, 28, endian))
            ?: return Result(null, null, emptyList(), emptyList(), 0, listOf("Invalid ELF program-header offset"))
        val ehsize = u16(bytes, if (is64) 52 else 40, endian)
        val phentsize = u16(bytes, if (is64) 54 else 42, endian)
        val phnum = u16(bytes, if (is64) 56 else 44, endian)
        if (ehsize < headerSize || phentsize < if (is64) 56 else 32) {
            return Result(null, null, emptyList(), emptyList(), 0, listOf("Invalid ELF header or program-header entry size"))
        }
        if (phnum > MAX_PROGRAM_HEADERS) {
            return Result(null, null, emptyList(), emptyList(), 0, listOf("Program-header count exceeds safety limit"))
        }
        val tableBytes = phentsize.toLong() * phnum.toLong()
        if (!range(bytes, phoff, tableBytes)) {
            return Result(null, null, emptyList(), emptyList(), 0, listOf("Program-header table is out of bounds"))
        }

        val segments = ArrayList<Segment>(phnum)
        var executableStack: Boolean? = null
        for (index in 0 until phnum) {
            val at = phoff + index.toLong() * phentsize
            val type = u32(bytes, at, endian) ?: continue
            val flags: Long
            val offset: Long
            val vaddr: Long
            val fileSize: Long
            if (is64) {
                flags = u32(bytes, at + 4, endian) ?: continue
                offset = u64(bytes, at + 8, endian) ?: continue
                vaddr = u64(bytes, at + 16, endian) ?: continue
                fileSize = u64(bytes, at + 32, endian) ?: continue
            } else {
                offset = u32(bytes, at + 4, endian) ?: continue
                vaddr = u32(bytes, at + 8, endian) ?: continue
                fileSize = u32(bytes, at + 16, endian) ?: continue
                flags = u32(bytes, at + 24, endian) ?: continue
            }
            if (!range(bytes, offset, fileSize)) {
                warnings += "Program header $index references bytes outside the file"
                continue
            }
            segments += Segment(type, flags, offset, vaddr, fileSize)
            if (type == PT_GNU_STACK) executableStack = flags and PF_X != 0L
        }
        if (segments.none { it.type == PT_GNU_STACK }) {
            warnings += "PT_GNU_STACK is absent; stack executability is not asserted"
        }

        val dynamic = segments.firstOrNull { it.type == PT_DYNAMIC }
            ?: return Result(executableStack, null, emptyList(), emptyList(), phnum, warnings + "PT_DYNAMIC is absent")
        val entrySize = if (is64) 16L else 8L
        val count = minOf(dynamic.fileSize / entrySize, MAX_DYNAMIC_ENTRIES.toLong()).toInt()
        if (dynamic.fileSize / entrySize > MAX_DYNAMIC_ENTRIES) warnings += "Dynamic-entry count capped"
        val tags = mutableListOf<Pair<Long, Long>>()
        for (i in 0 until count) {
            val at = dynamic.offset + i * entrySize
            val tag = if (is64) u64(bytes, at, endian) else u32(bytes, at, endian)
            val value = if (is64) u64(bytes, at + 8, endian) else u32(bytes, at + 4, endian)
            if (tag == null || value == null) { warnings += "Malformed dynamic entry"; break }
            if (tag == DT_NULL) break
            tags += tag to value
        }
        var textRelocations = tags.any { (tag, _) -> tag == DT_TEXTREL }
        val flagsValue = tags.firstOrNull { it.first == DT_FLAGS }?.second
        if (flagsValue != null && (flagsValue and DF_TEXTREL) != 0L) textRelocations = true

        val strtabVaddr = tags.firstOrNull { it.first == DT_STRTAB }?.second
        val strsz = tags.firstOrNull { it.first == DT_STRSZ }?.second ?: 0L
        val strtabOffset = strtabVaddr?.let { virtualToFile(it, segments) }
        val stringTableValid = strtabOffset != null && strsz in 1..MAX_STRING_TABLE &&
            range(bytes, strtabOffset, strsz)
        if (strtabVaddr != null && !stringTableValid) warnings += "Dynamic string table is invalid or exceeds bounds"

        val needed = mutableListOf<String>()
        if (stringTableValid) {
            tags.filter { it.first == DT_NEEDED }.take(MAX_NEEDED).forEach { (_, index) ->
                readCString(bytes, strtabOffset!! + index, strtabOffset!! + strsz)?.let { needed += it }
                    ?: run { warnings += "DT_NEEDED string offset is invalid" }
            }
        }

        val symbols = mutableListOf<String>()
        val symtabVaddr = tags.firstOrNull { it.first == DT_SYMTAB }?.second
        val symtabOffset = symtabVaddr?.let { virtualToFile(it, segments) }
        val syment = tags.firstOrNull { it.first == DT_SYMENT }?.second ?: if (is64) 24L else 16L
        if (symtabOffset != null && stringTableValid && syment >= (if (is64) 24L else 16L) &&
            syment <= 64L
        ) {
            val hashVaddr = tags.firstOrNull { it.first == DT_HASH }?.second
            val hashOffset = hashVaddr?.let { virtualToFile(it, segments) }
            val hashCount = hashOffset?.let { if (range(bytes, it, 8)) u32(bytes, it + 4, endian) else null }
            val estimated = if (hashCount != null) hashCount else {
                warnings += "Dynamic symbol count estimated without DT_HASH; symbol coverage may be incomplete"
                ((strtabOffset!! - symtabOffset) / syment).coerceAtLeast(0)
            }
            val symbolCount = minOf(estimated, MAX_SYMBOLS.toLong()).toInt()
            if (estimated > MAX_SYMBOLS) warnings += "Dynamic symbol count capped"
            for (i in 0 until symbolCount) {
                val at = symtabOffset + i.toLong() * syment
                if (!range(bytes, at, syment)) break
                val nameOffset = u32(bytes, at, endian) ?: break
                if (nameOffset == 0L || nameOffset >= strsz) continue
                val infoOffset = at + if (is64) 4 else 12
                val info = u8(bytes, infoOffset)
                val binding = info ushr 4
                if (binding !in 1..2) continue
                readCString(bytes, strtabOffset!! + nameOffset, strtabOffset!! + strsz)?.let {
                    if (symbols.size < MAX_REPORTED_SYMBOLS && it.isNotBlank()) symbols += it
                }
            }
        } else if (symtabVaddr != null) {
            warnings += "Dynamic symbol table could not be safely mapped"
        }

        return Result(
            executableStack,
            textRelocations,
            needed.distinct().take(MAX_NEEDED),
            symbols.distinct().take(MAX_REPORTED_SYMBOLS),
            phnum,
            warnings.distinct().take(40)
        )
    }

    private fun virtualToFile(address: Long, segments: List<Segment>): Long? {
        for (segment in segments) {
            if (segment.type != PT_LOAD || address < segment.vaddr) continue
            val delta = address - segment.vaddr
            if (delta < segment.fileSize && segment.offset <= Long.MAX_VALUE - delta) return segment.offset + delta
        }
        return null
    }

    private fun readCString(bytes: ByteArray, start: Long, end: Long): String? {
        if (start < 0 || end < start || end > bytes.size.toLong() || start >= end) return null
        val limit = minOf(end, start + 4096L)
        var p = start.toInt()
        val stop = limit.toInt()
        while (p < stop && bytes[p].toInt() != 0) p++
        if (p == stop) return null
        return bytes.copyOfRange(start.toInt(), p).toString(Charsets.UTF_8)
    }

    private fun range(bytes: ByteArray, offset: Long, length: Long): Boolean =
        offset >= 0 && length >= 0 && offset <= bytes.size.toLong() &&
            length <= bytes.size.toLong() - offset

    private fun u8(bytes: ByteArray, offset: Long): Int =
        if (offset >= 0 && offset < bytes.size) bytes[offset.toInt()].toInt() and 0xff else 0

    private fun u16(bytes: ByteArray, offset: Int, be: Boolean): Int {
        if (offset < 0 || offset > bytes.size - 2) return 0
        val a = bytes[offset].toInt() and 0xff
        val b = bytes[offset + 1].toInt() and 0xff
        return if (be) (a shl 8) or b else a or (b shl 8)
    }

    private fun u32(bytes: ByteArray, offset: Long, be: Boolean): Long? {
        if (!range(bytes, offset, 4)) return null
        val p = offset.toInt()
        var value = 0L
        if (be) for (i in 0..3) value = (value shl 8) or (bytes[p + i].toLong() and 0xff)
        else for (i in 3 downTo 0) value = (value shl 8) or (bytes[p + i].toLong() and 0xff)
        return value
    }

    private fun u64(bytes: ByteArray, offset: Long, be: Boolean): Long? {
        if (!range(bytes, offset, 8)) return null
        val p = offset.toInt()
        var value = 0L
        if (be) for (i in 0..7) {
            val b = bytes[p + i].toLong() and 0xff
            if (value > (Long.MAX_VALUE - b) / 256L) return null
            value = value * 256L + b
        } else for (i in 7 downTo 0) {
            val b = bytes[p + i].toLong() and 0xff
            if (value > (Long.MAX_VALUE - b) / 256L) return null
            value = value * 256L + b
        }
        return value
    }
}
