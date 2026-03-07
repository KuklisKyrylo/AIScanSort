package com.smartscan.ai.domain.repository

interface ScanQuotaRepository {
    suspend fun refreshFromServer()
    suspend fun getUsedScans(): Int
    suspend fun incrementAndGet(): Int
}

