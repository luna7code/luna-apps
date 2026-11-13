package com.lunacode.mobilesecurity

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lunacode.mobilesecurity.monitor.DailyAuditWorker
import java.util.concurrent.TimeUnit

class ProSecApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val request = PeriodicWorkRequestBuilder<DailyAuditWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "pro-sec-daily-audit",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
