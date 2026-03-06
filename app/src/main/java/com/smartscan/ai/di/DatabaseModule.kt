package com.smartscan.ai.di

import android.content.Context
import androidx.room.Room
import com.smartscan.ai.data.local.ScanDao
import com.smartscan.ai.data.local.SmartScanDatabase
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): SmartScanDatabase {
        return Room.databaseBuilder(
            context,
            SmartScanDatabase::class.java,
            SmartScanDatabase.DB_NAME
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    @Provides
    @Singleton
    fun provideScanDao(database: SmartScanDatabase): ScanDao = database.scanDao()
}

