package com.smartscan.ai.ui.main

import org.junit.Assert.assertEquals
import org.junit.Test

class MainUiStateTest {

    @Test
    fun `default trial values match free tier limit`() {
        val state = MainUiState()
        assertEquals(300, state.trialScansLimit)
        assertEquals(300, state.trialScansRemaining)
    }
}
