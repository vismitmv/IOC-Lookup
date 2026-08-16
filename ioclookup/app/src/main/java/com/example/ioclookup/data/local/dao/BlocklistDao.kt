package com.example.ioclookup.data.local.dao

import androidx.room.*
import com.example.ioclookup.data.local.entity.BlocklistEntryEntity
import com.example.ioclookup.data.local.entity.BlocklistFeedEntity
import kotlinx.coroutines.flow.Flow

data class BlocklistMatchResult(
    val feedName: String,
    val matchedIoc: String
)

@Dao
interface BlocklistDao {
    @Query("SELECT * FROM blocklist_feeds ORDER BY id DESC")
    fun getAllFeedsFlow(): Flow<List<BlocklistFeedEntity>>

    @Query("SELECT * FROM blocklist_feeds WHERE isEnabled = 1")
    suspend fun getActiveFeeds(): List<BlocklistFeedEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeed(feed: BlocklistFeedEntity): Long

    @Update
    suspend fun updateFeed(feed: BlocklistFeedEntity)

    @Delete
    suspend fun deleteFeed(feed: BlocklistFeedEntity)

    @Query("DELETE FROM blocklist_entries WHERE feedId = :feedId")
    suspend fun deleteEntriesForFeed(feedId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<BlocklistEntryEntity>)

    @Transaction
    suspend fun replaceFeedEntries(feedId: Long, rawEntries: List<String>) {
        deleteEntriesForFeed(feedId)
        rawEntries.chunked(1000).forEach { chunk ->
            val entities = chunk.map { ioc -> BlocklistEntryEntity(feedId = feedId, ioc = ioc) }
            insertEntries(entities)
        }
    }

    @Query("""
        SELECT f.name as feedName, e.ioc as matchedIoc 
        FROM blocklist_entries e 
        JOIN blocklist_feeds f ON e.feedId = f.id 
        WHERE f.isEnabled = 1 AND (e.ioc = :ioc OR e.ioc = :iocWithProtocol OR e.ioc = :iocWithoutProtocol)
    """)
    suspend fun checkIoc(ioc: String, iocWithProtocol: String, iocWithoutProtocol: String): List<BlocklistMatchResult>
}
