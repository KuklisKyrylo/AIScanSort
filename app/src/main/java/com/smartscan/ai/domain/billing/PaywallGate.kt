package com.smartscan.ai.domain.billing

private const val FREE_SCAN_LIMIT = 50

object PaywallGate {
    fun shouldShowPaywall(isPro: Boolean, scannedCount: Int): Boolean {
        return !isPro && scannedCount > FREE_SCAN_LIMIT
    }
}

