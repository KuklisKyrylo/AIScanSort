package com.smartscan.ai.data.analyzer

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.nl.entityextraction.EntityExtractor
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.smartscan.ai.domain.model.SUPPORTED_LANGUAGES
import com.smartscan.ai.domain.model.ScanAnalysisResult
import com.smartscan.ai.data.preferences.PreferencesManager
import com.smartscan.ai.domain.model.AppLanguage
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Singleton
class MLKitAnalyzer @Inject constructor(
    private val tesseractOcrFallback: TesseractOcrFallback,
    private val preferencesManager: PreferencesManager
) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val extractor: EntityExtractor = EntityExtraction.getClient(
        EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH).build()
    )
    private val languageIdentifier = LanguageIdentification.getClient()

    suspend fun analyze(bitmap: Bitmap): ScanAnalysisResult = withContext(Dispatchers.Default) {
        Log.d("MLKitAnalyzer", "=== OCR ANALYSIS START ===")

        val image = InputImage.fromBitmap(bitmap, 0)
        val mlKitText = OcrTextSanitizer.sanitize(recognizer.process(image).await().text)

        if (mlKitText.isBlank()) {
            return@withContext ScanAnalysisResult(text = "", tags = emptyList())
        }

        val currentLang = preferencesManager.languageFlow.first()
        val mlKitLang = detectLanguage(mlKitText)
        val shouldTryRuAssist = currentLang == AppLanguage.RUSSIAN &&
            OcrSelectionPolicy.shouldTryRussianAssist(mlKitText, mlKitLang)

        val tesseractText = if (shouldTryRuAssist) {
            runCatching {
                tesseractOcrFallback.tryRecognizeRussian(bitmap)
                    ?.let { OcrTextSanitizer.sanitize(it) }
                    ?.takeIf { it.isNotBlank() }
            }.getOrNull()
        } else {
            null
        }

        val finalText = if (shouldTryRuAssist) {
            OcrSelectionPolicy.chooseRussianPreferred(mlKitText, tesseractText)
        } else {
            mlKitText
        }

        if (finalText.isBlank()) {
            return@withContext ScanAnalysisResult(text = "", tags = emptyList())
        }

        val detectedLanguage = detectLanguage(finalText)
        val isLanguageSupported = detectedLanguage?.let { it in SUPPORTED_LANGUAGES } ?: true

        // Keep recognized text as-is even for unsupported languages to avoid false-positive language warnings.
        extractor.downloadModelIfNeeded().await()
        val params = EntityExtractionParams.Builder(finalText).build()
        val annotations = extractor.annotate(params).await()

        val tags = annotations
            .flatMap { annotation -> annotation.entities.mapNotNull { entity -> mapEntityType(entity) } }
            .distinct()

        ScanAnalysisResult(
            text = finalText,
            tags = tags,
            detectedLanguage = detectedLanguage,
            isLanguageSupported = isLanguageSupported
        )
    }

    private suspend fun detectLanguage(text: String): String? {
        return try {
            languageIdentifier
                .identifyLanguage(text)
                .await()
                .takeIf { it.isNotEmpty() && it != "und" && it != "unknown" }
        } catch (_: Exception) {
            null
        }
    }

    private fun mapEntityType(entity: com.google.mlkit.nl.entityextraction.Entity): String? {
        return when (entity.type) {
            com.google.mlkit.nl.entityextraction.Entity.TYPE_ADDRESS -> "address"
            com.google.mlkit.nl.entityextraction.Entity.TYPE_DATE_TIME -> "datetime"
            com.google.mlkit.nl.entityextraction.Entity.TYPE_EMAIL -> "email"
            com.google.mlkit.nl.entityextraction.Entity.TYPE_FLIGHT_NUMBER -> "flight"
            com.google.mlkit.nl.entityextraction.Entity.TYPE_IBAN -> "iban"
            com.google.mlkit.nl.entityextraction.Entity.TYPE_ISBN -> "isbn"
            com.google.mlkit.nl.entityextraction.Entity.TYPE_MONEY -> "money"
            com.google.mlkit.nl.entityextraction.Entity.TYPE_PAYMENT_CARD -> "card"
            com.google.mlkit.nl.entityextraction.Entity.TYPE_PHONE -> "phone"
            com.google.mlkit.nl.entityextraction.Entity.TYPE_TRACKING_NUMBER -> "tracking"
            com.google.mlkit.nl.entityextraction.Entity.TYPE_URL -> "url"
            else -> null
        }
    }
}

internal object OcrSelectionPolicy {
    fun shouldTryRussianAssist(mlKitText: String, mlKitLang: String?): Boolean {
        if (mlKitText.isBlank()) return false

        // Never run RU assist for clearly non-Russian languages.
        if (mlKitLang != null && mlKitLang in setOf("en", "es", "de", "zh", "ja", "ko")) {
            return false
        }

        // If text already has good Cyrillic quality, don't spend Tesseract time.
        if (containsEnoughCyrillic(mlKitText) && !looksNoisyMixedScript(mlKitText)) {
            return false
        }

        // Trigger assist for transliterated/broken outputs or noisy mixed-script garbage.
        return looksLikeBrokenRussianTransliteration(mlKitText) || looksNoisyMixedScript(mlKitText)
    }

    fun chooseRussianPreferred(mlKitText: String, tesseractText: String?): String {
        if (tesseractText.isNullOrBlank()) return mlKitText

        val mlCyr = mlKitText.count { it in '\u0400'..'\u04FF' }
        val tsCyr = tesseractText.count { it in '\u0400'..'\u04FF' }

        if (mlCyr == 0 && tsCyr > 0) return tesseractText

        val mlRatio = cyrRatio(mlKitText)
        val tsRatio = cyrRatio(tesseractText)
        if (tsCyr >= mlCyr + 5 && tsRatio >= mlRatio + 0.10f) return tesseractText

        return mlKitText
    }

    private fun containsEnoughCyrillic(text: String): Boolean {
        val ratio = cyrRatio(text)
        return ratio >= 0.35f
    }

    private fun cyrRatio(text: String): Float {
        val letters = text.filter { it.isLetter() }
        if (letters.isEmpty()) return 0f
        val cyr = letters.count { it in '\u0400'..'\u04FF' }
        return cyr.toFloat() / letters.length.toFloat()
    }

    private fun looksLikeBrokenRussianTransliteration(text: String): Boolean {
        val hasCyr = text.any { it in '\u0400'..'\u04FF' }
        if (hasCyr) return false

        val latin = text.count { it in 'A'..'Z' || it in 'a'..'z' }
        if (latin < 8) return false

        val confusable = setOf('A','B','C','E','H','K','M','O','P','T','X','Y','a','c','e','o','p','x','y','l','b','u','n','r')
        val confusableCount = text.count { it in confusable }
        val ratio = confusableCount.toFloat() / latin.toFloat()
        val weirdCase = text.any { it.isUpperCase() } && text.any { it.isLowerCase() }

        return ratio >= 0.60f && weirdCase
    }

    private fun looksNoisyMixedScript(text: String): Boolean {
        val tokens = text.split(' ').filter { it.length >= 4 }
        val mixedTokenCount = tokens.count { token ->
            val hasCyr = token.any { it in '\u0400'..'\u04FF' }
            val hasLat = token.any { it in 'A'..'Z' || it in 'a'..'z' }
            hasCyr && hasLat
        }

        val suspiciousPunct = text.count {
            !it.isLetterOrDigit() && !it.isWhitespace() && it !in setOf('.', ',', ':', ';', '!', '?', '-', '(', ')')
        }
        val punctRatio = suspiciousPunct.toFloat() / text.length.coerceAtLeast(1).toFloat()

        return mixedTokenCount >= 2 || punctRatio >= 0.12f
    }
}

internal object OcrTextSanitizer {
    fun sanitize(rawText: String): String {
        val normalized = rawText.trim().replace(Regex("\\s+"), " ")
        if (normalized.isBlank()) return ""

        val filteredTokens = normalized
            .split(' ')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { isLikelyNoiseToken(it) }

        return filteredTokens.joinToString(" ").trim()
    }

    private fun isLikelyNoiseToken(token: String): Boolean {
        val cleaned = token.filter { it.isLetterOrDigit() }
        if (cleaned.isBlank()) return true
        if (cleaned.length <= 1) return true

        val lowered = cleaned.lowercase()
        val distinctChars = lowered.toSet().size

        if (distinctChars == 1 && lowered.length >= 2) return true
        if (cleaned.length == 2 && distinctChars < 2) return true

        if (lowered.length >= 4) {
            val maxCharCount = lowered.groupingBy { it }.eachCount().values.maxOrNull() ?: 0
            if (maxCharCount >= (lowered.length * 0.75).toInt()) return true
        }

        val maxRun = maxConsecutiveRun(lowered)
        if (maxRun >= 4) return true

        return false
    }

    private fun maxConsecutiveRun(value: String): Int {
        if (value.isEmpty()) return 0
        var best = 1
        var current = 1
        for (i in 1 until value.length) {
            if (value[i] == value[i - 1]) {
                current++
                if (current > best) best = current
            } else {
                current = 1
            }
        }
        return best
    }
}
