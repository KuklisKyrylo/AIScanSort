package com.smartscan.ai.domain.repository

import android.app.Activity
import com.smartscan.ai.domain.model.SubscriptionType
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    fun observeIsPremium(): Flow<Boolean>
    fun observeSubscriptionType(): Flow<SubscriptionType>
    fun observeBillingInProgress(): Flow<Boolean>
    fun observeBillingMessage(): Flow<String>
    fun observeMonthlyPrice(): Flow<String>
    fun observeLifetimePrice(): Flow<String>

    fun currentIsPremium(): Boolean
    fun currentSubscriptionType(): SubscriptionType

    fun refreshPurchases()
    fun restorePurchases()

    fun launchMonthlyPurchase(activity: Activity): Boolean
    fun launchLifetimePurchase(activity: Activity): Boolean
}
