package com.smartscan.ai.di

import com.smartscan.ai.data.account.GoogleAccountIdentityProvider
import com.smartscan.ai.data.quota.LocalAccountScanQuotaRepository
import com.smartscan.ai.domain.repository.AccountIdentityProvider
import com.smartscan.ai.domain.repository.ScanQuotaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class QuotaModule {

    @Binds
    @Singleton
    abstract fun bindAccountIdentityProvider(
        provider: GoogleAccountIdentityProvider
    ): AccountIdentityProvider

    @Binds
    @Singleton
    abstract fun bindScanQuotaRepository(
        repository: LocalAccountScanQuotaRepository
    ): ScanQuotaRepository
}
