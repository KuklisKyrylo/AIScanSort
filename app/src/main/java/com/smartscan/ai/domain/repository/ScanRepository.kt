package com.smartscan.ai.domain.repository

import com.smartscan.ai.domain.model.ScannedImage
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    fun observeScannedImages(searchQuery: String = ""): Flow<List<ScannedImage>>

    fun observeScannedCount(): Flow<Int>

    suspend fun getScannedCount(): Int

    suspend fun hasScannedUri(uri: String): Boolean

    suspend fun upsertScannedImage(scannedImage: ScannedImage): Long

    suspend fun deleteScannedImage(imageId: Long)

    suspend fun deleteAllScannedImages()

    suspend fun deleteScannedImagesAfter(timestamp: Long): Int
}
