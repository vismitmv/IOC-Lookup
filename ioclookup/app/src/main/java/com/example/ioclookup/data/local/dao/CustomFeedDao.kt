package com.example.ioclookup.data.local.dao

import androidx.room.*
import com.example.ioclookup.data.local.entity.CustomFeedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomFeedDao {
    @Query("SELECT * FROM custom_feeds ORDER BY id DESC")
    fun getAllFeedsFlow(): Flow<List<CustomFeedEntity>>

    @Query("SELECT * FROM custom_feeds WHERE isEnabled = 1")
    suspend fun getActiveFeeds(): List<CustomFeedEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeed(feed: CustomFeedEntity): Long

    @Update
    suspend fun updateFeed(feed: CustomFeedEntity)

    @Delete
    suspend fun deleteFeed(feed: CustomFeedEntity)

    @Query("DELETE FROM custom_feeds WHERE id = :id")
    suspend fun deleteById(id: Int)
}
