package com.emul8r.bizap.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.emul8r.bizap.domain.model.ThemePreference
import com.emul8r.bizap.presentation.viewmodel.SettingsViewModel
import com.emul8r.bizap.ui.theme.AppTheme
import com.emul8r.bizap.ui.theme.ClassicTheme
import com.emul8r.bizap.ui.theme.ModernTheme
import com.emul8r.bizap.ui.theme.ThemeManager

/**
 * Root theme wrapper for the Bizap app.
 * 
 * This composable handles two layers of theming:
 * 1. Theme Style: CLASSIC (Material Design 2) vs MODERN (Material Design 3) - managed by ThemeManager
 * 2. Theme Mode: LIGHT, DARK, or AUTO - managed by SettingsViewModel
 * 
 * Theme changes take effect immediately without requiring app restart.
 * 
 * @param themeManager Manages the app theme style (Classic/Modern)
 * @param content The app content to wrap with the selected theme
 */
@Composable
fun BizapApp(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    // Load saved theme on first composition
    LaunchedEffect(Unit) {
        themeManager.loadSavedTheme()
    }

    // Observe current theme style (Classic/Modern)
    val appTheme by themeManager.theme.collectAsStateWithLifecycle()
    
    // Observe theme mode (Light/Dark/Auto) from settings
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()

    // Resolve whether to use dark mode based on user preference
    val isDarkMode = when (settings.themePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.AUTO -> isSystemDark
    }

    // Apply the selected theme style with the resolved dark/light mode
    when (appTheme) {
        AppTheme.CLASSIC -> ClassicTheme(isDarkMode = isDarkMode, content = content)
        AppTheme.MODERN -> ModernTheme(isDarkMode = isDarkMode, content = content)
    }
}
