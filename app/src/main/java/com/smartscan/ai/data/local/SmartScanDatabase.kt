package com.smartscan.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ScanEntity::class,
        ScanFtsEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class SmartScanDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao

    companion object {
        const val DB_NAME: String = "smartscan.db"

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scans ADD COLUMN photoCreatedAtEpochMillis INTEGER")
            }
        }
    }
}
