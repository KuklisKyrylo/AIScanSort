package com.smartscan.ai.domain.repository

import android.app.Activity
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    fun observeIsPro(): Flow<Boolean>
    fun currentIsPro(): Boolean
    fun refreshPurchases()
    fun launchProPurchase(activity: Activity): Boolean
}
