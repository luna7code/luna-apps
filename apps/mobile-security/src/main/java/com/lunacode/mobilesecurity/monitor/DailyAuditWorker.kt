package com.lunacode.mobilesecurity.monitor

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lunacode.mobilesecurity.data.ProSecDatabase
import com.lunacode.mobilesecurity.data.ScanRecord
import com.lunacode.mobilesecurity.intelligence.ChangeSentinel
import com.lunacode.mobilesecurity.scanner.DeviceAuditScanner
import com.lunacode.mobilesecurity.security.LocalPayloadCipher
import kotlinx.coroutines.CancellationException

class DailyAuditWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            val dao = ProSecDatabase.get(applicationContext).scanDao()
            val cipher = LocalPayloadCipher()
            val previousPayload = dao.latest()?.let { record ->
                runCatching { cipher.decrypt(record.encryptedPayload) }.getOrNull()
            }
            val scanner = DeviceAuditScanner(applicationContext)
            val findings = scanner.scan()
            val snapshot = ChangeSentinel.enrich(previousPayload, scanner.toJson(findings))
            val encrypted = cipher.encrypt(snapshot)
            dao.insert(
                ScanRecord(
                    scannedAt = System.currentTimeMillis(),
                    encryptedPayload = encrypted
                )
            )
            dao.pruneToLimit(90)
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
