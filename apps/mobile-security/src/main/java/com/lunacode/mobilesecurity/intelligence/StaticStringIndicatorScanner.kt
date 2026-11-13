package com.lunacode.mobilesecurity.intelligence

import java.io.File
import java.util.zip.ZipFile

data class StaticStringFinding(
    val category: String,
    val indicator: String,
    val occurrences: Int
)

data class StaticStringScanResult(
    val urls: List<String>,
    val findings: List<StaticStringFinding>,
    val entriesScanned: Int,
    val bytesScanned: Long,
    val truncated: Boolean
)

/**
 * Bounded, offline string inspection. It never loads classes or executes archive content.
 * Findings are capability/context hints only; a string can be dead code or documentation.
 */
object StaticStringIndicatorScanner {
    private data class Rule(val category: String, val token: String)

    private val rules = listOf(
        Rule("Удалённое управление / shell", "java/lang/Runtime"),
        Rule("Удалённое управление / shell", "java/lang/ProcessBuilder"),
        Rule("Удалённое управление / shell", "/system/bin/sh"),
        Rule("Удалённое управление / shell", "su -c"),
        Rule("Динамическая загрузка кода", "DexClassLoader"),
        Rule("Динамическая загрузка кода", "InMemoryDexClassLoader"),
        Rule("Доступность / управление интерфейсом", "AccessibilityService"),
        Rule("Доступность / управление интерфейсом", "performGlobalAction"),
        Rule("Перехват уведомлений", "NotificationListenerService"),
        Rule("SMS / телефонные данные", "SmsManager"),
        Rule("SMS / телефонные данные", "READ_SMS"),
        Rule("Запись / захват экрана", "MediaProjection"),
        Rule("Скрытие / overlay", "SYSTEM_ALERT_WINDOW"),
        Rule("Установка пакетов", "REQUEST_INSTALL_PACKAGES"),
        Rule("Сбор идентификаторов", "ANDROID_ID"),
        Rule("Сбор идентификаторов", "getImei"),
        Rule("Сбор идентификаторов", "getDeviceId"),
        Rule("Сетевой канал", "HttpURLConnection"),
        Rule("Сетевой канал", "okhttp3"),
        Rule("Сетевой канал", "Socket")
    )

    private val urlPattern = Regex(
        """(?i)https?://[A-Z0-9._~:/?#\\[\\]@!$&'()*+,;=%-]{4,}"""
    )
    private val candidateExtensions = setOf(
        "dex", "xml", "json", "txt", "html", "htm", "js", "properties", "cfg", "conf", "so"
    )
    private const val MAX_ENTRIES = 160
    private const val MAX_ENTRY_BYTES = 2L * 1024L * 1024L
    private const val MAX_TOTAL_BYTES = 32L * 1024L * 1024L
    private const val MAX_URLS = 80
    private const val MAX_FINDINGS = 40

    fun scan(apkFile: File): StaticStringScanResult {
        val urls = linkedSetOf<String>()
        val counts = linkedMapOf<Pair<String, String>, Int>()
        var entriesScanned = 0
        var totalBytes = 0L
        var truncated = false

        ZipFile(apkFile).use { zip ->
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.isDirectory) continue
                val extension = entry.name.substringAfterLast('.', "").lowercase()
                if (extension !in candidateExtensions) continue
                if (entriesScanned >= MAX_ENTRIES || totalBytes >= MAX_TOTAL_BYTES) {
                    truncated = true
                    break
                }
                if (entry.size > MAX_ENTRY_BYTES) {
                    truncated = true
                    continue
                }
                val remaining = minOf(MAX_ENTRY_BYTES, MAX_TOTAL_BYTES - totalBytes)
                val bytes = zip.getInputStream(entry).use { input ->
                    readBounded(input, remaining)
                }
                totalBytes += bytes.size
                entriesScanned++
                val text = bytes.toString(Charsets.ISO_8859_1)
                urlPattern.findAll(text).take(MAX_URLS).forEach { match ->
                    val value = match.value.trimEnd('.', ',', ';', ')', ']')
                    if (value.isNotBlank() && urls.size < MAX_URLS) urls += value
                }
                rules.forEach { rule ->
                    val occurrences = Regex(Regex.escape(rule.token), RegexOption.IGNORE_CASE)
                        .findAll(text).count()
                    if (occurrences > 0) {
                        val key = rule.category to rule.token
                        counts[key] = (counts[key] ?: 0) + occurrences
                    }
                }
                if (bytes.size.toLong() >= remaining && (entry.size < 0 || entry.size > bytes.size)) {
                    truncated = true
                }
            }
        }
        val findings = counts.entries
            .sortedWith(compareByDescending<Map.Entry<Pair<String, String>, Int>> { it.value }.thenBy { it.key.second })
            .take(MAX_FINDINGS)
            .map { StaticStringFinding(it.key.first, it.key.second, it.value) }
        return StaticStringScanResult(urls.toList(), findings, entriesScanned, totalBytes, truncated)
    }

    internal fun readBounded(input: java.io.InputStream, limit: Long): ByteArray {
        require(limit >= 0L)
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var remaining = limit
        while (remaining > 0) {
            val read = input.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
            if (read < 0) break
            if (read == 0) continue
            output.write(buffer, 0, read)
            remaining -= read
        }
        return output.toByteArray()
    }
}
