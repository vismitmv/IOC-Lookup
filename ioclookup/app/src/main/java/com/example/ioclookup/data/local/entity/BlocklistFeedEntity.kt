package com.example.ioclookup.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocklist_feeds")
data class BlocklistFeedEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val feedUrl: String,
    val entryCount: Int = 0,
    val lastSyncedAt: Long = 0,
    val isEnabled: Boolean = true
)
