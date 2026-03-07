package com.smartscan.ai.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartscan.ai.data.preferences.PreferencesManager
import com.smartscan.ai.domain.model.AppLanguage
import com.smartscan.ai.ui.strings.StringResources
import com.smartscan.ai.ui.strings.getStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                preferencesManager.languageFlow,
                preferencesManager.syncScreenshotsOnlyFlow
            ) { language, screenshotsOnly ->
                language to screenshotsOnly
            }.collect { (language, screenshotsOnly) ->
                _uiState.update {
                    it.copy(
                        currentLanguage = language,
                        strings = getStrings(language),
                        syncScreenshotsOnly = screenshotsOnly
                    )
                }
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferencesManager.setLanguage(language)
        }
    }

    fun setSyncScreenshotsOnly(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSyncScreenshotsOnly(enabled)
        }
    }
}

data class SettingsUiState(
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val strings: StringResources = getStrings(AppLanguage.ENGLISH),
    val syncScreenshotsOnly: Boolean = true
)
