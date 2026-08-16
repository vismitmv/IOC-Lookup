package com.example.ioclookup.domain

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.ioclookup.data.BanlistSyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    fun schedule(
        feedId: String,
        feedUrl: String,
        intervalHours: Long,
        wifiOnly: Boolean
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (wifiOnly) NetworkType.UNMETERED
                else NetworkType.CONNECTED
            )
            .build()

        val request = PeriodicWorkRequestBuilder<BanlistSyncWorker>(
            repeatInterval = intervalHours,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    "feed_id" to feedId,
                    "feed_url" to feedUrl
                )
            )
            .addTag(feedId)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "banlist_sync_$feedId",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(feedId: String) {
        workManager.cancelUniqueWork("banlist_sync_$feedId")
    }

    fun scheduleImmediate(feedId: String, feedUrl: String) {
        val request = OneTimeWorkRequestBuilder<BanlistSyncWorker>()
            .setInputData(
                workDataOf(
                    "feed_id" to feedId,
                    "feed_url" to feedUrl
                )
            )
            .addTag(feedId)
            .build()
        workManager.enqueue(request)
    }
}
