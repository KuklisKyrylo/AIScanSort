package com.smartscan.ai.data.analyzer

import android.graphics.Bitmap
import com.google.mlkit.nl.entityextraction.EntityExtractor
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.smartscan.ai.domain.model.ScanAnalysisResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Singleton
class MLKitAnalyzer @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val extractor: EntityExtractor = EntityExtraction.getClient(
        EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH).build()
    )

    suspend fun analyze(bitmap: Bitmap): ScanAnalysisResult = withContext(Dispatchers.Default) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val visionText = recognizer.process(image).await().text.trim()

        if (visionText.isBlank()) {
            return@withContext ScanAnalysisResult(text = "", tags = emptyList())
        }

        extractor.downloadModelIfNeeded().await()

        val params = EntityExtractionParams.Builder(visionText).build()
        val annotations = extractor.annotate(params).await()

        val tags = annotations
            .flatMap { annotation ->
                annotation.entities.mapNotNull { entity -> entity.typeName() }
            }
            .distinct()

        ScanAnalysisResult(
            text = visionText,
            tags = tags
        )
    }

    private fun com.google.mlkit.nl.entityextraction.Entity.typeName(): String? {
        return when (type) {
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

