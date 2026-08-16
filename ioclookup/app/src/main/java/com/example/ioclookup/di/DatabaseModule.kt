package com.example.ioclookup.di

import android.content.Context
import androidx.room.Room
import com.example.ioclookup.data.local.IocDatabase
import com.example.ioclookup.data.local.dao.LookupDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): IocDatabase =
        Room.databaseBuilder(context, IocDatabase::class.java, "ioc_lookup.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideLookupDao(db: IocDatabase): LookupDao = db.lookupDao()

    @Provides
    fun provideCustomFeedDao(db: IocDatabase): com.example.ioclookup.data.local.dao.CustomFeedDao = db.customFeedDao()

    @Provides
    fun provideBlocklistDao(db: IocDatabase): com.example.ioclookup.data.local.dao.BlocklistDao = db.blocklistDao()
}
