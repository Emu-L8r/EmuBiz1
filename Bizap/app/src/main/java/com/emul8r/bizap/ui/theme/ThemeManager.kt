package com.emul8r.bizap.ui.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Theme style options for the app.
 * - CLASSIC: Material Design 2 style (traditional GUI look)
 * - MODERN: Material Design 3 style (modern GUI look)
 */
enum class AppTheme {
    CLASSIC,
    MODERN
}

/**
 * Manages the app's theme style (Classic vs Modern) and persists it across app restarts.
 * 
 * This is separate from the light/dark mode preference. Users can independently choose:
 * - Theme style: CLASSIC or MODERN (Material Design 2 vs 3)
 * - Theme mode: LIGHT, DARK, or AUTO (handled by ThemePreference)
 * 
 * @param dataStore DataStore for persisting theme preference
 */
@Singleton
class ThemeManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val APP_THEME_KEY = stringPreferencesKey("app_theme")
    }

    private val _theme = MutableStateFlow(AppTheme.MODERN)
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    /**
     * Loads the saved theme from DataStore when ThemeManager is first created.
     * Call this during app initialization.
     */
    suspend fun loadSavedTheme() {
        val savedTheme = dataStore.data
            .map { prefs ->
                prefs[APP_THEME_KEY]?.let { themeName ->
                    try {
                        AppTheme.valueOf(themeName)
                    } catch (e: IllegalArgumentException) {
                        AppTheme.MODERN // Default if invalid
                    }
                } ?: AppTheme.MODERN // Default if not set
            }
            .first()
        
        _theme.value = savedTheme
    }

    /**
     * Sets the theme and persists it to DataStore.
     * Theme changes take effect immediately without app restart.
     * 
     * @param newTheme The theme to apply (CLASSIC or MODERN)
     */
    suspend fun setTheme(newTheme: AppTheme) {
        _theme.value = newTheme
        dataStore.edit { prefs ->
            prefs[APP_THEME_KEY] = newTheme.name
        }
    }

    /**
     * Returns the current theme as a Flow for observing changes.
     */
    fun observeTheme(): Flow<AppTheme> = theme
}
