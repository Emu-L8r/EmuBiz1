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
 * ViewModel that consolidates all user-preference management for the Settings screen.
 *
 * Exposes the complete [Settings] aggregate as a [StateFlow] and individual derived
 * [StateFlow]s for convenience so composables can observe only the slice they care about.
 * All write operations are launched on the [viewModelScope] and delegate to the
 * corresponding use-case; errors are logged via Timber but do not crash the app.
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

    // ── Aggregate state ────────────────────────────────────────────────────

    /** Full settings snapshot.  Initial value is the default [Settings] object. */
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
