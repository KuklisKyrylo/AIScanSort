package com.smartscan.ai.ui.main

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.smartscan.ai.data.preferences.PreferencesManager
import com.smartscan.ai.domain.billing.PaywallGate
import com.smartscan.ai.domain.model.AppLanguage
import com.smartscan.ai.domain.model.ScannedImage
import com.smartscan.ai.domain.model.SubscriptionType
import com.smartscan.ai.domain.repository.BillingRepository
import com.smartscan.ai.domain.repository.ScanRepository
import com.smartscan.ai.domain.usecase.SyncGalleryUseCase
import com.smartscan.ai.ui.base.BaseViewModel
import com.smartscan.ai.ui.strings.StringResources
import com.smartscan.ai.ui.strings.getStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    private val preferencesManager: PreferencesManager
) : BaseViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val showEmptyScansFlow = MutableStateFlow(false)

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

    init {
        billingRepository.refreshPurchases()
        // Listen for language changes
        viewModelScope.launch {
            preferencesManager.languageFlow.collect { language ->
                val strings = getStrings(language)
                _uiState.update {
                    it.copy(
                        currentLanguage = language,
                        strings = strings,
                        syncStatusMessage = ""
                    )
                }
            }
        }
        observeImages()
        observePaywallState()
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onMediaPermissionChanged(granted: Boolean) {
        _uiState.update { it.copy(hasMediaPermission = granted) }
        if (granted) {
            syncGallery()
        }
    }

    fun onSyncNowClick() {
        if (_uiState.value.hasMediaPermission) {
            syncGallery()
        }
    }

    fun onBuyProClick(activity: Activity) {
        billingRepository.launchProPurchase(activity)
    }

    fun toggleEmptyScansFilter() {
        val newValue = !showEmptyScansFlow.value
        showEmptyScansFlow.value = newValue
        _uiState.update { it.copy(showEmptyScans = newValue) }
    }

    private fun observeImages() {
        viewModelScope.launch {
            combine(
                searchQuery,
                showEmptyScansFlow,
                scanRepository.observeScannedImages("")
            ) { query, showEmpty, allImages ->
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
                Triple(query, finalFiltered, showEmpty)
            }.collect { (query, filteredImages, showEmpty) ->
                _uiState.update {
                    it.copy(
                        searchQuery = query,
                        images = filteredImages,
                        showEmptyScans = showEmpty
                    )
                }
            }
        }
    }

    private fun observePaywallState() {
        viewModelScope.launch {
            combine(
                billingRepository.observeIsPro(),
                scanRepository.observeScannedCount(),
                preferencesManager.subscriptionTypeFlow,
                preferencesManager.scanCountFlow
            ) { isPro, scannedCount, subscriptionType, trialScans ->
                val showPaywall = subscriptionType == SubscriptionType.FREE && trialScans >= 30
                Quadruple(isPro, scannedCount, trialScans, showPaywall)
            }.collect { (isPro, scannedCount, trialScans, showPaywall) ->
                _uiState.update {
                    it.copy(
                        isPro = isPro,
                        scannedCount = scannedCount,
                        trialScansUsed = trialScans,
                        trialScansRemaining = (30 - trialScans).coerceAtLeast(0),
                        showPaywall = showPaywall
                    )
                }
            }
        }
    }

    private fun syncGallery() {
        if (_uiState.value.isSyncing) return

        viewModelScope.launch {
            val strings = _uiState.value.strings
            _uiState.update { it.copy(isSyncing = true, syncStatusMessage = strings.syncingGallery) }
            val result = syncGalleryUseCase()
            val message = if (result.paywallReached) {
                strings.freeLimitReached.format(result.inserted, result.skipped)
            } else {
                strings.syncComplete.format(result.inserted, result.skipped)
            }
            _uiState.update { it.copy(isSyncing = false, syncStatusMessage = message) }
        }
    }
}

data class MainUiState(
    val searchQuery: String = "",
    val images: List<ScannedImage> = emptyList(),
    val scannedCount: Int = 0,
    val trialScansUsed: Int = 0,
    val trialScansRemaining: Int = 30,
    val isPro: Boolean = false,
    val showPaywall: Boolean = false,
    val hasMediaPermission: Boolean = false,
    val isSyncing: Boolean = false,
    val syncStatusMessage: String = "",
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val strings: StringResources = getStrings(AppLanguage.ENGLISH),
    val showEmptyScans: Boolean = false
)

// Small tuple helper to keep combine readable.
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
