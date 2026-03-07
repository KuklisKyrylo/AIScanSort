package com.smartscan.ai.ui.main

import com.smartscan.ai.domain.model.AppLanguage
import com.smartscan.ai.domain.usecase.SyncGalleryResult
import com.smartscan.ai.ui.strings.getStrings
import org.junit.Assert.assertEquals
import org.junit.Test

class MainViewModelMessageTest {

    @Test
    fun `uses localized empty screenshots message for all languages`() {
        AppLanguage.entries.forEach { language ->
            val strings = getStrings(language)
            val result = SyncGalleryResult(
                inserted = 0,
                skipped = 0,
                paywallReached = false,
                screenshotsFolderEmpty = true
            )

            assertEquals(strings.screenshotsFolderEmpty, buildSyncStatusMessage(strings, result))
        }
    }

    @Test
    fun `uses paywall message when paywall reached`() {
        val strings = getStrings(AppLanguage.ENGLISH)
        val result = SyncGalleryResult(
            inserted = 2,
            skipped = 1,
            paywallReached = true,
            screenshotsFolderEmpty = false
        )

        assertEquals(strings.freeLimitReached.format(2, 1), buildSyncStatusMessage(strings, result))
    }

    @Test
    fun `uses localized clear all success message for all languages`() {
        AppLanguage.entries.forEach { language ->
            val strings = getStrings(language)
            assertEquals(strings.clearAllScansSuccess, buildClearAllStatusMessage(strings))
        }
    }
}
