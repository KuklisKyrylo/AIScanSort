package com.smartscan.ai.domain.billing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaywallGateTest {

    @Test
    fun `shows paywall when user is not pro and scans exceed limit`() {
        assertTrue(PaywallGate.shouldShowPaywall(isPro = false, scannedCount = 301))
    }

    @Test
    fun `does not show paywall at free limit`() {
        assertFalse(PaywallGate.shouldShowPaywall(isPro = false, scannedCount = 300))
    }

    @Test
    fun `does not show paywall for pro users`() {
        assertFalse(PaywallGate.shouldShowPaywall(isPro = true, scannedCount = 1000))
    }
}
