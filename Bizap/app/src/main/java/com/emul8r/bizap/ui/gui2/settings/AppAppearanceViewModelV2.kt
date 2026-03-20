package com.emul8r.bizap.ui.gui2.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.DisplayMode
import com.emul8r.bizap.domain.model.ThemePreference
import com.emul8r.bizap.domain.repository.SettingsRepository
import com.emul8r.bizap.ui.theme.AppTheme
import com.emul8r.bizap.ui.theme.ThemeManager
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
    private val settingsRepository: SettingsRepository,
    private val themeManager: ThemeManager
) : ViewModel() {

    data class AppSettingsUiState(
        val themePreference: ThemePreference = ThemePreference.AUTO,
        val themeStyle: AppTheme = AppTheme.MODERN,
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
                // Combine settings and theme style into a single flow to avoid race conditions
                kotlinx.coroutines.flow.combine(
                    settingsRepository.settings,
                    themeManager.theme
                ) { settings, themeStyle ->
                    AppSettingsUiState(
                        themePreference = settings.themePreference,
                        themeStyle = themeStyle,
                        displayMode = settings.displayMode,
                        isLoading = false
                    )
                }.collect { newState ->
                    _uiState.value = newState
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
                // State will be updated reactively via the combine flow
            } catch (e: Exception) {
                Timber.e(e, "Failed to update theme preference")
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateThemeStyle(style: AppTheme) {
        viewModelScope.launch {
            try {
                themeManager.setTheme(style)
                // State will be updated reactively via the combine flow
            } catch (e: Exception) {
                Timber.e(e, "Failed to update theme style")
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateDisplayMode(mode: DisplayMode) {
        viewModelScope.launch {
            try {
                settingsRepository.updateDisplayMode(mode)
                // State will be updated reactively via the combine flow
            } catch (e: Exception) {
                Timber.e(e, "Failed to update display mode")
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
