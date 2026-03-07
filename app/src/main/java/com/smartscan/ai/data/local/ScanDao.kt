package com.smartscan.ai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {

    @Query("SELECT * FROM scans ORDER BY scannedAtEpochMillis DESC")
    fun observeAllScans(): Flow<List<ScanEntity>>

    @Query(
        """
        SELECT scans.*
        FROM scans
        JOIN scans_fts ON scans.rowid = scans_fts.rowid
        WHERE scans_fts MATCH :ftsQuery
        ORDER BY scans.scannedAtEpochMillis DESC
        """
    )
    fun observeSearchScans(ftsQuery: String): Flow<List<ScanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(scanEntity: ScanEntity): Long

    @Query("DELETE FROM scans WHERE id = :scanId")
    suspend fun deleteById(scanId: Long)

    @Query("DELETE FROM scans")
    suspend fun deleteAll()

    @Query("SELECT COUNT(id) FROM scans")
    fun observeScannedCount(): Flow<Int>

    @Query("SELECT COUNT(id) FROM scans")
    suspend fun getScannedCount(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM scans WHERE uri = :uri LIMIT 1)")
    suspend fun hasScannedUri(uri: String): Boolean
}
