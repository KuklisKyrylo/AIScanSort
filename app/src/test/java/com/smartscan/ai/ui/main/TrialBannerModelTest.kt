package com.smartscan.ai.ui.main

import com.smartscan.ai.domain.model.AppLanguage
import com.smartscan.ai.ui.strings.getStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrialBannerModelTest {

    private val strings = getStrings(AppLanguage.ENGLISH)

    @Test
    fun `shows premium and unlimited when subscription is active`() {
        val model = buildTrialBannerModel(
            isPremium = true,
            scansUsed = 120,
            scansRemaining = 180,
            scansLimit = 300,
            strings = strings
        )

        assertEquals(strings.premiumLabel, model.title)
        assertEquals(strings.unlimitedLabel, model.subtitle)
        assertFalse(model.showUpgrade)
        assertFalse(model.showProgress)
    }

    @Test
    fun `shows trial usage and progress for free user`() {
        val model = buildTrialBannerModel(
            isPremium = false,
            scansUsed = 90,
            scansRemaining = 210,
            scansLimit = 300,
            strings = strings
        )

        assertEquals(strings.trialUsage.format(90, 300), model.title)
        assertEquals(strings.trialRemaining.format(210), model.subtitle)
        assertTrue(model.showUpgrade)
        assertTrue(model.showProgress)
        assertEquals(0.3f, model.progress, 0.0001f)
    }

    @Test
    fun `returns trial view again when subscription is not active`() {
        val model = buildTrialBannerModel(
            isPremium = false,
            scansUsed = 300,
            scansRemaining = 0,
            scansLimit = 300,
            strings = strings
        )

        assertTrue(model.title.startsWith("Trial:"))
        assertEquals(strings.trialRemaining.format(0), model.subtitle)
        assertTrue(model.showUpgrade)
    }
}
