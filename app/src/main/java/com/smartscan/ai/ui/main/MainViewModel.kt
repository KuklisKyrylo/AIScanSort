package com.smartscan.ai.ui.main

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.smartscan.ai.data.preferences.PreferencesManager
import com.smartscan.ai.domain.billing.PaywallGate
import com.smartscan.ai.domain.model.AppLanguage
import com.smartscan.ai.domain.model.ScannedImage
import com.smartscan.ai.domain.model.SubscriptionType
import com.smartscan.ai.domain.repository.BillingRepository
import com.smartscan.ai.domain.repository.ScanQuotaRepository
import com.smartscan.ai.domain.repository.ScanRepository
import com.smartscan.ai.domain.usecase.SyncGalleryResult
import com.smartscan.ai.domain.usecase.SyncGalleryUseCase
import com.smartscan.ai.ui.base.BaseViewModel
import com.smartscan.ai.ui.strings.StringResources
import com.smartscan.ai.ui.strings.getStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val billingRepository: BillingRepository,
    private val syncGalleryUseCase: SyncGalleryUseCase,
    private val preferencesManager: PreferencesManager,
    private val scanQuotaRepository: ScanQuotaRepository
) : BaseViewModel() {

    companion object {
        private const val FREE_TRIAL_SCAN_LIMIT = 300
    }

    private val searchQuery = MutableStateFlow("")
    private val showEmptyScansFlow = MutableStateFlow(true) // Show all scans by default
    private var syncJob: Job? = null

    // Initialize with current language from preferences
    private val initialLanguage = try {
        runBlocking { preferencesManager.languageFlow.first() }
    } catch (_: Exception) {
        AppLanguage.ENGLISH
    }

    private val _uiState = MutableStateFlow(MainUiState(
        currentLanguage = initialLanguage,
        strings = getStrings(initialLanguage)
    ))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _showOnboarding = MutableStateFlow(false)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    init {
        // Restore sync summary data synchronously to avoid empty summary after app restart.
        try {
            val savedLastSyncTs = runBlocking { preferencesManager.lastSuccessfulSyncTimeFlow.first() }
            val savedSummaryScanned = runBlocking { preferencesManager.lastSummaryScannedCountFlow.first() }
            val savedSummaryElapsed = runBlocking { preferencesManager.lastSummaryElapsedSecondsFlow.first() }
            _uiState.update {
                it.copy(
                    lastSuccessfulSyncTimeMs = savedLastSyncTs,
                    sessionDocumentCount = savedSummaryScanned,
                    sessionElapsedSeconds = savedSummaryElapsed
                )
            }
        } catch (_: Exception) {
            // Ignore - defaults remain
        }

        billingRepository.refreshPurchases()
        viewModelScope.launch {
            scanQuotaRepository.refreshFromServer()
            val serverCount = scanQuotaRepository.getUsedScans()
            preferencesManager.ensureScanCountAtLeast(serverCount)
        }
        // Listen for language changes
        viewModelScope.launch {
            preferencesManager.languageFlow
                .distinctUntilChanged()
                .collect { language ->
                    val strings = getStrings(language)
                    _uiState.update { state ->
                        val localizedStatusMessage = when {
                            state.syncPhase == SyncPhase.RUNNING -> strings.syncingGallery
                            state.syncPhase == SyncPhase.PAUSED -> "${strings.syncingGallery} (${state.syncProcessed}/${state.syncTotal})"
                            state.lastSyncResultForStatus != null -> buildSyncStatusMessage(strings, state.lastSyncResultForStatus)
                            else -> state.syncStatusMessage
                        }
                        state.copy(
                            currentLanguage = language,
                            strings = strings,
                            syncStatusMessage = localizedStatusMessage
                        )
                    }
                }
        }
        checkFirstTimeUser()
        observeImages()
        observePaywallState()
    }

    private fun checkFirstTimeUser() {
        // Check if this is the first time user is launching the app
        val isFirstTime = preferencesManager.isFirstTimeUser()
        _showOnboarding.value = isFirstTime
    }

    fun onOnboardingComplete() {
        preferencesManager.markFirstTimeUserComplete()
        _showOnboarding.value = false
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onMediaPermissionChanged(granted: Boolean) {
        val previousPermission = _uiState.value.hasMediaPermission
        _uiState.update { it.copy(hasMediaPermission = granted) }

        // Only auto-sync if:
        // 1. Permission was just granted (transition from false to true)
        // 2. Never synced before (lastSuccessfulSyncTimeMs == 0L, first time user)
        // 3. Not currently syncing
        if (granted && !previousPermission && _uiState.value.lastSuccessfulSyncTimeMs == 0L && !_uiState.value.isSyncing) {
            android.util.Log.d("MainViewModel", "Permission granted for first time, starting initial sync")
            syncGallery()
        }
    }

    fun onSyncNowClick() {
        val state = _uiState.value
        android.util.Log.d("MainViewModel", "onSyncNowClick called - hasPermission=${state.hasMediaPermission}, isSyncAllowed=${state.isSyncAllowed}, phase=${state.syncPhase}")
        if (!state.hasMediaPermission || !state.isSyncAllowed) {
            android.util.Log.w("MainViewModel", "Sync blocked - hasPermission=${state.hasMediaPermission}, isSyncAllowed=${state.isSyncAllowed}")
            return
        }
        when (state.syncPhase) {
            SyncPhase.RUNNING -> Unit
            SyncPhase.PAUSED -> onContinueSyncClick()
            SyncPhase.IDLE -> syncGallery(startIndex = 0, restart = true)
        }
    }

    fun onStopSyncClick() {
        if (syncJob?.isActive == true) {
            android.util.Log.d("MainViewModel", "Stopping sync by user action")
            syncJob?.cancel()
        }
    }

    fun onContinueSyncClick() {
        val state = _uiState.value
        if (!state.hasMediaPermission || !state.isSyncAllowed) return
        if (state.syncPhase == SyncPhase.PAUSED) {
            android.util.Log.d("MainViewModel", "Continuing sync from index=${state.syncResumeIndex}")
            syncGallery(startIndex = state.syncResumeIndex, restart = false)
        }
    }

    fun onRestartSyncClick() {
        viewModelScope.launch {
            val state = _uiState.value
            if (!state.hasMediaPermission || !state.isSyncAllowed) return@launch
            android.util.Log.d("MainViewModel", "Restarting sync from beginning - deleting session data from timestamp=${state.syncStartTimestamp}")

            // Delete all scans from current session
            if (state.syncStartTimestamp > 0) {
                val deletedCount = scanRepository.deleteScannedImagesAfter(state.syncStartTimestamp)
                android.util.Log.d("MainViewModel", "Deleted $deletedCount scans from current session")
            }

            // Reset state and start from 0
            _uiState.update {
                it.copy(
                    syncResumeIndex = 0,
                    syncProcessed = 0,
                    syncTotal = 0,
                    syncStartTimestamp = 0L,
                    syncStartTimeMs = 0L,
                    sessionElapsedSeconds = 0
                )
            }

            syncGallery(startIndex = 0, restart = true)
        }
    }

    fun onMonthlyPlanClick(activity: Activity) {
        billingRepository.launchMonthlyPurchase(activity)
    }

    fun onLifetimePlanClick(activity: Activity) {
        billingRepository.launchLifetimePurchase(activity)
    }

    fun onBuyPremiumClick(activity: Activity) {
        billingRepository.launchLifetimePurchase(activity)
    }

    fun toggleEmptyScansFilter() {
        val newValue = !showEmptyScansFlow.value
        showEmptyScansFlow.value = newValue
        _uiState.update { it.copy(showEmptyScans = newValue) }
    }

    fun onClearAllScansConfirmed() {
        viewModelScope.launch {
            scanRepository.deleteAllScannedImages()
            val strings = _uiState.value.strings
            // Keep lastSuccessfulSyncTimeMs and other sync history - only clear session data
            _uiState.update {
                it.copy(
                    syncStatusMessage = buildClearAllStatusMessage(strings),
                    sessionDocumentCount = 0,
                    syncStartTimeMs = 0L,
                    syncStartTimestamp = 0L,
                    syncResumeIndex = 0,
                    syncProcessed = 0,
                    syncTotal = 0,
                    sessionElapsedSeconds = 0
                    // lastSuccessfulSyncTimeMs is preserved intentionally
                )
            }
        }
    }

    fun onToggleScreenshotsOnly(enabled: Boolean) {
        viewModelScope.launch {
            android.util.Log.d("MainViewModel", "Toggling screenshotsOnly to: $enabled")
            preferencesManager.setSyncScreenshotsOnly(enabled)
        }
    }

    fun onSwitchToAllGalleryAndSync() {
        viewModelScope.launch {
            android.util.Log.d("MainViewModel", "Switching to all gallery and starting sync...")
            preferencesManager.setSyncScreenshotsOnly(false)
            delay(100)
            if (_uiState.value.hasMediaPermission && _uiState.value.isSyncAllowed) {
                syncGallery(startIndex = 0, restart = true)
            }
        }
    }

    private fun observeImages() {
        viewModelScope.launch {
            combine(
                searchQuery,
                showEmptyScansFlow,
                scanRepository.observeScannedImages("")
            ) { query, showEmpty, allImages ->
                android.util.Log.d("MainViewModel", "observeImages triggered: query='$query', showEmpty=$showEmpty, allImages.size=${allImages.size}")
                val searchFiltered = if (query.isBlank()) {
                    allImages
                } else {
                    allImages.filter {
                        it.extractedText.contains(query, ignoreCase = true) ||
                            it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                    }
                }
                val finalFiltered = if (showEmpty) {
                    searchFiltered
                } else {
                    searchFiltered.filter { it.extractedText.isNotBlank() }
                }
                android.util.Log.d("MainViewModel", "observeImages result: finalFiltered.size=${finalFiltered.size}")
                Triple(query, finalFiltered, showEmpty)
            }.collect { (query, filteredImages, showEmpty) ->
                _uiState.update {
                    it.copy(
                        searchQuery = query,
                        images = filteredImages,
                        showEmptyScans = showEmpty
                    )
                }
                android.util.Log.d("MainViewModel", "UI state updated with ${filteredImages.size} images")
            }
        }
    }

    private fun observePaywallState() {
        viewModelScope.launch {
            combine(
                billingRepository.observeIsPremium(),
                scanRepository.observeScannedCount(),
                billingRepository.observeSubscriptionType(),
                preferencesManager.scanCountFlow,
                preferencesManager.syncScreenshotsOnlyFlow
            ) { isPremium, scannedCount, subscriptionType, trialScans, screenshotsOnly ->
                val showPaywall = subscriptionType == SubscriptionType.FREE && trialScans >= FREE_TRIAL_SCAN_LIMIT
                val isSyncAllowed = !showPaywall
                Sextuple(isPremium, scannedCount, trialScans, showPaywall, isSyncAllowed, screenshotsOnly)
            }.collect { (isPremium, scannedCount, trialScans, showPaywall, isSyncAllowed, screenshotsOnly) ->
                _uiState.update {
                    it.copy(
                        isPremium = isPremium,
                        scannedCount = scannedCount,
                        trialScansUsed = trialScans,
                        trialScansRemaining = (FREE_TRIAL_SCAN_LIMIT - trialScans).coerceAtLeast(0),
                        trialScansLimit = FREE_TRIAL_SCAN_LIMIT,
                        showPaywall = showPaywall,
                        isSyncAllowed = isSyncAllowed,
                        screenshotsOnly = screenshotsOnly
                    )
                }
            }
        }
    }

    private fun syncGallery(startIndex: Int = 0, restart: Boolean = false) {
        if (syncJob?.isActive == true) {
            android.util.Log.w("MainViewModel", "syncGallery already running, skipping")
            return
        }

        val startStrings = _uiState.value.strings
        val syncStartTime = if (restart || _uiState.value.syncStartTimestamp == 0L) {
            System.currentTimeMillis()
        } else {
            _uiState.value.syncStartTimestamp
        }

        // Active run start (not whole session start) to avoid counting paused idle time.
        val runStartTimeMs = System.currentTimeMillis()
        val sessionScannedBase = if (restart) 0 else _uiState.value.sessionDocumentCount
        val sessionElapsedBase = if (restart) 0 else _uiState.value.sessionElapsedSeconds

        syncJob = viewModelScope.launch {
            android.util.Log.d("MainViewModel", "syncGallery starting from index=$startIndex, restart=$restart, syncStartTime=$syncStartTime")
            _uiState.update {
                it.copy(
                    isSyncing = true,
                    isLoading = true,
                    syncPhase = SyncPhase.RUNNING,
                    syncStatusMessage = startStrings.syncingGallery,
                    syncResumeIndex = if (restart) 0 else it.syncResumeIndex,
                    syncProcessed = if (restart) 0 else it.syncProcessed,
                    syncTotal = if (restart) 0 else it.syncTotal,
                    syncStartTimestamp = syncStartTime,
                    syncStartTimeMs = runStartTimeMs,
                    sessionDocumentCount = sessionScannedBase,
                    sessionElapsedSeconds = sessionElapsedBase
                )
            }

            try {
                val result = syncGalleryUseCase(
                    startIndex = startIndex,
                    onProgress = { processed, total, nextIndex ->
                        _uiState.update {
                            it.copy(
                                syncProcessed = processed,
                                syncTotal = total,
                                syncResumeIndex = nextIndex,
                                // Keep previous session amount and add current run progress.
                                sessionDocumentCount = (sessionScannedBase + processed).coerceAtLeast(0)
                            )
                        }
                    }
                )

                android.util.Log.d("MainViewModel", "syncGalleryUseCase completed: inserted=${result.inserted}, skipped=${result.skipped}, paywall=${result.paywallReached}, empty=${result.screenshotsFolderEmpty}, nextIndex=${result.nextIndex}, total=${result.totalCount}, completed=${result.completed}")
                val message = buildSyncStatusMessage(_uiState.value.strings, result)
                val completedAtMs = System.currentTimeMillis()
                val runElapsedSeconds = ((completedAtMs - runStartTimeMs) / 1000L).toInt().coerceAtLeast(0)
                val accumulatedElapsed = (sessionElapsedBase + runElapsedSeconds).coerceAtLeast(0)
                val completedSessionScanned = (sessionScannedBase + result.inserted).coerceAtLeast(sessionScannedBase)
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        isLoading = false,
                        syncPhase = SyncPhase.IDLE,
                        syncStatusMessage = message,
                        syncResumeIndex = if (result.completed) 0 else result.nextIndex,
                        syncProcessed = if (result.completed) 0 else result.nextIndex,
                        syncTotal = if (result.completed) 0 else result.totalCount,
                        syncStartTimestamp = if (result.completed) 0L else syncStartTime,
                        syncStartTimeMs = 0L,
                        sessionElapsedSeconds = accumulatedElapsed,
                        sessionDocumentCount = maxOf(it.sessionDocumentCount, completedSessionScanned),
                        lastSuccessfulSyncTimeMs = if (result.completed) completedAtMs else it.lastSuccessfulSyncTimeMs,
                        lastSyncResultForStatus = result
                    )
                }
                if (result.completed) {
                    preferencesManager.setLastSuccessfulSyncTimeMillis(completedAtMs)
                    preferencesManager.setLastSummaryMetrics(
                        scannedCount = maxOf(_uiState.value.sessionDocumentCount, completedSessionScanned),
                        elapsedSeconds = accumulatedElapsed
                    )
                }
            } catch (_: CancellationException) {
                val pausedStrings = _uiState.value.strings
                val pausedMessage = "${pausedStrings.syncingGallery} (${_uiState.value.syncProcessed}/${_uiState.value.syncTotal})"
                val pausedAtMs = System.currentTimeMillis()
                val runElapsedSeconds = ((pausedAtMs - runStartTimeMs) / 1000L).toInt().coerceAtLeast(0)
                val accumulatedElapsed = (sessionElapsedBase + runElapsedSeconds).coerceAtLeast(0)
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        isLoading = false,
                        syncPhase = SyncPhase.PAUSED,
                        syncStatusMessage = pausedMessage,
                        syncStartTimeMs = 0L,
                        sessionElapsedSeconds = accumulatedElapsed
                    )
                }
            } finally {
                syncJob = null
            }
        }
    }
}

enum class SyncPhase {
    IDLE,
    RUNNING,
    PAUSED
}

internal fun buildSyncStatusMessage(strings: StringResources, result: SyncGalleryResult): String {
    return when {
        result.screenshotsFolderEmpty -> strings.screenshotsFolderEmpty
        result.paywallReached -> strings.freeLimitReached.format(
            strings.syncedLabel, result.inserted,
            strings.skippedLabel.lowercase(), result.skipped
        )
        else -> strings.syncComplete.format(
            strings.syncedLabel, result.inserted,
            strings.skippedLabel.lowercase(), result.skipped
        )
    }
}

internal fun buildClearAllStatusMessage(strings: StringResources): String {
    return strings.clearAllScansSuccess
}

data class MainUiState(
    val searchQuery: String = "",
    val images: List<ScannedImage> = emptyList(),
    val scannedCount: Int = 0,
    val trialScansUsed: Int = 0,
    val trialScansRemaining: Int = 300,
    val trialScansLimit: Int = 300,
    val isPremium: Boolean = false,
    val showPaywall: Boolean = false,
    val hasMediaPermission: Boolean = false,
    val isSyncing: Boolean = false,
    val isLoading: Boolean = false,
    val syncStatusMessage: String = "",
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val strings: StringResources = getStrings(AppLanguage.ENGLISH),
    val showEmptyScans: Boolean = true,
    val isSyncAllowed: Boolean = true,
    val screenshotsOnly: Boolean = true,
    val syncPhase: SyncPhase = SyncPhase.IDLE,
    val syncResumeIndex: Int = 0,
    val syncProcessed: Int = 0,
    val syncTotal: Int = 0,
    val syncStartTimestamp: Long = 0L,
    val sessionDocumentCount: Int = 0,
    val syncStartTimeMs: Long = 0L,
    val sessionElapsedSeconds: Int = 0,
    val lastSuccessfulSyncTimeMs: Long = 0L,
    val lastSyncResultForStatus: SyncGalleryResult? = null
)

// Small tuple helper to keep combine readable.
private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

private data class Sextuple<A, B, C, D, E, F>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F
)
