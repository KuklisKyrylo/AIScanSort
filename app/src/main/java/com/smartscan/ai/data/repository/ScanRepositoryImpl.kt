package com.smartscan.ai.data.repository

import com.smartscan.ai.data.local.ScanDao
import com.smartscan.ai.data.local.toDomain
import com.smartscan.ai.data.local.toEntity
import com.smartscan.ai.domain.model.ScannedImage
import com.smartscan.ai.domain.repository.ScanRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ScanRepositoryImpl @Inject constructor(
    private val scanDao: ScanDao
) : ScanRepository {

    override fun observeScannedImages(searchQuery: String): Flow<List<ScannedImage>> {
        return if (searchQuery.isBlank()) {
            scanDao.observeAllScans().map { entities -> entities.map { it.toDomain() } }
        } else {
            scanDao.observeSearchScans(searchQuery.toFtsPrefixQuery())
                .map { entities -> entities.map { it.toDomain() } }
        }
    }

    override fun observeScannedCount(): Flow<Int> = scanDao.observeScannedCount()

    override suspend fun getScannedCount(): Int = scanDao.getScannedCount()

    override suspend fun hasScannedUri(uri: String): Boolean = scanDao.hasScannedUri(uri)

    override suspend fun upsertScannedImage(scannedImage: ScannedImage): Long {
        return scanDao.upsert(scannedImage.toEntity())
    }

    override suspend fun deleteScannedImage(imageId: Long) {
        scanDao.deleteById(imageId)
    }

    private fun String.toFtsPrefixQuery(): String {
        return trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(separator = " AND ") { token -> "${token.replace("\"", "")}*" }
    }
}
