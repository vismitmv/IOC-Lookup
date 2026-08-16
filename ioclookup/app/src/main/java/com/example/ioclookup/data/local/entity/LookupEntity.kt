package com.example.ioclookup.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lookups")
data class LookupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val ioc: String,
    val iocType: String,            // IocType.name
    val verdict: String,            // Verdict.name
    val sourcesJson: String,        // Gson-serialized Map<String, SourceResult>
    val timestamp: Long,
    val isBookmarked: Boolean = false,
    val bookmarkNote: String = ""
)
