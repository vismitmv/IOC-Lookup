package com.example.ioclookup.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ioclookup.data.local.dao.LookupDao
import com.example.ioclookup.data.local.entity.LookupEntity

import com.example.ioclookup.data.local.dao.CustomFeedDao
import com.example.ioclookup.data.local.entity.CustomFeedEntity

import com.example.ioclookup.data.local.dao.BlocklistDao
import com.example.ioclookup.data.local.entity.BlocklistEntryEntity
import com.example.ioclookup.data.local.entity.BlocklistFeedEntity

@Database(
    entities = [LookupEntity::class, CustomFeedEntity::class, BlocklistFeedEntity::class, BlocklistEntryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class IocDatabase : RoomDatabase() {
    abstract fun lookupDao(): LookupDao
    abstract fun customFeedDao(): CustomFeedDao
    abstract fun blocklistDao(): BlocklistDao
}
