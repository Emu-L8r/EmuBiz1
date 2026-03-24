package com.emul8r.bizap.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.DisplayMode
import com.emul8r.bizap.domain.model.Settings
import com.emul8r.bizap.domain.model.ThemePreference
import com.emul8r.bizap.domain.model.UiDensity
import com.emul8r.bizap.domain.repository.SettingsRepository
import com.emul8r.bizap.domain.usecase.settings.GetSettingsUseCase
import com.emul8r.bizap.domain.usecase.settings.ResetSettingsToDefaultUseCase
import com.emul8r.bizap.domain.usecase.settings.UpdateDisplayModeUseCase
import com.emul8r.bizap.domain.usecase.settings.UpdateNotificationSettingsUseCase
import com.emul8r.bizap.domain.usecase.settings.UpdateSyncSettingsUseCase
import com.emul8r.bizap.domain.usecase.settings.UpdateThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Manages all user preferences and settings operations.
 *
 * **Purpose:**
 * Consolidates all settings management in one place. Handles reading, updating, and resetting
 * user preferences across display, theme, notifications, and sync settings.
 *
 * **Architecture:**
 * - Exposes full [Settings] aggregate as [StateFlow]
 * - Provides derived [StateFlow]s for individual settings slices
 * - Delegates all operations to use-cases
 * - Errors logged but don't crash app
 * - Supports atomic updates and reset to defaults
 *
 * **Settings Categories:**
 * 1. **Theme:** Dark mode, light mode, system default
 * 2. **Display:** UI density, font size, visual preferences
 * 3. **Notifications:** Push notifications, email alerts, reminder preferences
 * 4. **Sync:** Auto-sync settings, sync frequency, data sync preferences
 *
 * **Data Flow:**
 * ```
 * Use Case (GetSettingsUseCase)
 *     ↓
 * Settings Repository
 *     ↓
 * StateFlow<Settings>
 *     ↓
 * Derived flows (theme, displayMode, etc.)
 *     ↓
 * UI observes relevant slices
 * ```
 *
 * **User Actions:**
 * - Change theme → updateTheme()
 * - Change display → updateDisplayMode()
 * - Notification preferences → updateNotifications()
 * - Sync preferences → updateSyncSettings()
 * - Reset all → resetToDefaults()
 *
 * **Usage:**
 * ```kotlin
 * @Composable
 * fun SettingsScreen() {
 *     val viewModel: SettingsViewModel = hiltViewModel()
 *     val theme by viewModel.theme.collectAsStateWithLifecycle()
 *     val displayMode by viewModel.displayMode.collectAsStateWithLifecycle()
 *
 *     Column {
 *         ThemeSelector(
 *             selected = theme,
 *             onThemeChange = { viewModel.updateTheme(it) }
 *         )
 *         DisplayModeSelector(
 *             selected = displayMode,
 *             onDisplayChange = { viewModel.updateDisplayMode(it) }
 *         )
 *         ResetButton { viewModel.resetToDefaults() }
 *     }
 * }
 * ```
 *
 * **State Persistence:**
 * - All changes persisted immediately to repository
 * - Repository backed by local database/preferences
 * - Settings survive app restarts
 * - Synced across devices (if backend available)
 *
 * @param getSettingsUseCase Retrieves current settings
 * @param settingsRepository Persists settings changes
 * @param updateThemeUseCase Handles theme updates
 * @param updateDisplayModeUseCase Handles display preference updates
 * @param updateNotificationSettingsUseCase Handles notification settings
 * @param updateSyncSettingsUseCase Handles sync preferences
 * @param resetSettingsToDefaultUseCase Resets all settings
 *
 * @see Settings
 * @see ThemePreference
 * @see DisplayMode
 * @see UiDensity
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase,
    private val settingsRepository: SettingsRepository,
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val updateDisplayModeUseCase: UpdateDisplayModeUseCase,
    private val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase,
    private val updateSyncSettingsUseCase: UpdateSyncSettingsUseCase,
    private val resetSettingsToDefaultUseCase: ResetSettingsToDefaultUseCase
) : ViewModel() {

    /**
     * Full settings snapshot as reactive state flow.
     *
     * Contains all user settings:
     * - Theme preference
     * - Display mode
     * - UI density
     * - Notification settings
     * - Sync preferences
     *
     * Initial value: Default [Settings] object
     * Subscription: Eager (starts immediately)
     *
     * **Updates:**
     * When user changes any setting, this flow emits new Settings object
     * with only that field changed.
     */
    val settings: StateFlow<Settings> = getSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Settings()
        )

    // ── Derived convenience flows ──────────────────────────────────────────

    val themePreference: StateFlow<ThemePreference> = settings
        .map { it.themePreference }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings().themePreference)

    val displayMode: StateFlow<DisplayMode> = settings
        .map { it.displayMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings().displayMode)

    val uiDensity: StateFlow<UiDensity> = settings
        .map { it.uiDensity }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings().uiDensity)

    val notificationsEnabled: StateFlow<Boolean> = settings
        .map { it.notificationsEnabled }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings().notificationsEnabled)

    val emailNotificationsEnabled: StateFlow<Boolean> = settings
        .map { it.emailNotificationsEnabled }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings().emailNotificationsEnabled)

    val autoSyncEnabled: StateFlow<Boolean> = settings
        .map { it.autoSyncEnabled }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings().autoSyncEnabled)

    val syncFrequencyMinutes: StateFlow<Int> = settings
        .map { it.syncFrequencyMinutes }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings().syncFrequencyMinutes)

    // ── Write helpers ──────────────────────────────────────────────────────

    fun setThemePreference(preference: ThemePreference) = launch("setThemePreference") {
        updateThemeUseCase(preference)
    }

    fun setDisplayMode(mode: DisplayMode) = launch("setDisplayMode") {
        updateDisplayModeUseCase(mode)
    }

    fun setUiDensity(density: UiDensity) = launch("setUiDensity") {
        settingsRepository.updateUiDensity(density)
    }

    fun setNotificationsEnabled(enabled: Boolean) = launch("setNotificationsEnabled") {
        updateNotificationSettingsUseCase.setNotificationsEnabled(enabled)
    }

    fun setEmailNotificationsEnabled(enabled: Boolean) = launch("setEmailNotificationsEnabled") {
        updateNotificationSettingsUseCase.setEmailNotificationsEnabled(enabled)
    }

    fun setAutoSyncEnabled(enabled: Boolean) = launch("setAutoSyncEnabled") {
        updateSyncSettingsUseCase.setAutoSyncEnabled(enabled)
    }

    fun setSyncFrequencyMinutes(minutes: Int) = launch("setSyncFrequencyMinutes") {
        updateSyncSettingsUseCase.setSyncFrequencyMinutes(minutes)
    }

    fun resetToDefaults() = launch("resetToDefaults") {
        resetSettingsToDefaultUseCase()
    }

    // ── Internal ───────────────────────────────────────────────────────────

    private fun launch(tag: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                Timber.e(e, "SettingsViewModel.$tag failed")
            }
        }
    }
}
