package com.smartscan.ai.domain.usecase

import android.graphics.Bitmap
import android.util.Log
import com.smartscan.ai.data.analyzer.MLKitAnalyzer
import com.smartscan.ai.data.media.MediaStoreImageSource
import com.smartscan.ai.data.preferences.PreferencesManager
import com.smartscan.ai.domain.model.ScanAnalysisResult
import com.smartscan.ai.domain.repository.BillingRepository
import com.smartscan.ai.domain.repository.ScanQuotaRepository
import com.smartscan.ai.domain.repository.ScanRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SyncGalleryUseCaseTest {

    private val mediaStoreImageSource = mockk<MediaStoreImageSource>()
    private val analyzer = mockk<MLKitAnalyzer>()
    private val scanRepository = mockk<ScanRepository>()
    private val billingRepository = mockk<BillingRepository>()
    private val preferencesManager = mockk<PreferencesManager>()
    private val scanQuotaRepository = mockk<ScanQuotaRepository>()

    private val useCase = SyncGalleryUseCase(
        mediaStoreImageSource = mediaStoreImageSource,
        mlKitAnalyzer = analyzer,
        scanRepository = scanRepository,
        billingRepository = billingRepository,
        preferencesManager = preferencesManager,
        scanQuotaRepository = scanQuotaRepository
    )

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `returns paywall reached and does not analyze when free limit exhausted`() = runTest {
        // given
        coEvery { scanQuotaRepository.refreshFromServer() } returns Unit
        every { preferencesManager.syncScreenshotsOnlyFlow } returns flowOf(true)
        coEvery { mediaStoreImageSource.loadLatestImageUris(any(), any()) } returns listOf("content://test/1")
        coEvery { scanQuotaRepository.getUsedScans() } returns 300
        every { billingRepository.currentIsPremium() } returns false

        // when
        val result = useCase(limit = 1)

        // then
        assertTrue(result.paywallReached)
        assertEquals(0, result.inserted)
        assertEquals(0, result.skipped)

        coVerify(exactly = 0) { mediaStoreImageSource.loadBitmap(any()) }
        coVerify(exactly = 0) { analyzer.analyze(any()) }
        coVerify(exactly = 0) { scanQuotaRepository.incrementAndGet() }
    }

    @Test
    fun `syncs one image and increments quota when below limit`() = runTest {
        // given
        val bitmap = mockk<Bitmap>(relaxed = true)

        coEvery { scanQuotaRepository.refreshFromServer() } returns Unit
        every { preferencesManager.syncScreenshotsOnlyFlow } returns flowOf(true)
        coEvery { mediaStoreImageSource.loadLatestImageUris(any(), any()) } returns listOf("content://test/1")
        coEvery { scanQuotaRepository.getUsedScans() } returns 10
        every { billingRepository.currentIsPremium() } returns false
        coEvery { scanRepository.hasScannedUri("content://test/1") } returns false
        coEvery { mediaStoreImageSource.loadBitmap("content://test/1") } returns bitmap
        coEvery { analyzer.analyze(bitmap) } returns ScanAnalysisResult(text = "hello", tags = listOf("tag"))
        coEvery { scanRepository.upsertScannedImage(any()) } returns 1L
        coEvery { scanQuotaRepository.incrementAndGet() } returns 11
        coEvery { preferencesManager.setScanCount(11) } returns Unit

        // when
        val result = useCase(limit = 1)

        // then
        assertFalse(result.paywallReached)
        assertEquals(1, result.inserted)
        assertEquals(0, result.skipped)

        coVerify(exactly = 1) { scanQuotaRepository.incrementAndGet() }
        coVerify(exactly = 1) { preferencesManager.setScanCount(11) }
        coVerify(exactly = 1) { analyzer.analyze(bitmap) }
    }

    @Test
    fun `returns screenshots folder empty when screenshots only mode has no images`() = runTest {
        // given
        coEvery { scanQuotaRepository.refreshFromServer() } returns Unit
        every { preferencesManager.syncScreenshotsOnlyFlow } returns flowOf(true)
        coEvery { mediaStoreImageSource.loadLatestImageUris(any(), any()) } returns emptyList()

        // when
        val result = useCase(limit = 80)

        // then
        assertFalse(result.paywallReached)
        assertTrue(result.screenshotsFolderEmpty)
        assertEquals(0, result.inserted)
        assertEquals(0, result.skipped)

        coVerify(exactly = 0) { scanQuotaRepository.getUsedScans() }
        coVerify(exactly = 0) { mediaStoreImageSource.loadBitmap(any()) }
        coVerify(exactly = 0) { analyzer.analyze(any()) }
    }
}
