package com.emul8r.bizap.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.repository.ThemeRepository
import com.emul8r.bizap.ui.components.theme.PresetTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Data class representing the current theme colors.
 */
data class ThemeColors(
    val primary: Color = Color(0xFF6200EE),
    val secondary: Color = Color(0xFF03DAC6),
    val tertiary: Color = Color(0xFF018786)
)

/**
 * ViewModel for unified theme settings.
 * Manages color customization, preset themes, and dark mode.
 *
 * This ViewModel works across both GUI1 and GUI2 to ensure
 * theme consistency throughout the app.
 */
@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val _themeState = MutableStateFlow(ThemeColors())
    val themeState: StateFlow<ThemeColors> = _themeState.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        loadThemeSettings()
    }

    /**
     * Load saved theme settings from repository.
     */
    private fun loadThemeSettings() {
        viewModelScope.launch {
            try {
                themeRepository.themeConfig.collect { config ->
                    Timber.d("Loading theme: seedColor=${config.seedColorHex}, darkMode=${config.isDarkMode}")
                    _isDarkMode.value = config.isDarkMode

                    // For now, use the seed color as primary
                    // In a full implementation, you'd extract all 4 colors from storage
                    val primaryColor = try {
                        Color(android.graphics.Color.parseColor(config.seedColorHex))
                    } catch (e: Exception) {
                        Color(0xFF6200EE)
                    }

                    _themeState.value = _themeState.value.copy(primary = primaryColor)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading theme settings")
            }
        }
    }

    /**
     * Set the primary color.
     */
    fun setPrimaryColor(color: Color) {
        _themeState.value = _themeState.value.copy(primary = color)
        Timber.d("Primary color set to: ${colorToHex(color)}")
    }

    /**
     * Set the secondary color.
     */
    fun setSecondaryColor(color: Color) {
        _themeState.value = _themeState.value.copy(secondary = color)
        Timber.d("Secondary color set to: ${colorToHex(color)}")
    }

    /**
     * Set the tertiary color.
     */
    fun setTertiaryColor(color: Color) {
        _themeState.value = _themeState.value.copy(tertiary = color)
        Timber.d("Tertiary color set to: ${colorToHex(color)}")
    }

    /**
     * Toggle dark mode.
     */
    fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
        Timber.d("Dark mode toggled: $isDark")
    }

    /**
     * Apply a preset theme.
     */
    fun applyPreset(preset: PresetTheme) {
        Timber.d("Applying preset theme: ${preset.name}")
        _themeState.value = ThemeColors(
            primary = preset.primary,
            secondary = preset.secondary,
            tertiary = preset.tertiary
        )
    }

    /**
     * Reset to default theme colors.
     */
    fun resetToDefaults() {
        Timber.d("Resetting to default theme colors")
        _themeState.value = ThemeColors(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6),
            tertiary = Color(0xFF018786)
        )
        _isDarkMode.value = false
    }

    /**
     * Save the current theme configuration.
     */
    fun saveTheme() {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                Timber.d("Saving theme: primary=${colorToHex(_themeState.value.primary)}, dark=${_isDarkMode.value}")

                // Save primary color as seed color
                themeRepository.updateSeedColor(colorToHex(_themeState.value.primary))
                themeRepository.updateDarkMode(_isDarkMode.value)

                // TODO: Extend repository to save all 4 colors (secondary, tertiary, background)

                _saveSuccess.value = true
                Timber.d("✅ Theme saved successfully")

                // Reset success message after 2 seconds
                kotlinx.coroutines.delay(2000)
                _saveSuccess.value = false
            } catch (e: Exception) {
                Timber.e(e, "❌ Error saving theme")
            } finally {
                _isSaving.value = false
            }
        }
    }

    companion object {
        fun colorToHex(color: Color): String {
            val alpha = (color.alpha * 255).toInt()
            val red = (color.red * 255).toInt()
            val green = (color.green * 255).toInt()
            val blue = (color.blue * 255).toInt()
            return String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
        }
    }
}

