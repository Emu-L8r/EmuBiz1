package com.emul8r.bizap.ui.state

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.AuthState
import com.emul8r.bizap.domain.service.AuthenticationManager
import com.emul8r.bizap.ui.landing.GuiMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private val KEY_GUI_MODE = stringPreferencesKey("gui_mode")
private val KEY_FIRST_LAUNCH_WARNING_SHOWN = booleanPreferencesKey("first_launch_warning_shown")

/**
 * Single source of truth for all top-level UI state in [com.emul8r.bizap.MainActivity].
 *
 * Combines [AuthenticationManager] session state with DataStore preferences
 * (first-launch warning flag and GUI mode selection) into a single [AppState]
 * sealed class. MainActivity observes [appState] and renders exactly one composable
 * per state, eliminating the multiple-screen transitions caused by the previous
 * four-layer conditional approach.
 *
 * The initial value is [AppState.SplashLoading], which persists until DataStore
 * has completed its first read. This removes the previous hardcoded 2500 ms splash
 * delay while still ensuring the branded splash is visible during actual data loading.
 */
@HiltViewModel
class AppStateViewModel @Inject constructor(
    private val authManager: AuthenticationManager,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    /** Mutable auth state, updated synchronously on construction and on demand. */
    private val authStateFlow = MutableStateFlow<AuthState>(authManager.checkSessionValidity())

    /** DataStore-backed first-launch warning flag. */
    private val warningShownFlow = dataStore.data
        .map { prefs -> prefs[KEY_FIRST_LAUNCH_WARNING_SHOWN] ?: false }

    /** DataStore-backed GUI mode selection. */
    private val selectedModeFlow = dataStore.data
        .map { prefs ->
            val stored = prefs[KEY_GUI_MODE] ?: return@map null
            try {
                GuiMode.valueOf(stored)
            } catch (e: IllegalArgumentException) {
                Timber.w("AppStateViewModel: unknown stored GUI mode '$stored', resetting")
                null
            }
        }

    /**
     * Single stream of app UI state derived by combining all individual state sources.
     *
     * Starts as [AppState.SplashLoading] and transitions to the correct state once
     * DataStore has emitted its first values (i.e. once [combine] produces its first
     * combined result). No hardcoded delays are involved.
     */
    val appState: StateFlow<AppState> = combine(
        authStateFlow,
        warningShownFlow,
        selectedModeFlow
    ) { authState, warningShown, selectedMode ->
        computeAppState(authState, warningShown, selectedMode)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppState.SplashLoading
    )

    // -------------------------------------------------------------------------
    // State computation
    // -------------------------------------------------------------------------

    private fun computeAppState(
        authState: AuthState,
        warningShown: Boolean,
        selectedMode: GuiMode?
    ): AppState = when (authState) {
        is AuthState.NotInitialized -> AppState.PINSetup
        is AuthState.Authenticated -> when {
            !warningShown -> AppState.FirstLaunchWarning
            selectedMode == null -> AppState.GUISelection
            else -> AppState.AppReady(selectedMode)
        }
        else -> AppState.Login
    }

    // -------------------------------------------------------------------------
    // Actions called by composables
    // -------------------------------------------------------------------------

    /**
     * Re-evaluates auth state after PIN setup or successful login.
     * Triggers recomposition of any collector of [appState].
     */
    fun refreshAuth() {
        authStateFlow.value = authManager.checkSessionValidity()
    }

    /** Persists acknowledgement of the first-launch data-loss warning. */
    fun markFirstLaunchWarningShown() {
        viewModelScope.launch {
            dataStore.edit { it[KEY_FIRST_LAUNCH_WARNING_SHOWN] = true }
        }
    }

    /**
     * Persists the user's GUI mode selection, immediately transitioning
     * [appState] to [AppState.AppReady] without launching a new activity.
     */
    fun selectGui(mode: GuiMode) {
        Timber.d("AppStateViewModel: user selected $mode")
        viewModelScope.launch {
            dataStore.edit { it[KEY_GUI_MODE] = mode.name }
        }
    }

    /**
     * Clears the persisted GUI mode, transitioning [appState] back to
     * [AppState.GUISelection] so the landing screen is shown again.
     */
    fun resetGuiMode() {
        viewModelScope.launch {
            dataStore.edit { it.remove(KEY_GUI_MODE) }
        }
    }
}
