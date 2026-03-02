package com.emul8r.bizap.ui.settings

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.ThemeRepository
import com.emul8r.bizap.domain.model.ThemeConfig
import com.emul8r.bizap.ui.theme.ThemePreset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val application: Application
) : ViewModel() {

    val themeConfig = themeRepository.themeConfig.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeConfig()
    )

    init {
        // Auto-detect system dark mode preference on startup
        detectSystemDarkMode()
    }

    /**
     * Detect and apply system dark mode preference
     */
    private fun detectSystemDarkMode() {
        viewModelScope.launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val systemDarkMode = isSystemDarkMode(application)
                    Timber.d("System dark mode: $systemDarkMode")
                    themeRepository.updateDarkMode(systemDarkMode)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error detecting system dark mode: ${e.message}")
            }
        }
    }

    /**
     * Apply a preset theme
     */
    fun applyPreset(preset: ThemePreset) {
        viewModelScope.launch {
            try {
                Timber.d("Applying preset: ${preset.name}")
                themeRepository.updateSeedColor(preset.colorHex)
                themeRepository.updateDarkMode(preset.isDarkModePreset)
                Timber.d("Preset applied successfully")
            } catch (e: Exception) {
                Timber.e(e, "Error applying preset: ${e.message}")
            }
        }
    }

    fun updateSeedColor(hex: String) {
        viewModelScope.launch {
            try {
                Timber.d("Attempting to save color: $hex")
                themeRepository.updateSeedColor(hex)
                Timber.d("Color saved successfully to DataStore")
            } catch (e: Exception) {
                Timber.e(e, "Error saving color: ${e.message}")
            }
        }
    }

    fun updateDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            try {
                Timber.d("Attempting to save dark mode: $isDark")
                themeRepository.updateDarkMode(isDark)
                Timber.d("Dark mode saved successfully to DataStore")
            } catch (e: Exception) {
                Timber.e(e, "Error saving dark mode: ${e.message}")
            }
        }
    }

    /**
     * Check if system dark mode is enabled (Android 10+)
     */
    private fun isSystemDarkMode(context: Context): Boolean {
        return try {
            val uiMode = context.resources.configuration.uiMode
            val nightMode = uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        } catch (e: Exception) {
            Timber.e("Error checking system dark mode: ${e.message}")
            false
        }
    }
}

