package com.example.ioclookup.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ioclookup.data.local.dao.LookupDao
import com.example.ioclookup.data.local.entity.LookupEntity

@Database(
    entities = [LookupEntity::class],
    version = 1,
    exportSchema = false
)
abstract class IocDatabase : RoomDatabase() {
    abstract fun lookupDao(): LookupDao
}
