package com.smartscan.ai.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.smartscan.ai.data.preferences.PreferencesManager
import com.smartscan.ai.domain.model.SubscriptionType
import com.smartscan.ai.domain.repository.BillingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext context: Context,
    private val preferencesManager: PreferencesManager
) : BillingRepository, PurchasesUpdatedListener {

    private val billingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val isPremiumState = MutableStateFlow(false)
    private val subscriptionTypeState = MutableStateFlow(SubscriptionType.FREE)
    private val billingInProgressState = MutableStateFlow(false)
    private val billingMessageState = MutableStateFlow("")
    private val monthlyPriceState = MutableStateFlow("")
    private val lifetimePriceState = MutableStateFlow("")

    private var cachedLifetimeDetails: ProductDetails? = null
    private var cachedMonthlyDetails: ProductDetails? = null
    private var cachedMonthlyOfferToken: String? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        // Instant local entitlement for startup UX before network-backed refresh.
        billingScope.launch {
            val cachedType = preferencesManager.subscriptionTypeFlow.first()
            applySubscriptionType(cachedType)
        }
        startConnection()
    }

    override fun observeIsPremium(): Flow<Boolean> = isPremiumState.asStateFlow()

    override fun observeSubscriptionType(): Flow<SubscriptionType> = subscriptionTypeState.asStateFlow()

    override fun observeBillingInProgress(): Flow<Boolean> = billingInProgressState.asStateFlow()

    override fun observeBillingMessage(): Flow<String> = billingMessageState.asStateFlow()

    override fun observeMonthlyPrice(): Flow<String> = monthlyPriceState.asStateFlow()

    override fun observeLifetimePrice(): Flow<String> = lifetimePriceState.asStateFlow()

    override fun currentIsPremium(): Boolean = isPremiumState.value

    override fun currentSubscriptionType(): SubscriptionType = subscriptionTypeState.value

    override fun refreshPurchases() {
        if (!billingClient.isReady) {
            startConnection()
            return
        }

        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(inAppParams) { inAppResult, inAppPurchases ->
            if (inAppResult.responseCode != BillingClient.BillingResponseCode.OK) {
                postBillingMessage("Unable to refresh one-time purchases")
                return@queryPurchasesAsync
            }

            processPurchases(inAppPurchases)
            billingClient.queryPurchasesAsync(subsParams) { subsResult, subsPurchases ->
                if (subsResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    postBillingMessage("Unable to refresh subscriptions")
                    return@queryPurchasesAsync
                }

                processPurchases(subsPurchases)
                applySubscriptionType(
                    hasLifetime = hasActivePurchase(inAppPurchases, LIFETIME_PRODUCT_ID),
                    hasMonthly = hasActivePurchase(subsPurchases, MONTHLY_PRODUCT_ID)
                )
            }
        }
    }

    override fun restorePurchases() {
        postBillingMessage("Restoring purchases...")
        refreshPurchases()
    }

    override fun launchMonthlyPurchase(activity: Activity): Boolean {
        if (!billingClient.isReady) {
            startConnection()
            postBillingMessage("Billing is initializing. Please try again.")
            return false
        }

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
            } else {
                postBillingMessage("Monthly plan is unavailable")
            }
        }
        return true
    }

    override fun launchLifetimePurchase(activity: Activity): Boolean {
        if (!billingClient.isReady) {
            startConnection()
            postBillingMessage("Billing is initializing. Please try again.")
            return false
        }

        val details = cachedLifetimeDetails
        if (details != null) {
            launchLifetimeBillingFlow(activity, details)
            return true
        }

        queryLifetimeDetails { productDetails ->
            if (productDetails != null) {
                cachedLifetimeDetails = productDetails
                launchLifetimeBillingFlow(activity, productDetails)
            } else {
                postBillingMessage("Lifetime plan is unavailable")
            }
        }
        return true
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        billingInProgressState.value = false

        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val list = purchases.orEmpty()
                processPurchases(list)
                applySubscriptionType(
                    hasLifetime = hasActivePurchase(list, LIFETIME_PRODUCT_ID) ||
                        subscriptionTypeState.value == SubscriptionType.LIFETIME,
                    hasMonthly = hasActivePurchase(list, MONTHLY_PRODUCT_ID) ||
                        subscriptionTypeState.value == SubscriptionType.MONTHLY
                )
                postBillingMessage("Purchase updated")
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                postBillingMessage("Purchase canceled")
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                postBillingMessage("Item already owned. Restoring purchases...")
                refreshPurchases()
            }

            else -> {
                postBillingMessage("Billing error: ${billingResult.debugMessage}")
            }
        }
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
                val details = productDetailsList.firstOrNull()
                lifetimePriceState.value = details?.oneTimePurchaseOfferDetails?.formattedPrice.orEmpty()
                onResult(details)
            } else {
                postBillingMessage("Unable to load lifetime plan")
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
                postBillingMessage("Unable to load monthly plan")
                onResult(null, null)
                return@queryProductDetailsAsync
            }

            val details = productDetailsList.firstOrNull()
            monthlyPriceState.value = extractMonthlyFormattedPrice(details)
             val offerToken = details
                 ?.subscriptionOfferDetails
                 ?.firstOrNull { !it.offerToken.isNullOrBlank() }
                 ?.offerToken

             onResult(details, offerToken)
         }
     }

    private fun launchLifetimeBillingFlow(activity: Activity, productDetails: ProductDetails) {
        billingInProgressState.value = true
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            billingInProgressState.value = false
            postBillingMessage("Unable to start billing flow")
        }
    }

    private fun launchMonthlyBillingFlow(activity: Activity, productDetails: ProductDetails, offerToken: String) {
        billingInProgressState.value = true
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            billingInProgressState.value = false
            postBillingMessage("Unable to start billing flow")
        }
    }

    private fun startConnection() {
        if (billingClient.isReady) return

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Retry with a small delay to recover transient Play service disconnects.
                billingScope.launch {
                    delay(1200)
                    startConnection()
                }
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    refreshPurchases()
                    queryLifetimeDetails { cachedLifetimeDetails = it }
                    queryMonthlyDetails { details, token ->
                        cachedMonthlyDetails = details
                        cachedMonthlyOfferToken = token
                    }
                    return
                }
                postBillingMessage("Billing setup failed: ${billingResult.debugMessage}")
            }
        })
    }

    private fun processPurchases(purchases: List<Purchase>) {
        purchases.forEach { purchase ->
            when (purchase.purchaseState) {
                Purchase.PurchaseState.PENDING -> {
                    postBillingMessage("Purchase is pending")
                }

                Purchase.PurchaseState.PURCHASED -> {
                    if (purchase.products.any { it == LIFETIME_PRODUCT_ID || it == MONTHLY_PRODUCT_ID }) {
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase)
                        }
                    }
                }

                else -> Unit
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val ackParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(ackParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                postBillingMessage("Purchase confirmed")
                refreshPurchases()
            } else {
                postBillingMessage("Acknowledge failed: ${billingResult.debugMessage}")
            }
        }
    }

    private fun hasActivePurchase(purchases: List<Purchase>, productId: String): Boolean {
        return purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                purchase.products.contains(productId)
        }
    }

    private fun applySubscriptionType(hasLifetime: Boolean, hasMonthly: Boolean) {
        val type = when {
            hasLifetime -> SubscriptionType.LIFETIME
            hasMonthly -> SubscriptionType.MONTHLY
            else -> SubscriptionType.FREE
        }
        applySubscriptionType(type)
    }

    private fun applySubscriptionType(type: SubscriptionType) {
        subscriptionTypeState.value = type
        isPremiumState.value = type != SubscriptionType.FREE
        billingScope.launch {
            preferencesManager.setSubscriptionType(type)
        }
    }

    private fun postBillingMessage(message: String) {
        billingMessageState.value = message
    }

    private fun extractMonthlyFormattedPrice(details: ProductDetails?): String {
        if (details == null) return ""
        val pricingPhase = details.subscriptionOfferDetails
            ?.firstOrNull { !it.offerToken.isNullOrBlank() }
            ?.pricingPhases
            ?.pricingPhaseList
            ?.firstOrNull()

        return pricingPhase?.formattedPrice.orEmpty()
    }

    companion object {
        const val LIFETIME_PRODUCT_ID: String = "smartscan_pro_lifetime"
        const val MONTHLY_PRODUCT_ID: String = "smartscan_pro_monthly"
    }
}
