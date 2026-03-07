package com.smartscan.ai.domain.usecase

import android.util.Log
import com.smartscan.ai.data.analyzer.MLKitAnalyzer
import com.smartscan.ai.data.media.MediaStoreImageSource
import com.smartscan.ai.data.preferences.PreferencesManager
import com.smartscan.ai.domain.billing.PaywallGate
import com.smartscan.ai.domain.model.ScanStatus
import com.smartscan.ai.domain.model.ScannedImage
import com.smartscan.ai.domain.repository.BillingRepository
import com.smartscan.ai.domain.repository.ScanQuotaRepository
import com.smartscan.ai.domain.repository.ScanRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncGalleryUseCase @Inject constructor(
    private val mediaStoreImageSource: MediaStoreImageSource,
    private val mlKitAnalyzer: MLKitAnalyzer,
    private val scanRepository: ScanRepository,
    private val billingRepository: BillingRepository,
    private val preferencesManager: PreferencesManager,
    private val scanQuotaRepository: ScanQuotaRepository
) {

    suspend operator fun invoke(limit: Int = 80): SyncGalleryResult {
        Log.d("SyncGalleryUseCase", "Starting sync with limit=$limit")
        scanQuotaRepository.refreshFromServer()
        val screenshotsOnly = preferencesManager.syncScreenshotsOnlyFlow.first()
        val uris = mediaStoreImageSource.loadLatestImageUris(limit, screenshotsOnly)
        Log.d("SyncGalleryUseCase", "Found ${uris.size} URIs from MediaStore")

        if (screenshotsOnly && uris.isEmpty()) {
            Log.d("SyncGalleryUseCase", "Screenshots-only mode enabled but screenshot list is empty")
            return SyncGalleryResult(
                inserted = 0,
                skipped = 0,
                paywallReached = false,
                screenshotsFolderEmpty = true
            )
        }

        var inserted = 0
        var skipped = 0
        var scannedCount = scanQuotaRepository.getUsedScans()
        val isPremium = billingRepository.currentIsPremium()
        Log.d("SyncGalleryUseCase", "Current scannedCount=$scannedCount, isPremium=$isPremium")

        for ((index, uri) in uris.withIndex()) {
            Log.d("SyncGalleryUseCase", "Processing image $index/${ uris.size}: $uri")

            if (PaywallGate.shouldShowPaywall(isPremium = isPremium, scannedCount = scannedCount)) {
                Log.d("SyncGalleryUseCase", "Paywall reached at index $index")
                return SyncGalleryResult(inserted = inserted, skipped = skipped, paywallReached = true)
            }

            if (scanRepository.hasScannedUri(uri)) {
                Log.d("SyncGalleryUseCase", "URI already scanned: $uri")
                skipped++
                continue
            }

            val bitmap = mediaStoreImageSource.loadBitmap(uri)
            if (bitmap == null) {
                Log.w("SyncGalleryUseCase", "Failed to load bitmap for: $uri")
                skipped++
                continue
            }

            val analysis = try {
                Log.d("SyncGalleryUseCase", "Analyzing bitmap for: $uri")
                mlKitAnalyzer.analyze(bitmap)
            } catch (e: Throwable) {
                Log.e("SyncGalleryUseCase", "Error analyzing bitmap: ${e.message}", e)
                null
            } finally {
                bitmap.recycle()
            }

            val scannedImage = ScannedImage(
                uri = uri,
                extractedText = analysis?.text.orEmpty(),
                tags = analysis?.tags.orEmpty(),
                scannedAtEpochMillis = System.currentTimeMillis(),
                status = if (analysis == null) ScanStatus.FAILED else ScanStatus.PROCESSED
            )

            scanRepository.upsertScannedImage(scannedImage)
            val updatedCount = scanQuotaRepository.incrementAndGet()
            preferencesManager.setScanCount(updatedCount)
            scannedCount = updatedCount
            inserted++
            Log.d("SyncGalleryUseCase", "Successfully inserted image, total inserted=$inserted")
        }

        Log.d("SyncGalleryUseCase", "Sync completed: inserted=$inserted, skipped=$skipped")
        return SyncGalleryResult(
            inserted = inserted,
            skipped = skipped,
            paywallReached = false,
            screenshotsFolderEmpty = false
        )
    }
}

data class SyncGalleryResult(
    val inserted: Int,
    val skipped: Int,
    val paywallReached: Boolean,
    val screenshotsFolderEmpty: Boolean = false
)
