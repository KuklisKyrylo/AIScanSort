package com.smartscan.ai.ui.detail

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartscan.ai.data.media.MediaStoreImageSource
import com.smartscan.ai.data.preferences.PreferencesManager
import com.smartscan.ai.domain.model.AppLanguage
import com.smartscan.ai.domain.model.ScannedImage
import com.smartscan.ai.domain.repository.ScanRepository
import com.smartscan.ai.ui.strings.StringResources
import com.smartscan.ai.ui.strings.getStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class ImageDetailViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val mediaStoreImageSource: MediaStoreImageSource,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // Initialize with current language from preferences
    private val initialLanguage = try {
        runBlocking { preferencesManager.languageFlow.first() }
    } catch (_: Exception) {
        AppLanguage.ENGLISH
    }

    private val _uiState = MutableStateFlow(ImageDetailUiState(
        currentLanguage = initialLanguage,
        strings = getStrings(initialLanguage)
    ))
    val uiState: StateFlow<ImageDetailUiState> = _uiState.asStateFlow()

    init {
        // Listen for language changes
        viewModelScope.launch {
            preferencesManager.languageFlow.collect { language ->
                _uiState.update {
                    it.copy(
                        currentLanguage = language,
                        strings = getStrings(language)
                    )
                }
            }
        }
    }

    fun loadImage(imageId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            scanRepository.observeScannedImages("").collect { images ->
                val image = images.find { it.id == imageId }
                val bitmap = image?.let { mediaStoreImageSource.loadBitmap(it.uri) }
                val photoCreatedAt = image?.let { mediaStoreImageSource.loadPhotoCreatedAtEpochMillis(it.uri) }

                _uiState.update {
                    it.copy(
                        image = image,
                        bitmap = bitmap,
                        photoCreatedAtEpochMillis = photoCreatedAt,
                        isLoading = false
                    )
                }

                if (image != null) {
                    return@collect
                }
            }
        }
    }

    fun deleteImage(imageId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            scanRepository.deleteScannedImage(imageId)
            onComplete()
        }
    }
}

data class ImageDetailUiState(
    val image: ScannedImage? = null,
    val bitmap: Bitmap? = null,
    val photoCreatedAtEpochMillis: Long? = null,
    val isLoading: Boolean = false,
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val strings: StringResources = getStrings(AppLanguage.ENGLISH)
)
