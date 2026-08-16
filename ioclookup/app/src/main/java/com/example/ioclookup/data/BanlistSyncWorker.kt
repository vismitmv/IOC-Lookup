package com.example.ioclookup.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ioclookup.data.local.dao.BlocklistDao
import com.example.ioclookup.data.local.entity.BlocklistEntryEntity
import com.example.ioclookup.data.security.SecurePreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader

@HiltWorker
class BanlistSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val blocklistDao: BlocklistDao,
    private val okHttpClient: OkHttpClient,
    private val securePreferences: SecurePreferences
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "banlist_sync_channel"
        const val NOTIFICATION_ID_OFFSET = 1000
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val feedIdStr = inputData.getString("feed_id")
        val feedUrl = inputData.getString("feed_url")

        if (feedIdStr == null || feedUrl == null) {
            return@withContext Result.failure()
        }

        val feedId = feedIdStr.toLongOrNull() ?: return@withContext Result.failure()

        try {
            val request = Request.Builder()
                .url(feedUrl)
                .header("User-Agent", "IOC-Lookup-Android/1.0")
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                showNotification("Threat feed sync failed — HTTP ${response.code}", feedId.toInt())
                return@withContext Result.retry()
            }

            val inputStream = response.body?.byteStream() ?: run {
                showNotification("Threat feed sync failed — Empty response", feedId.toInt())
                return@withContext Result.failure()
            }

            // Wipe existing entries for this feed
            blocklistDao.deleteEntriesForFeed(feedId)

            var entryCount = 0
            val batchSize = 1000
            val batch = mutableListOf<BlocklistEntryEntity>()

            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    val trimmed = line.trim()
                    if (trimmed.isNotBlank() && !trimmed.startsWith("#") && !trimmed.startsWith("//")) {
                        val normalized = trimmed.lowercase()
                        batch.add(BlocklistEntryEntity(feedId = feedId, ioc = normalized))
                        entryCount++

                        if (batch.size >= batchSize) {
                            blocklistDao.insertEntries(batch)
                            batch.clear()
                        }
                    }
                    line = reader.readLine()
                }
            }

            if (batch.isNotEmpty()) {
                blocklistDao.insertEntries(batch)
            }

            // Update feed table
            val activeFeeds = blocklistDao.getActiveFeeds()
            val feed = activeFeeds.find { it.id == feedId }
            if (feed != null) {
                val updatedFeed = feed.copy(
                    entryCount = entryCount,
                    lastSyncedAt = System.currentTimeMillis()
                )
                blocklistDao.updateFeed(updatedFeed)
            }

            // Update SecurePreferences timestamp for UI
            securePreferences.setSyncTimestamp(feedIdStr, System.currentTimeMillis())

            showNotification("Threat feeds updated — $entryCount entries indexed", feedId.toInt())
            Result.success()

        } catch (e: Exception) {
            showNotification("Threat feed sync failed — will retry at next interval", feedId.toInt())
            Result.retry()
        }
    }

    private fun showNotification(message: String, id: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Threat Feed Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent notifications for background threat feed synchronization"
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done) // Placeholder icon
            .setContentTitle("IOC Lookup")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_OFFSET + id, notification)
    }
}
