package com.example.ioclookup.data.local.dao

import androidx.room.*
import com.example.ioclookup.data.local.entity.LookupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LookupDao {

    @Query("SELECT * FROM lookups ORDER BY timestamp DESC")
    fun getAllLookups(): Flow<List<LookupEntity>>

    @Query("SELECT * FROM lookups WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarks(): Flow<List<LookupEntity>>

    @Query("""
        SELECT * FROM lookups
        WHERE (:query = '' OR ioc LIKE '%' || :query || '%')
        AND (:type = '' OR iocType = :type)
        AND (:verdict = '' OR verdict = :verdict)
        ORDER BY timestamp DESC
    """)
    fun searchLookups(query: String, type: String, verdict: String): Flow<List<LookupEntity>>

    @Query("SELECT * FROM lookups WHERE ioc = :ioc ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestByIoc(ioc: String): LookupEntity?

    @Query("SELECT * FROM lookups WHERE id = :id")
    suspend fun getById(id: Long): LookupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LookupEntity): Long

    @Delete
    suspend fun delete(entity: LookupEntity)

    @Query("DELETE FROM lookups WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM lookups")
    suspend fun clearAll()

    @Query("UPDATE lookups SET isBookmarked = :isBookmarked, bookmarkNote = :note WHERE id = :id")
    suspend fun updateBookmark(id: Long, isBookmarked: Boolean, note: String)

    @Query("SELECT COUNT(*) FROM lookups")
    suspend fun count(): Int
}
