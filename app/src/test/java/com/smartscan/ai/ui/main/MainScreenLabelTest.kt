package com.smartscan.ai.ui.main

import com.smartscan.ai.domain.model.AppLanguage
import com.smartscan.ai.ui.strings.getStrings
import org.junit.Assert.assertEquals
import org.junit.Test

class MainScreenLabelTest {

    @Test
    fun `build clear all label with count`() {
        assertEquals("Clear all (12)", buildClearAllScansLabel("Clear all", 12))
    }

    @Test
    fun `build clear all label clamps negative count to zero`() {
        assertEquals("Clear all (0)", buildClearAllScansLabel("Clear all", -5))
    }

    @Test
    fun `build clear all label uses localized russian base`() {
        val ru = getStrings(AppLanguage.RUSSIAN)
        assertEquals("${ru.clearAllScans} (3)", buildClearAllScansLabel(ru.clearAllScans, 3))
    }
}

