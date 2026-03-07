package com.smartscan.ai.data.quota

import com.smartscan.ai.data.preferences.PreferencesManager
import com.smartscan.ai.domain.repository.AccountIdentityProvider
import com.smartscan.ai.domain.repository.ScanQuotaRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalAccountScanQuotaRepository @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val accountIdentityProvider: AccountIdentityProvider
) : ScanQuotaRepository {

    override suspend fun refreshFromServer() {
        // No-op: purely local implementation for Play Market without backend
    }

    override suspend fun getUsedScans(): Int {
        return preferencesManager.getScanCountNow()
    }

    override suspend fun incrementAndGet(): Int {
        preferencesManager.addScanCount()
        return preferencesManager.getScanCountNow()
    }
}

