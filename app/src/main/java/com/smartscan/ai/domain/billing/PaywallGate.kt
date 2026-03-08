package com.smartscan.ai.domain.billing

private const val FREE_SCAN_LIMIT = 15_000

object PaywallGate {
    fun shouldShowPaywall(isPremium: Boolean, scannedCount: Int): Boolean {
        return !isPremium && scannedCount >= FREE_SCAN_LIMIT
    }
}
