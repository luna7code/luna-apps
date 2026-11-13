package com.lunacode.mobilesecurity.intelligence

import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

data class DexScanResult(
    val signals: List<String>,
    val dexFilesScanned: Int,
    val bytesScanned: Long,
    val truncated: Boolean
)

object DexIndicatorScanner {
    private data class Rule(val signature: String, val label: String)

    private val rules = listOf(
        Rule("Landroid/accessibilityservice/AccessibilityService;", "Accessibility service API"),
        Rule("Landroid/service/notification/NotificationListenerService;", "Notification listener API"),
        Rule("Landroid/app/admin/DevicePolicyManager;", "Device administrator API"),
        Rule("Landroid/media/projection/MediaProjectionManager;", "Screen capture / MediaProjection API"),
        Rule("Ldalvik/system/DexClassLoader;", "Dynamic DEX loading API"),
        Rule("Ldalvik/system/PathClassLoader;", "Path-based class loading API"),
        Rule("Landroid/telephony/SmsManager;", "SMS sending API"),
        Rule("Landroid/content/pm/PackageInstaller;", "Package installation API"),
        Rule("Landroid/net/VpnService;", "VPN service API"),
        Rule("Landroid/view/WindowManager;", "Window / overlay management API"),
        Rule("Landroid/hardware/camera2/CameraManager;", "Camera API"),
        Rule("Landroid/location/LocationManager;", "Location API")
    )

    private const val MAX_DEX_FILES = 16
    private const val MAX_TOTAL_DEX_BYTES = 64L * 1024L * 1024L
    private const val BUFFER_SIZE = 16 * 1024

    fun scan(apkFile: File): DexScanResult {
        val found = linkedSetOf<String>()
        var totalBytes = 0L
        var dexCount = 0
        var truncated = false
        ZipFile(apkFile).use { zip ->
            val entries = zip.entries().asSequence()
                .filter { !it.isDirectory && it.name.endsWith(".dex", ignoreCase = true) }
                .take(MAX_DEX_FILES)
                .toList()
            for (entry in entries) {
                if (totalBytes >= MAX_TOTAL_DEX_BYTES) {
                    truncated = true
                    break
                }
                zip.getInputStream(entry).use { input ->
                    val result = scanStream(input, MAX_TOTAL_DEX_BYTES - totalBytes)
                    found += result.first
                    totalBytes += result.second
                    dexCount++
                    if (result.second >= MAX_TOTAL_DEX_BYTES) {
                        truncated = true
                        break
                    }
                    if (entry.size > result.second) truncated = true
                }
            }
        }
        return DexScanResult(found.toList(), dexCount, totalBytes, truncated)
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
            total += read
            val chunk = String(buffer, 0, read, Charsets.ISO_8859_1)
            val combined = carry + chunk
            rules.forEach { rule ->
                if (combined.contains(rule.signature)) found += rule.label
            }
            carry = if (combined.length > maxPatternLength - 1) {
                combined.takeLast(maxPatternLength - 1)
            } else {
                combined
            }
        }
        return found to total
    }
}
