package com.lunacode.mobilesecurity.intelligence

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class ManifestIntentFilter(
    val component: String,
    val actions: List<String>,
    val categories: List<String>,
    val schemes: List<String>,
    val hosts: List<String>,
    val autoVerify: Boolean
)

data class BinaryManifestInspection(
    val packageName: String?,
    val minSdk: Int?,
    val targetSdk: Int?,
    val declaredPermissions: List<Pair<String, String?>>,
    val intentFilters: List<ManifestIntentFilter>,
    val parseWarnings: List<String>
)

/**
 * Minimal, bounds-checked Android binary XML reader for manifest metadata.
 * Unknown chunks/attributes are ignored. It never resolves resources or executes APK code.
 */
object AndroidBinaryManifestInspector {
    private const val RES_XML_TYPE = 0x0003
    private const val RES_STRING_POOL_TYPE = 0x0001
    private const val RES_XML_START_ELEMENT_TYPE = 0x0102
    private const val RES_XML_END_ELEMENT_TYPE = 0x0103
    private const val NO_INDEX = -1

    fun inspect(bytes: ByteArray): BinaryManifestInspection {
        val warnings = mutableListOf<String>()
        val strings = mutableListOf<String>()
        val permissions = mutableListOf<Pair<String, String?>>()
        val filters = mutableListOf<ManifestIntentFilter>()
        var packageName: String? = null
        var minSdk: Int? = null
        var targetSdk: Int? = null
        var currentComponent: String? = null
        var inFilter = false
        var actions = mutableListOf<String>()
        var categories = mutableListOf<String>()
        var schemes = mutableListOf<String>()
        var hosts = mutableListOf<String>()
        var autoVerify = false

        try {
            require(bytes.size >= 8) { "Binary manifest too short" }
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            require(u16(buffer, 0) == RES_XML_TYPE) { "Not Android binary XML" }
            val declaredSize = u32(buffer, 4)
            require(declaredSize in 8..bytes.size) { "Invalid binary XML size" }
            var offset = u16(buffer, 2)
            while (offset >= 0 && offset.toLong() + 8L <= declaredSize.toLong()) {
                val type = u16(buffer, offset)
                val headerSize = u16(buffer, offset + 2)
                val chunkSize = u32(buffer, offset + 4)
                if (headerSize < 8 || chunkSize < headerSize || chunkSize <= 0 || offset.toLong() + chunkSize.toLong() > declaredSize.toLong()) {
                    warnings += "Invalid XML chunk bounds"
                    break
                }
                when (type) {
                    RES_STRING_POOL_TYPE -> {
                        strings.clear()
                        strings.addAll(readStringPool(buffer, offset, chunkSize))
                    }
                    RES_XML_START_ELEMENT_TYPE -> {
                        if (headerSize >= 16 && chunkSize >= 36) {
                            val nameIndex = u32(buffer, offset + 20)
                            val tag = strings.getOrNull(nameIndex) ?: ""
                            val attributeStart = u16(buffer, offset + 24)
                            val attributeSize = u16(buffer, offset + 26)
                            val attributeCount = u16(buffer, offset + 28)
                            val attributes = linkedMapOf<String, String>()
                            val base = offset + 16 + attributeStart
                            if (attributeSize < 20 || attributeCount > 512) {
                                warnings += "Invalid manifest attribute table"
                            } else {
                                for (i in 0 until attributeCount) {
                                    val at = base + i * attributeSize
                                    if (at + 20 > offset + chunkSize) { warnings += "Truncated manifest attributes"; break }
                                    val attrName = strings.getOrNull(u32(buffer, at + 4)) ?: continue
                                    val rawIndex = u32(buffer, at + 8)
                                    val valueType = buffer.get(at + 15).toInt() and 0xff
                                    val valueData = u32(buffer, at + 16)
                                    val value = if (rawIndex != NO_INDEX) strings.getOrNull(rawIndex)
                                        else when (valueType) {
                                            0x03 -> strings.getOrNull(valueData)
                                            0x10, 0x11 -> valueData.toString()
                                            0x12 -> (valueData != 0).toString()
                                            else -> valueData.toString()
                                        }
                                    if (value != null) attributes[attrName] = value
                                }
                            }
                            when (tag) {
                                "manifest" -> packageName = attributes["package"]
                                "uses-sdk" -> {
                                    minSdk = attributes["minSdkVersion"]?.toIntOrNull()
                                    targetSdk = attributes["targetSdkVersion"]?.toIntOrNull()
                                }
                                "uses-permission", "uses-permission-sdk-23", "uses-permission-sdk-m" -> {
                                    attributes["name"]?.let { permissions += it to null }
                                }
                                "permission" -> {
                                    attributes["name"]?.let { permissions += it to attributes["protectionLevel"] }
                                }
                                "activity", "activity-alias", "service", "receiver", "provider" -> {
                                    currentComponent = attributes["name"]?.let { name ->
                                        if (name.startsWith(".")) packageName.orEmpty() + name
                                        else if (!name.contains('.')) packageName.orEmpty() + "." + name
                                        else name
                                    }
                                }
                                "intent-filter" -> if (currentComponent != null) {
                                    inFilter = true; actions = mutableListOf(); categories = mutableListOf()
                                    schemes = mutableListOf(); hosts = mutableListOf()
                                    autoVerify = attributes["autoVerify"] == "true"
                                }
                                "action" -> if (inFilter) attributes["name"]?.let { actions += it }
                                "category" -> if (inFilter) attributes["name"]?.let { categories += it }
                                "data" -> if (inFilter) {
                                    attributes["scheme"]?.let { schemes += it }
                                    attributes["host"]?.let { hosts += it }
                                }
                            }
                        }
                    }
                    RES_XML_END_ELEMENT_TYPE -> {
                        if (headerSize >= 16 && chunkSize >= 24) {
                            val tag = strings.getOrNull(u32(buffer, offset + 20)).orEmpty()
                            if (tag == "intent-filter" && inFilter && currentComponent != null) {
                                filters += ManifestIntentFilter(currentComponent!!, actions.toList(),
                                    categories.toList(), schemes.toList(), hosts.toList(), autoVerify)
                                inFilter = false
                            }
                            if (tag in setOf("activity", "activity-alias", "service", "receiver", "provider")) currentComponent = null
                        }
                    }
                }
                offset += chunkSize
            }
        } catch (error: Exception) {
            warnings += (error.message ?: "Binary manifest parse error").take(180)
        }
        return BinaryManifestInspection(packageName, minSdk, targetSdk, permissions, filters, warnings)
    }

    private fun u16(buffer: ByteBuffer, offset: Int): Int =
        buffer.getShort(offset).toInt() and 0xffff

    private fun u32(buffer: ByteBuffer, offset: Int): Int =
        buffer.getInt(offset)

    private fun readStringPool(buffer: ByteBuffer, offset: Int, chunkSize: Int): List<String> {
        if (chunkSize < 28) return emptyList()
        val count = u32(buffer, offset + 8).coerceIn(0, 20_000)
        val flags = u32(buffer, offset + 16)
        val stringsStart = u32(buffer, offset + 20)
        val headerSize = u16(buffer, offset + 2)
        val utf8 = flags and 0x100 != 0
        val result = ArrayList<String>(count)
        for (i in 0 until count) {
            val indexAt = offset + headerSize + i * 4
            if (indexAt + 4 > offset + chunkSize) break
            val relative = buffer.getInt(indexAt)
            val startLong = offset.toLong() + stringsStart.toLong() + relative.toLong()
            val endLong = offset.toLong() + chunkSize.toLong()
            if (startLong < offset.toLong() || startLong >= endLong || startLong > Int.MAX_VALUE) { result += ""; continue }
            val start = startLong.toInt()
            result += if (utf8) readUtf8(buffer, start, endLong.toInt()) else readUtf16(buffer, start, endLong.toInt())
        }
        return result
    }

    private fun readUtf8(buffer: ByteBuffer, start: Int, end: Int): String {
        var p = start
        fun length(): Int {
            val first = buffer.get(p++).toInt() and 0xff
            if (first and 0x80 == 0) return first
            return ((first and 0x7f) shl 8) or (buffer.get(p++).toInt() and 0xff)
        }
        length()
        val byteCount = length().coerceIn(0, end - p)
        val bytes = ByteArray(byteCount)
        buffer.position(p); buffer.get(bytes)
        return bytes.toString(Charsets.UTF_8)
    }

    private fun readUtf16(buffer: ByteBuffer, start: Int, end: Int): String {
        var p = start
        var length = buffer.getShort(p).toInt() and 0xffff
        p += 2
        if (length and 0x8000 != 0) {
            length = ((length and 0x7fff) shl 16) or (buffer.getShort(p).toInt() and 0xffff)
            p += 2
        }
        val byteCount = (length.toLong() * 2).coerceAtMost((end - p).toLong()).toInt()
        val bytes = ByteArray(byteCount)
        buffer.position(p); buffer.get(bytes)
        return bytes.toString(Charsets.UTF_16LE)
    }
}
