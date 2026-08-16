package com.example.ioclookup.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ioclookup.data.local.dao.LookupDao
import com.example.ioclookup.data.local.entity.LookupEntity

import com.example.ioclookup.data.local.dao.CustomFeedDao
import com.example.ioclookup.data.local.entity.CustomFeedEntity

@Database(
    entities = [LookupEntity::class, CustomFeedEntity::class],
    version = 2,
    exportSchema = false
)
abstract class IocDatabase : RoomDatabase() {
    abstract fun lookupDao(): LookupDao
    abstract fun customFeedDao(): CustomFeedDao
}
