package com.smartscan.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ScanEntity::class,
        ScanFtsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class SmartScanDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao

    companion object {
        const val DB_NAME: String = "smartscan.db"
    }
}

