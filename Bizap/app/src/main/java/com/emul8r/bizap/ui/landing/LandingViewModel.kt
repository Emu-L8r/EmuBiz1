package com.emul8r.bizap.ui.landing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private val KEY_GUI_MODE = stringPreferencesKey("gui_mode")
private val KEY_FIRST_LAUNCH_WARNING_SHOWN = booleanPreferencesKey("first_launch_warning_shown")

/**
 * ViewModel for the landing screen.
 * Persists the user's GUI selection (GUI1 or GUI2) via DataStore so the choice
 * is remembered across app restarts.
 */
@HiltViewModel
class LandingViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    /** Currently selected GUI mode; null while loading from DataStore. */
    val selectedMode: StateFlow<GuiMode?> = dataStore.data
        .map { prefs ->
            val stored = prefs[KEY_GUI_MODE] ?: return@map null
            try {
                GuiMode.valueOf(stored)
            } catch (e: IllegalArgumentException) {
                Timber.w("LandingViewModel: unknown stored GUI mode '$stored', resetting")
                null
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    /** Persist the user's GUI selection. */
    fun selectMode(mode: GuiMode) {
        Timber.d("LandingViewModel: user selected $mode")
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_GUI_MODE] = mode.name
            }
        }
    }

    /** Clear the stored mode so the landing screen is shown again on next launch. */
    fun resetMode() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs.remove(KEY_GUI_MODE)
            }
        }
    }

    /** Whether the first-launch data-loss warning has been shown and acknowledged.
     *  Null while loading from DataStore; false = never shown; true = already shown. */
    val firstLaunchWarningShown: StateFlow<Boolean?> = dataStore.data
        .map { prefs -> prefs[KEY_FIRST_LAUNCH_WARNING_SHOWN] }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null  // null while DataStore hasn't emitted yet
        )

    /** Mark the first-launch warning as acknowledged so it never shows again. */
    fun markFirstLaunchWarningShown() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_FIRST_LAUNCH_WARNING_SHOWN] = true
            }
        }
    }
}
