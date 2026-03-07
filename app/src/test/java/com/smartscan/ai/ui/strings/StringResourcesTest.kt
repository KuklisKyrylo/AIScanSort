package com.smartscan.ai.ui.strings

import com.smartscan.ai.domain.model.AppLanguage
import org.junit.Assert.assertFalse
import org.junit.Test

class StringResourcesTest {

    @Test
    fun `paywall message formats with used and limit for all languages`() {
        AppLanguage.entries.forEach { language ->
            val message = getStrings(language).paywallMessage.format(7, 300)
            assertFalse(message.contains("%d"))
        }
    }

    @Test
    fun `paywall copy does not contain hardcoded 300 limit`() {
        AppLanguage.entries.forEach { language ->
            val strings = getStrings(language)
            assertFalse(strings.paywallMessage.contains("300"))
            assertFalse(strings.paywallSubtitle.contains("300"))
        }
    }

    @Test
    fun `pro and screenshots empty labels are present for all languages`() {
        AppLanguage.entries.forEach { language ->
            val strings = getStrings(language)
            assertFalse(strings.premiumLabel.isBlank())
            assertFalse(strings.unlimitedLabel.isBlank())
            assertFalse(strings.screenshotsFolderEmpty.isBlank())
        }
    }

    @Test
    fun `clear all labels are present for all languages`() {
        AppLanguage.entries.forEach { language ->
            val strings = getStrings(language)
            assertFalse(strings.clearAllScans.isBlank())
            assertFalse(strings.clearAllScansTitle.isBlank())
            assertFalse(strings.clearAllScansMessage.isBlank())
            assertFalse(strings.clearAllScansSuccess.isBlank())
        }
    }
}
