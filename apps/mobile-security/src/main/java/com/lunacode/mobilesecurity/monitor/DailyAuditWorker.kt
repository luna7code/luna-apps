package com.lunacode.mobilesecurity.monitor

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lunacode.mobilesecurity.data.ProSecDatabase
import com.lunacode.mobilesecurity.data.ScanRecord
import com.lunacode.mobilesecurity.intelligence.ChangeSentinel
import com.lunacode.mobilesecurity.scanner.AuditFinding
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
            val previousRecord = dao.latest()
            val previousStored = previousRecord?.let { record ->
                runCatching { cipher.decrypt(record.encryptedPayload) }.getOrNull()
            }
            val previousPayloadUnreadable = previousRecord != null && previousStored == null
            val previousEnvelope = previousStored?.let { ReportEnvelopeCodec.decode(it) }
            val previousVerified = previousEnvelope?.let { envelope ->
                if (!envelope.wrapped || envelope.chainTag == null) {
                    false
                } else {
                    runCatching {
                        LocalEvidenceSeal.verify(
                            ReportEnvelopeCodec.material(envelope.previousTag, envelope.report),
                            envelope.chainTag
                        )
                    }.getOrDefault(false)
                }
            }
            val previousReport = when {
                previousEnvelope == null -> null
                !previousEnvelope.wrapped -> previousEnvelope.report
                previousVerified == true -> previousEnvelope.report
                else -> null
            }
            val previousTag = if (previousVerified == true) {
                previousEnvelope?.chainTag.orEmpty()
            } else {
                ""
            }

            val scanner = DeviceAuditScanner(applicationContext)
            val findings = scanner.scan().toMutableList()
            if (previousPayloadUnreadable || (previousEnvelope?.wrapped == true && previousVerified != true)) {
                findings += AuditFinding(
                    id = "evidence-chain-invalid-previous",
                    title = "Предыдущая печать истории не подтверждена",
                    detail = "PRO SEC не использовал предыдущий отчёт как базу сравнения. Возможны изменение записи, повреждение данных или смена/потеря ключа Android Keystore. Новый снимок создаётся как новая точка отсчёта.",
                    severity = "HIGH",
                    source = "LocalEvidenceSeal"
                )
            }

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
