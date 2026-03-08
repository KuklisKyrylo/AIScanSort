package com.smartscan.ai.domain.billing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaywallGateTest {

    @Test
    fun `shows paywall when user is not premium and scans exceed limit`() {
        assertTrue(PaywallGate.shouldShowPaywall(isPremium = false, scannedCount = 1201))
    }

    @Test
    fun `shows paywall at free limit`() {
        assertTrue(PaywallGate.shouldShowPaywall(isPremium = false, scannedCount = 1200))
    }

    @Test
    fun `does not show paywall below limit`() {
        assertFalse(PaywallGate.shouldShowPaywall(isPremium = false, scannedCount = 1199))
    }

    @Test
    fun `does not show paywall for premium users`() {
        assertFalse(PaywallGate.shouldShowPaywall(isPremium = true, scannedCount = 1000))
    }
}
