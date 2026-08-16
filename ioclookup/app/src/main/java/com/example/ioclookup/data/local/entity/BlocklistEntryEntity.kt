package com.example.ioclookup.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "blocklist_entries",
    indices = [
        Index(value = ["ioc"]),
        Index(value = ["feedId"])
    ]
)
data class BlocklistEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val feedId: Long,
    val ioc: String
)
