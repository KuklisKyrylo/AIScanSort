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

    private val isProState = MutableStateFlow(false)
    private var cachedProDetails: ProductDetails? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        startConnection()
    }

    override fun observeIsPro(): Flow<Boolean> = isProState.asStateFlow()

    override fun currentIsPro(): Boolean = isProState.value

    override fun refreshPurchases() {
        if (!billingClient.isReady) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                updateProState(purchases)
            }
        }
    }

    override fun launchProPurchase(activity: Activity): Boolean {
        if (!billingClient.isReady) return false

        val details = cachedProDetails
        if (details != null) {
            launchBillingFlow(activity, details)
            return true
        }

        queryProDetails { productDetails ->
            if (productDetails != null) {
                cachedProDetails = productDetails
                launchBillingFlow(activity, productDetails)
            }
        }
        return true
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            updateProState(purchases)
        }
    }

    private fun queryProDetails(onResult: (ProductDetails?) -> Unit) {
        val queryParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRO_PRODUCT_ID)
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

    private fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Keep the previous known state; service can reconnect later.
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    refreshPurchases()
                    queryProDetails { cachedProDetails = it }
                }
            }
        })
    }

    private fun updateProState(purchases: List<Purchase>) {
        isProState.value = purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                purchase.products.contains(PRO_PRODUCT_ID)
        }
    }

    companion object {
        const val PRO_PRODUCT_ID: String = "smartscan_pro_lifetime"
    }
}
