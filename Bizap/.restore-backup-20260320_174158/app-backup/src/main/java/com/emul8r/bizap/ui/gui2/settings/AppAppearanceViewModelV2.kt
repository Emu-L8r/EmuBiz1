package com.emul8r.bizap.ui.gui2.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.DisplayMode
import com.emul8r.bizap.domain.model.ThemePreference
import com.emul8r.bizap.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AppAppearanceViewModelV2 @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    data class AppSettingsUiState(
        val themePreference: ThemePreference = ThemePreference.AUTO,
        val displayMode: DisplayMode = DisplayMode.LIST_VIEW,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(AppSettingsUiState(isLoading = true))
    val uiState: StateFlow<AppSettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                settingsRepository.settings.collect { settings ->
                    _uiState.update {
                        it.copy(
                            themePreference = settings.themePreference,
                            displayMode = settings.displayMode,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load app settings")
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun updateThemePreference(preference: ThemePreference) {
        viewModelScope.launch {
            try {
                settingsRepository.updateThemePreference(preference)
                _uiState.update { it.copy(themePreference = preference) }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update theme preference")
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateDisplayMode(mode: DisplayMode) {
        viewModelScope.launch {
            try {
                settingsRepository.updateDisplayMode(mode)
                _uiState.update { it.copy(displayMode = mode) }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update display mode")
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
