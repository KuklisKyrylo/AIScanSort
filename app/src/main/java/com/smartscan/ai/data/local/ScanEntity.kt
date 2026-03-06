package com.smartscan.ai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val uri: String,
    val extractedText: String,
    val tagsSerialized: String,
    val scannedAtEpochMillis: Long,
    val status: String
)

