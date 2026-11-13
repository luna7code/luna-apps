package com.lunacode.mobilesecurity.monitor

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lunacode.mobilesecurity.data.ProSecDatabase
import com.lunacode.mobilesecurity.data.ScanRecord
import com.lunacode.mobilesecurity.intelligence.ChangeSentinel
import com.lunacode.mobilesecurity.scanner.DeviceAuditScanner
import com.lunacode.mobilesecurity.security.LocalEvidenceSeal
import com.lunacode.mobilesecurity.security.LocalPayloadCipher
import com.lunacode.mobilesecurity.security.ReportEnvelopeCodec
import kotlinx.coroutines.CancellationException

class DailyAuditWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            val dao = ProSecDatabase.get(applicationContext).scanDao()
            val cipher = LocalPayloadCipher()
            val previousStored = dao.latest()?.let { record ->
                runCatching { cipher.decrypt(record.encryptedPayload) }.getOrNull()
            }
            val previousEnvelope = previousStored?.let { ReportEnvelopeCodec.decode(it) }
            val previousReport = previousEnvelope?.report
            val previousTag = previousEnvelope?.chainTag.orEmpty()

            val scanner = DeviceAuditScanner(applicationContext)
            val findings = scanner.scan()
            val snapshot = ChangeSentinel.enrich(previousReport, scanner.toJson(findings))
            val tag = LocalEvidenceSeal.sign(
                ReportEnvelopeCodec.material(previousTag, snapshot)
            )
            val sealedPayload = ReportEnvelopeCodec.encode(previousTag, snapshot, tag)
            val encrypted = cipher.encrypt(sealedPayload)

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
