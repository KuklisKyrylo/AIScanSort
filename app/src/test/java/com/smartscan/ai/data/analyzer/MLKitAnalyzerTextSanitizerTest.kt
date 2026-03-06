package com.smartscan.ai.data.analyzer

import org.junit.Assert.assertEquals
import org.junit.Test

class MLKitAnalyzerTextSanitizerTest {

    @Test
    fun sanitizer_drops_single_char_noise() {
        assertEquals("", OcrTextSanitizer.sanitize("A"))
    }

    @Test
    fun sanitizer_drops_repeated_char_noise() {
        assertEquals("", OcrTextSanitizer.sanitize("aaasA"))
    }

    @Test
    fun sanitizer_keeps_meaningful_text() {
        assertEquals("Invoice 2900", OcrTextSanitizer.sanitize("Invoice   2900"))
    }

    @Test
    fun sanitizer_drops_two_char_repeated() {
        assertEquals("", OcrTextSanitizer.sanitize("aa"))
    }

    @Test
    fun sanitizer_keeps_valid_short_words() {
        assertEquals("cat", OcrTextSanitizer.sanitize("cat"))
        assertEquals("the dog ran", OcrTextSanitizer.sanitize("the dog ran"))
    }
}
