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
import com.emul8r.bizap.domain.settings.UIPreferences
import com.emul8r.bizap.ui.landing.GuiMode
import com.emul8r.bizap.domain.model.UIMode
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
 *
 * As of v2.0, GUI2 is the only supported interface. GUI1 is no longer offered
 * and the GUI selection screen is never shown. Any previously stored GUI1 preference
 * is silently upgraded to GUI2.
 */
@HiltViewModel
class AppStateViewModel @Inject constructor(
    private val authManager: AuthenticationManager,
    private val dataStore: DataStore<Preferences>,
    private val uiPreferences: UIPreferences
) : ViewModel() {

    /** Mutable auth state, updated synchronously on construction and on demand. */
    private val authStateFlow = MutableStateFlow<AuthState>(AuthState.SessionExpired)

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

    /** Current UI density mode (MODERN / COMPACT). */
    val uiMode: StateFlow<UIMode> = uiPreferences.uiMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UIMode.DEFAULT
        )

    /** Persists the chosen [UIMode] to DataStore. */
    fun setUIMode(mode: UIMode) {
        viewModelScope.launch {
            uiPreferences.setUIMode(mode)
        }
    }

    init {
        // Refresh auth state asynchronously after initialization
        // This prevents blocking on DataStore read during ViewModel creation
        viewModelScope.launch {
            try {
                val state = authManager.checkSessionValidity()
                authStateFlow.value = state
                Timber.d("✅ Initial auth state loaded: $state")
            } catch (e: Exception) {
                Timber.e(e, "❌ Error checking session validity during init")
                authStateFlow.value = AuthState.SessionExpired
            }
        }
    }

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
            // GUI2 is now the only supported interface. GUI1 is no longer offered.
            // Any stored preference (including null or GUI1) defaults to GUI2.
            else -> AppState.AppReady(GuiMode.GUI2)
        }
        else -> AppState.Login
    }

    // -------------------------------------------------------------------------
    // Actions called by composables
    // -------------------------------------------------------------------------

    /**
     * Re-evaluates auth state after PIN setup or successful login.
     * Triggers recomposition of any collector of [appState].
     *
     * Now runs asynchronously to avoid blocking on DataStore operations.
     */
    fun refreshAuth() {
        viewModelScope.launch {
            try {
                val newState = authManager.checkSessionValidity()
                authStateFlow.value = newState
                Timber.d("✅ Auth state refreshed after PIN setup/login: $newState")
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to refresh auth state")
                authStateFlow.value = AuthState.SessionExpired
            }
        }
    }

    /** Persists acknowledgement of the first-launch data-loss warning. */
    fun markFirstLaunchWarningShown() {
        viewModelScope.launch {
            dataStore.edit { it[KEY_FIRST_LAUNCH_WARNING_SHOWN] = true }
        }
    }

    /**
     * No-op in v2.0. GUI2 is always used; the GUI mode preference is ignored.
     * Kept for backward compatibility with code that may call this method.
     */
    fun selectGui(mode: GuiMode) {
        Timber.d("AppStateViewModel: selectGui($mode) called — GUI2 is always used in v2.0")
    }

    /**
     * No-op in v2.0. GUI2 is always used; there is no GUI selection screen to return to.
     * Kept for backward compatibility with code that may call this method.
     */
    fun resetGuiMode() {
        Timber.d("AppStateViewModel: resetGuiMode() called — no-op in v2.0")
    }
}
