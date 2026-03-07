package com.smartscan.ai.ui.main

import com.smartscan.ai.domain.model.AppLanguage
import com.smartscan.ai.ui.strings.getStrings
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingIntegrationTest {

    @Test
    fun `onboarding strings are present for all languages`() {
        AppLanguage.entries.forEach { language ->
            val strings = getStrings(language)
            assertTrue(strings.onboardingTitle1.isNotBlank())
            assertTrue(strings.onboardingSubtitle1.isNotBlank())
            assertTrue(strings.onboardingTitle2.isNotBlank())
            assertTrue(strings.onboardingSubtitle2.isNotBlank())
            assertTrue(strings.skip.isNotBlank())
            assertTrue(strings.next.isNotBlank())
            assertTrue(strings.done.isNotBlank())
        }
    }

    @Test
    fun `scanning progress strings are present for all languages`() {
        AppLanguage.entries.forEach { language ->
            val strings = getStrings(language)
            assertTrue(strings.onboardingScanning.isNotBlank())
            assertTrue(strings.scannedDocuments.contains("%d"))
            assertTrue(strings.scanningHint.isNotBlank())
        }
    }

    @Test
    fun `empty state strings are present for all languages`() {
        AppLanguage.entries.forEach { language ->
            val strings = getStrings(language)
            assertTrue(strings.emptyStateTitle.isNotBlank())
            assertTrue(strings.emptyStateSubtitle.isNotBlank())
            assertTrue(strings.emptyStateTip1.isNotBlank())
            assertTrue(strings.emptyStateTip2.isNotBlank())
            assertTrue(strings.emptyStateTip3.isNotBlank())
            assertTrue(strings.scanFirstDocument.isNotBlank())
        }
    }

    @Test
    fun `scanned documents message formats correctly`() {
        val strings = getStrings(AppLanguage.ENGLISH)
        val formatted = strings.scannedDocuments.format(42)
        assertTrue(formatted.contains("42"))
        assertTrue(!formatted.contains("%d"))
    }
}

