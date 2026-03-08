package com.smartscan.ai.ui.paywall

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartscan.ai.ui.main.MainViewModel

@Composable
fun PaywallScreenRoute(
    onNavigateBack: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()
    val uriHandler = LocalUriHandler.current

    PaywallScreen(
        strings = state.strings,
        onNavigateBack = onNavigateBack,
        onMonthlyClick = {
            if (activity != null) {
                viewModel.onMonthlyPlanClick(activity)
            }
        },
        onLifetimeClick = {
            if (activity != null) {
                viewModel.onLifetimePlanClick(activity)
            }
        },
        onTermsClick = { uriHandler.openUri(LegalLinks.TERMS_URL) },
        onPrivacyClick = { uriHandler.openUri(LegalLinks.PRIVACY_URL) },
        onRestoreClick = viewModel::onRestorePurchasesClick,
        isBillingInProgress = state.isBillingInProgress,
        billingMessage = state.billingMessage,
        monthlyPrice = state.monthlyPrice,
        lifetimePrice = state.lifetimePrice
    )
}

private fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
