package com.lunacode.mobilesecurity.intelligence

import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

data class DexScanResult(
    val signals: List<String>,
    val dexFilesScanned: Int,
    val bytesScanned: Long,
    val truncated: Boolean,
    val durationMs: Long = 0L,
    val limitReason: String? = null
)

object DexIndicatorScanner {
    private data class Rule(val signature: String, val label: String)

    private val rules = listOf(
        Rule("Landroid/accessibilityservice/AccessibilityService;", "Accessibility service API"),
        Rule("Landroid/view/accessibility/AccessibilityNodeInfo;", "Accessibility node traversal API"),
        Rule("Landroid/service/notification/NotificationListenerService;", "Notification listener API"),
        Rule("Landroid/app/admin/DevicePolicyManager;", "Device administrator API"),
        Rule("Landroid/media/projection/MediaProjectionManager;", "Screen capture / MediaProjection API"),
        Rule("Landroid/app/usage/UsageStatsManager;", "Application usage history API"),
        Rule("Ldalvik/system/DexClassLoader;", "Dynamic DEX loading API"),
        Rule("Ldalvik/system/PathClassLoader;", "Path-based class loading API"),
        Rule("Ldalvik/system/InMemoryDexClassLoader;", "In-memory DEX loading API"),
        Rule("Landroid/telephony/SmsManager;", "SMS sending API"),
        Rule("Landroid/provider/Telephony\$Sms;", "SMS database API"),
        Rule("Landroid/provider/CallLog;", "Call log API"),
        Rule("Landroid/provider/ContactsContract;", "Contacts database API"),
        Rule("Landroid/content/pm/PackageInstaller;", "Package installation API"),
        Rule("Landroid/net/VpnService;", "VPN service API"),
        Rule("Landroid/view/WindowManager\$LayoutParams;", "System overlay window API"),
        Rule("Landroid/content/ClipboardManager;", "Clipboard API"),
        Rule("Landroid/provider/Settings\$Secure;", "Secure settings API"),
        Rule("Landroid/hardware/camera2/CameraManager;", "Camera API"),
        Rule("Landroid/location/LocationManager;", "Location API"),
        Rule("Landroid/hardware/SensorManager;", "Device sensor API"),
        Rule("Landroid/app/NotificationManager;", "Notification management API"),
        Rule("Landroid/content/BroadcastReceiver;", "Broadcast receiver API")
    )

    private const val MAX_DEX_FILES = 24
    private const val MAX_TOTAL_DEX_BYTES = 96L * 1024L * 1024L
    private const val BUFFER_SIZE = 16 * 1024

    fun scan(apkFile: File): DexScanResult {
        val started = System.nanoTime()
        val found = linkedSetOf<String>()
        var totalBytes = 0L
        var dexCount = 0
        var truncated = false
        var limitReason: String? = null
        ZipFile(apkFile).use { zip ->
            val allEntries = zip.entries().asSequence()
                .filter { !it.isDirectory && it.name.endsWith(".dex", ignoreCase = true) }
                .toList()
            val entries = allEntries.take(MAX_DEX_FILES)
            if (allEntries.size > MAX_DEX_FILES) {
                truncated = true
                limitReason = "Превышен лимит числа DEX-файлов ($MAX_DEX_FILES)."
            }
            for (entry in entries) {
                if (totalBytes >= MAX_TOTAL_DEX_BYTES) {
                    truncated = true
                    limitReason = "Достигнут лимит объёма DEX-данных (96 МБ)."
                    break
                }
                zip.getInputStream(entry).use { input ->
                    val remaining = MAX_TOTAL_DEX_BYTES - totalBytes
                    val result = scanStream(input, remaining)
                    found += result.first
                    totalBytes += result.second
                    dexCount++
                    if (entry.size >= 0L && entry.size > result.second) {
                        truncated = true
                        limitReason = "Чтение остановлено по лимиту объёма DEX-данных."
                    }
                }
            }
        }
        return DexScanResult(
            found.toList(), dexCount, totalBytes, truncated,
            ((System.nanoTime() - started) / 1_000_000L).coerceAtLeast(0L),
            limitReason
        )
    }

    internal fun scanStream(input: InputStream, byteLimit: Long): Pair<Set<String>, Long> {
        val found = linkedSetOf<String>()
        val maxPatternLength = rules.maxOf { it.signature.length }
        var carry = ""
        var total = 0L
        val buffer = ByteArray(BUFFER_SIZE)
        while (total < byteLimit) {
            val allowed = minOf(buffer.size.toLong(), byteLimit - total).toInt()
            val read = input.read(buffer, 0, allowed)
            if (read < 0) break
            if (read == 0) continue
            total += read
            val chunk = String(buffer, 0, read, Charsets.ISO_8859_1)
            val combined = carry + chunk
            rules.forEach { rule -> if (combined.contains(rule.signature)) found += rule.label }
            carry = if (combined.length > maxPatternLength - 1) combined.takeLast(maxPatternLength - 1) else combined
        }
        return found to total
    }
}
