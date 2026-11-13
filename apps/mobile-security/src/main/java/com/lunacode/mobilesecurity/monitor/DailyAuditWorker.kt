package com.lunacode.mobilesecurity.monitor

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lunacode.mobilesecurity.data.ProSecDatabase
import com.lunacode.mobilesecurity.data.ScanRecord
import com.lunacode.mobilesecurity.scanner.DeviceAuditScanner
import com.lunacode.mobilesecurity.security.LocalPayloadCipher
import kotlinx.coroutines.CancellationException

class DailyAuditWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            val scanner = DeviceAuditScanner(applicationContext)
            val findings = scanner.scan()
            val encrypted = LocalPayloadCipher().encrypt(scanner.toJson(findings))
            ProSecDatabase.get(applicationContext).scanDao().insert(
                ScanRecord(scannedAt = System.currentTimeMillis(), encryptedPayload = encrypted)
            )
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
