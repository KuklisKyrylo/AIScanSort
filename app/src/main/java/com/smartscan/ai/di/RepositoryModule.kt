package com.smartscan.ai.di

import com.smartscan.ai.data.repository.ScanRepositoryImpl
import com.smartscan.ai.domain.repository.ScanRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindScanRepository(
        scanRepositoryImpl: ScanRepositoryImpl
    ): ScanRepository
}

