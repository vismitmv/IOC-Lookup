package com.example.ioclookup.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_feeds")
data class CustomFeedEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val urlTemplate: String, // e.g. "https://myfeed.com/api/{ioc}"
    val headerName: String? = null,
    val headerValue: String? = null,
    val jsonPathMalicious: String = "malicious", // field name to check for boolean or positive score
    val isEnabled: Boolean = true
)
