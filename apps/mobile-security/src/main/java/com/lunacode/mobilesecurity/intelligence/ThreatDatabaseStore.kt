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
    private val trustStore = ThreatKeyTrustStore(context)

    fun currentVersion(): Long = preferences.getLong("version", 0L)

    fun loadVerified(): VerifiedThreatDatabase? {
        val trustedKeys = trustStore.publicKeys()
        if (!file.isFile || trustedKeys.isEmpty()) return null
        return runCatching {
            val encrypted = file.readText(StandardCharsets.UTF_8)
            val plain = LocalPayloadCipher().decrypt(encrypted)
            val savedVersion = currentVersion()
            val database = OfflineThreatDatabaseVerifier.verify(
                plain,
                trustedKeys,
                minimumVersionExclusive = (savedVersion - 1L).coerceAtLeast(0L)
            )
            if (database.version > savedVersion) {
                preferences.edit().putLong("version", database.version).apply()
            }
            database
        }.getOrNull()
    }

    fun importFrom(uri: Uri): VerifiedThreatDatabase {
        val trustedKeys = trustStore.publicKeys()
        require(trustedKeys.isNotEmpty()) {
            "Сначала закрепите проверенный публичный ключ издателя. Неподписанная или неизвестная база не будет принята."
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
            plain,
            trustedKeys,
            minimumVersionExclusive = currentVersion()
        )
        val encrypted = LocalPayloadCipher().encrypt(plain)
        val atomic = AtomicFile(file)
        val stream = atomic.startWrite()
        try {
            stream.write(encrypted.toByteArray(StandardCharsets.UTF_8))
            atomic.finishWrite(stream)
            check(preferences.edit().putLong("version", verified.version).commit()) {
                "Не удалось сохранить версию базы индикаторов."
            }
        } catch (error: Throwable) {
            atomic.failWrite(stream)
            throw error
        }
        return verified
    }

    fun statusDescription(): String {
        val fingerprint = trustStore.currentFingerprint()
        if (trustStore.publicKeys().isEmpty()) {
            return "Корень доверия не настроен. Импорт заблокирован до закрепления публичного ключа издателя."
        }
        val database = loadVerified()
        return when {
            database != null ->
                "Подпись проверена · версия ${database.version} · индикаторов ${database.indicators.size} · ключ ${database.keyId} · SHA-256 ${fingerprint.orEmpty()}"
            file.exists() -> "Сохранённая база не прошла проверку подписи, версии или целостности."
            else -> "Публичный ключ закреплён · SHA-256 ${fingerprint.orEmpty()} · подписанная офлайн-база ещё не импортирована."
        }
    }
}
