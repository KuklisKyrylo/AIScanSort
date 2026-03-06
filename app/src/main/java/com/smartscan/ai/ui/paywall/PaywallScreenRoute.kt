package com.smartscan.ai.ui.paywall

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartscan.ai.ui.main.MainViewModel

@Composable
fun PaywallScreenRoute(
    onNavigateBack: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PaywallScreen(
        strings = state.strings,
        onNavigateBack = onNavigateBack,
        onMonthlyClick = {
            // TODO: Implement Google Play Billing for Monthly
        },
        onLifetimeClick = {
            // TODO: Implement Google Play Billing for Lifetime
        }
    )
}

