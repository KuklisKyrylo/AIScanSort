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
        observeLanguage()
    }

    private fun observeLanguage() {
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

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferencesManager.setLanguage(language)
        }
    }
}

data class SettingsUiState(
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val strings: StringResources = getStrings(AppLanguage.ENGLISH)
)

