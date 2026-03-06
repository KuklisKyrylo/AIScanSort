package com.smartscan.ai.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = ScanEntity::class)
@Entity(tableName = "scans_fts")
data class ScanFtsEntity(
    @ColumnInfo(name = "extractedText")
    val extractedText: String,
    @ColumnInfo(name = "tagsSerialized")
    val tagsSerialized: String
)

