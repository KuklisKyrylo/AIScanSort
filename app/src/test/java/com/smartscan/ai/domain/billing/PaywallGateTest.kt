package com.smartscan.ai.domain.billing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaywallGateTest {

    @Test
    fun `shows paywall when user is not premium and scans exceed limit`() {
        assertTrue(PaywallGate.shouldShowPaywall(isPremium = false, scannedCount = 301))
    }

    @Test
    fun `shows paywall at free limit`() {
        assertTrue(PaywallGate.shouldShowPaywall(isPremium = false, scannedCount = 300))
    }

    @Test
    fun `does not show paywall below limit`() {
        assertFalse(PaywallGate.shouldShowPaywall(isPremium = false, scannedCount = 299))
    }

    @Test
    fun `does not show paywall for premium users`() {
        assertFalse(PaywallGate.shouldShowPaywall(isPremium = true, scannedCount = 1000))
    }
}
