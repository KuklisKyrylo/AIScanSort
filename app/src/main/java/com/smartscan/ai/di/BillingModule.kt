package com.smartscan.ai.di

import com.smartscan.ai.data.billing.BillingManager
import com.smartscan.ai.domain.repository.BillingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {

    @Binds
    @Singleton
    abstract fun bindBillingRepository(
        billingManager: BillingManager
    ): BillingRepository
}

