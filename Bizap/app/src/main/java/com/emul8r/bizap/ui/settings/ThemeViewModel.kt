package com.emul8r.bizap.ui.settings

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.ThemeRepository
import com.emul8r.bizap.domain.model.ThemeConfig
import com.emul8r.bizap.ui.theme.ThemePreset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
                    Log.d("ThemeViewModel", "System dark mode: $systemDarkMode")
                    themeRepository.updateDarkMode(systemDarkMode)
                }
            } catch (e: Exception) {
                Log.e("ThemeViewModel", "Error detecting system dark mode: ${e.message}", e)
            }
        }
    }

    /**
     * Apply a preset theme
     */
    fun applyPreset(preset: ThemePreset) {
        viewModelScope.launch {
            try {
                Log.d("ThemeViewModel", "Applying preset: ${preset.name}")
                themeRepository.updateSeedColor(preset.colorHex)
                themeRepository.updateDarkMode(preset.isDarkModePreset)
                Log.d("ThemeViewModel", "Preset applied successfully")
            } catch (e: Exception) {
                Log.e("ThemeViewModel", "Error applying preset: ${e.message}", e)
            }
        }
    }

    fun updateSeedColor(hex: String) {
        viewModelScope.launch {
            try {
                Log.d("ThemeViewModel", "Attempting to save color: $hex")
                themeRepository.updateSeedColor(hex)
                Log.d("ThemeViewModel", "Color saved successfully to DataStore")
            } catch (e: Exception) {
                Log.e("ThemeViewModel", "Error saving color: ${e.message}", e)
            }
        }
    }

    fun updateDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            try {
                Log.d("ThemeViewModel", "Attempting to save dark mode: $isDark")
                themeRepository.updateDarkMode(isDark)
                Log.d("ThemeViewModel", "Dark mode saved successfully to DataStore")
            } catch (e: Exception) {
                Log.e("ThemeViewModel", "Error saving dark mode: ${e.message}", e)
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
            Log.e("ThemeViewModel", "Error checking system dark mode: ${e.message}")
            false
        }
    }
}
