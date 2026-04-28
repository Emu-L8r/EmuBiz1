package com.emul8r.bizap.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.AuthState
import com.emul8r.bizap.domain.service.AuthenticationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Top-level authentication state manager for the entire application.
 *
 * **Purpose:**
 * Exposes current authentication state to MainActivity, which uses it to determine
 * which screen to display (Initial Setup → Login → Main App).
 *
 * **Architecture:**
 * - Single source of truth for app authentication state
 * - Observed by MainActivity for root navigation
 * - Delegates to AuthenticationManager for state checks
 * - Automatically refreshes state on ViewModel creation
 *
 * **Auth State Flow:**
 * ```
 * App Start
 *     ↓
 * AuthViewModel.init() calls refreshAuthState()
 *     ↓
 * authManager.checkSessionValidity()
 *     ↓
 * Returns one of: NeedsSetup, RequiresLogin, Authenticated, SessionExpired
 *     ↓
 * MainActivity observes authState and displays appropriate screen
 *     ↓
 * User goes through setup/login flow
 *     ↓
 * State updates automatically
 * ```
 *
 * **State Transitions:**
 * ```
 * NeedsSetup
 *     ↓ (User completes setup)
 * RequiresLogin
 *     ↓ (User enters PIN)
 * Authenticated
 *     ↓ (Session expires or logout)
 * SessionExpired → RequiresLogin
 * ```
 *
 * **Usage:**
 * ```kotlin
 * @Composable
 * fun MainActivity() {
 *     val viewModel: AuthViewModel = hiltViewModel()
 *     val authState by viewModel.authState.collectAsStateWithLifecycle()
 *
 *     when (authState) {
 *         AuthState.NeedsSetup -> PINSetupScreen()
 *         AuthState.RequiresLogin -> LoginScreen()
 *         AuthState.Authenticated -> MainApp()
 *         AuthState.SessionExpired -> SessionExpiredDialog()
 *     }
 * }
 * ```
 *
 * @param authManager Handles authentication state validation
 *
 * @see AuthState
 * @see AuthenticationManager
 * @see MainActivity
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthenticationManager
) : ViewModel() {

    /**
     * Current authentication state as reactive stream.
     *
     * **Initial value:** [AuthState.SessionExpired] (checked in init)
     *
     * **Emits updates when:**
     * - User completes PIN setup
     * - User logs in successfully
     * - Session expires
     * - User logs out
     *
     * **Observed by:** MainActivity for root navigation
     */
    private val _authState = MutableStateFlow<AuthState>(AuthState.SessionExpired)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Initialization block.
     *
     * Automatically checks session validity on ViewModel creation.
     * This ensures the correct authentication screen is shown when app starts.
     */
    init {
        refreshAuthState()
    }

    /**
     * Re-evaluates authentication state.
     *
     * Stays a plain `fun` so all call sites (composables, lifecycle observers) are
     * unchanged. Internally launches a [viewModelScope] coroutine so the now-suspend
     * [AuthenticationManager.checkSessionValidity] is called off the main thread,
     * eliminating the previous runBlocking-on-Dispatchers.Main DiskRead violation.
     */
    fun refreshAuthState() {
        viewModelScope.launch {
            _authState.value = authManager.checkSessionValidity()
        }
    }
}
