package com.smartscan.ai.data.analyzer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrTextSelectionTest {

    @Test
    fun `try russian assist for broken transliterated text`() {
        val text = "AyreHTuuKauua lblTaeMcA ayreHTMQMLMpoBaTb"
        assertTrue(OcrSelectionPolicy.shouldTryRussianAssist(text, null))
    }

    @Test
    fun `do not try russian assist for clean english`() {
        val text = "Payment completed successfully for this order"
        assertFalse(OcrSelectionPolicy.shouldTryRussianAssist(text, "en"))
    }

    @Test
    fun `do not try russian assist for clean russian`() {
        val text = "Аутентификация. Пытаемся аутентифицировать приложение"
        assertFalse(OcrSelectionPolicy.shouldTryRussianAssist(text, "ru"))
    }

    @Test
    fun `choose tesseract when mlkit has no cyrillic`() {
        val mlKit = "AyreHTuuKauua lblTaeMcA"
        val tess = "Аутентификация Пытаемся"
        assertEquals(tess, OcrSelectionPolicy.chooseRussianPreferred(mlKit, tess))
    }

    @Test
    fun `keep mlkit when tesseract empty`() {
        val mlKit = "Some English text"
        assertEquals(mlKit, OcrSelectionPolicy.chooseRussianPreferred(mlKit, null))
    }

    @Test
    fun `sanitizer keeps cyrillic words`() {
        val input = "Аутентификация Discord для компьютера"
        val result = OcrTextSanitizer.sanitize(input)
        assertTrue(result.contains("Аутентификация"))
        assertTrue(result.contains("компьютера"))
    }
}
