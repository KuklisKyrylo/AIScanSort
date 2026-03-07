package com.smartscan.ai.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.smartscan.ai.domain.model.SubscriptionType
import com.smartscan.ai.domain.repository.BillingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext context: Context
) : BillingRepository, PurchasesUpdatedListener {

    private val isPremiumState = MutableStateFlow(false)
    private val subscriptionTypeState = MutableStateFlow(SubscriptionType.FREE)

    private var cachedLifetimeDetails: ProductDetails? = null
    private var cachedMonthlyDetails: ProductDetails? = null
    private var cachedMonthlyOfferToken: String? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        startConnection()
    }

    override fun observeIsPremium(): Flow<Boolean> = isPremiumState.asStateFlow()

    override fun observeSubscriptionType(): Flow<SubscriptionType> = subscriptionTypeState.asStateFlow()

    override fun currentIsPremium(): Boolean = isPremiumState.value

    override fun currentSubscriptionType(): SubscriptionType = subscriptionTypeState.value

    override fun refreshPurchases() {
        if (!billingClient.isReady) return

        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(inAppParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                updateTypeFromInApp(purchases)
            }
        }

        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(subsParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                updateTypeFromSubs(purchases)
            }
        }
    }

    override fun launchMonthlyPurchase(activity: Activity): Boolean {
        if (!billingClient.isReady) return false

        val details = cachedMonthlyDetails
        val offerToken = cachedMonthlyOfferToken
        if (details != null && !offerToken.isNullOrBlank()) {
            launchMonthlyBillingFlow(activity, details, offerToken)
            return true
        }

        queryMonthlyDetails { productDetails, token ->
            if (productDetails != null && !token.isNullOrBlank()) {
                cachedMonthlyDetails = productDetails
                cachedMonthlyOfferToken = token
                launchMonthlyBillingFlow(activity, productDetails, token)
            }
        }
        return true
    }

    override fun launchLifetimePurchase(activity: Activity): Boolean {
        if (!billingClient.isReady) return false

        val details = cachedLifetimeDetails
        if (details != null) {
            launchLifetimeBillingFlow(activity, details)
            return true
        }

        queryLifetimeDetails { productDetails ->
            if (productDetails != null) {
                cachedLifetimeDetails = productDetails
                launchLifetimeBillingFlow(activity, productDetails)
            }
        }
        return true
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK || purchases == null) return

        val hasLifetime = purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                purchase.products.contains(LIFETIME_PRODUCT_ID)
        }

        val hasMonthly = purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                purchase.products.contains(MONTHLY_PRODUCT_ID)
        }

        applySubscriptionType(hasLifetime, hasMonthly)
    }

    private fun queryLifetimeDetails(onResult: (ProductDetails?) -> Unit) {
        val queryParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(LIFETIME_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(queryParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                onResult(productDetailsList.firstOrNull())
            } else {
                onResult(null)
            }
        }
    }

    private fun queryMonthlyDetails(onResult: (ProductDetails?, String?) -> Unit) {
        val queryParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(MONTHLY_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(queryParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                onResult(null, null)
                return@queryProductDetailsAsync
            }

            val details = productDetailsList.firstOrNull()
            val offerToken = details
                ?.subscriptionOfferDetails
                ?.firstOrNull()
                ?.offerToken

            onResult(details, offerToken)
        }
    }

    private fun launchLifetimeBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    private fun launchMonthlyBillingFlow(activity: Activity, productDetails: ProductDetails, offerToken: String) {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Keep previous state; service can reconnect later.
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    refreshPurchases()
                    queryLifetimeDetails { cachedLifetimeDetails = it }
                    queryMonthlyDetails { details, token ->
                        cachedMonthlyDetails = details
                        cachedMonthlyOfferToken = token
                    }
                }
            }
        })
    }

    private fun updateTypeFromInApp(purchases: List<Purchase>) {
        val hasLifetime = purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                purchase.products.contains(LIFETIME_PRODUCT_ID)
        }

        applySubscriptionType(hasLifetime = hasLifetime, hasMonthly = subscriptionTypeState.value == SubscriptionType.MONTHLY)
    }

    private fun updateTypeFromSubs(purchases: List<Purchase>) {
        val hasMonthly = purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                purchase.products.contains(MONTHLY_PRODUCT_ID)
        }

        applySubscriptionType(hasLifetime = subscriptionTypeState.value == SubscriptionType.LIFETIME, hasMonthly = hasMonthly)
    }

    private fun applySubscriptionType(hasLifetime: Boolean, hasMonthly: Boolean) {
        val type = when {
            hasLifetime -> SubscriptionType.LIFETIME
            hasMonthly -> SubscriptionType.MONTHLY
            else -> SubscriptionType.FREE
        }

        subscriptionTypeState.value = type
        isPremiumState.value = type != SubscriptionType.FREE
    }

    companion object {
        const val LIFETIME_PRODUCT_ID: String = "smartscan_pro_lifetime"
        const val MONTHLY_PRODUCT_ID: String = "smartscan_pro_monthly"
    }
}
