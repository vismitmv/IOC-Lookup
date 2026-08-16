package com.example.ioclookup.data.repository

import com.example.ioclookup.data.local.dao.BlocklistDao
import com.example.ioclookup.data.local.dao.BlocklistMatchResult
import com.example.ioclookup.data.local.entity.BlocklistFeedEntity
import com.example.ioclookup.domain.model.SourceResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlocklistRepository @Inject constructor(
    private val blocklistDao: BlocklistDao,
    private val okHttpClient: OkHttpClient
) {
    suspend fun syncFeed(feed: BlocklistFeedEntity): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(feed.feedUrl)
                .header("User-Agent", "IOC-Lookup-Android/1.0")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))

            val entries = body.lines()
                .map { it.trim() }
                .filter { line -> line.isNotBlank() && !line.startsWith("#") && !line.startsWith("//") }
                .map { line -> line.lowercase() }
                .distinct()

            blocklistDao.replaceFeedEntries(feed.id, entries)

            val updatedFeed = feed.copy(
                entryCount = entries.size,
                lastSyncedAt = System.currentTimeMillis()
            )
            blocklistDao.updateFeed(updatedFeed)

            Result.success(entries.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAllFeeds(): List<Pair<String, Result<Int>>> = withContext(Dispatchers.IO) {
        val active = blocklistDao.getActiveFeeds()
        active.map { feed ->
            feed.name to syncFeed(feed)
        }
    }

    suspend fun checkBlocklists(ioc: String): List<SourceResult.CustomFeed> = withContext(Dispatchers.IO) {
        val normalized = ioc.trim().lowercase()
        val withProtocol = if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) "http://$normalized" else normalized
        val withoutProtocol = normalized.removePrefix("https://").removePrefix("http://")

        val matches = blocklistDao.checkIoc(normalized, withProtocol, withoutProtocol)
        matches.map { match ->
            SourceResult.CustomFeed(
                feedName = "Banlist: ${match.feedName}",
                isFlagged = true,
                summary = "Matched indicator [${match.matchedIoc}] in ${match.feedName}",
                responseCode = 200,
                rawJson = null
            )
        }
    }
}
