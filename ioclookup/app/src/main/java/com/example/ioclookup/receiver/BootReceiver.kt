package com.example.ioclookup.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.ioclookup.data.local.dao.BlocklistDao
import com.example.ioclookup.data.security.SecurePreferences
import com.example.ioclookup.domain.SyncScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var blocklistDao: BlocklistDao

    @Inject
    lateinit var securePreferences: SecurePreferences

    @Inject
    lateinit var syncScheduler: SyncScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeFeeds = blocklistDao.getActiveFeeds()
                    for (feed in activeFeeds) {
                        val feedId = feed.id.toString()
                        val autoSyncEnabled = securePreferences.getSyncEnabled(feedId)
                        if (autoSyncEnabled) {
                            val interval = securePreferences.getSyncInterval(feedId)
                            val wifiOnly = securePreferences.getSyncWifiOnly(feedId)
                            syncScheduler.schedule(feedId, feed.feedUrl, interval, wifiOnly)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
