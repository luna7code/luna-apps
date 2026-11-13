package com.lunacode.mobilesecurity.intelligence

import android.content.Context
import android.net.Uri
import android.util.AtomicFile
import com.lunacode.mobilesecurity.security.LocalPayloadCipher
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets

class ThreatDatabaseStore(private val context: Context) {
    private val file = File(context.filesDir, "pro-sec-threat-db.enc")
    private val preferences = context.getSharedPreferences("pro_sec_threat_database", Context.MODE_PRIVATE)

    fun currentVersion(): Long = preferences.getLong("version", 0L)

    fun loadVerified(): VerifiedThreatDatabase? {
        if (!file.isFile || TrustedThreatKeys.publicKeysBase64.isEmpty()) return null
        return runCatching {
            val encrypted = file.readText(StandardCharsets.UTF_8)
            val plain = LocalPayloadCipher().decrypt(encrypted)
            val savedVersion = currentVersion()
            OfflineThreatDatabaseVerifier.verify(
                plain, TrustedThreatKeys.publicKeysBase64,
                minimumVersionExclusive = (savedVersion - 1L).coerceAtLeast(0L)
            )
        }.getOrNull()
    }

    fun importFrom(uri: Uri): VerifiedThreatDatabase {
        require(TrustedThreatKeys.publicKeysBase64.isNotEmpty()) {
            "Доверенный публичный ключ издателя ещё не настроен. Неподписанная или неизвестная база не будет принята."
        }
        val plain = context.contentResolver.openInputStream(uri)?.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8 * 1024)
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                total += read
                require(total <= 4 * 1024 * 1024) { "Файл базы превышает 4 МБ." }
                output.write(buffer, 0, read)
            }
            String(output.toByteArray(), StandardCharsets.UTF_8)
        } ?: throw IllegalArgumentException("Не удалось открыть выбранный файл.")
        val verified = OfflineThreatDatabaseVerifier.verify(
            plain, TrustedThreatKeys.publicKeysBase64, minimumVersionExclusive = currentVersion()
        )
        val encrypted = LocalPayloadCipher().encrypt(plain)
        val atomic = AtomicFile(file)
        val stream = atomic.startWrite()
        try {
            stream.write(encrypted.toByteArray(StandardCharsets.UTF_8))
            atomic.finishWrite(stream)
            preferences.edit().putLong("version", verified.version).apply()
        } catch (error: Throwable) {
            atomic.failWrite(stream)
            throw error
        }
        return verified
    }

    fun statusDescription(): String = when {
        TrustedThreatKeys.publicKeysBase64.isEmpty() ->
            "Корень доверия ещё не настроен. Импорт блокируется до добавления публичного ключа издателя."
        loadVerified() != null -> {
            val database = loadVerified()!!
            "Подпись проверена · версия ${database.version} · индикаторов ${database.indicators.size}"
        }
        file.exists() -> "Сохранённая база не прошла проверку подписи или целостности."
        else -> "Подписанная офлайн-база ещё не импортирована."
    }
}
