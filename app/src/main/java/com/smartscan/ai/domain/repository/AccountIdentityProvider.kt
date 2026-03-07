package com.smartscan.ai.domain.repository

interface AccountIdentityProvider {
    suspend fun getStableAccountId(): String
}

