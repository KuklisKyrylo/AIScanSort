package com.smartscan.ai.ui.main

import com.smartscan.ai.domain.model.SubscriptionType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainViewModelBillingStateTest {

    @Test
    fun `monthly purchase keeps sync allowed and hides paywall`() {
        val state = resolvePaywallState(
            subscriptionType = SubscriptionType.MONTHLY,
            trialScans = 5000,
            freeTrialScanLimit = 15_000
        )

        assertFalse(state.showPaywall)
        assertTrue(state.isSyncAllowed)
    }

    @Test
    fun `lifetime purchase keeps sync allowed and hides paywall`() {
        val state = resolvePaywallState(
            subscriptionType = SubscriptionType.LIFETIME,
            trialScans = 5000,
            freeTrialScanLimit = 15_000
        )

        assertFalse(state.showPaywall)
        assertTrue(state.isSyncAllowed)
    }

    @Test
    fun `free user at limit sees paywall and sync blocked`() {
        val state = resolvePaywallState(
            subscriptionType = SubscriptionType.FREE,
            trialScans = 15_000,
            freeTrialScanLimit = 15_000
        )

        assertTrue(state.showPaywall)
        assertFalse(state.isSyncAllowed)
    }
}
