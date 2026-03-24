package com.emul8r.bizap.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.AuthState
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.service.AuthenticationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for login screen.
 *
 * **Fields:**
 * - [pin]: Current PIN entry (masked in UI)
 * - [attemptCount]: Number of failed login attempts
 * - [lockoutSecondsRemaining]: Seconds until lockout expires (0 = no lockout)
 * - [errorMessage]: User-facing error message
 * - [isLoading]: True while authenticating
 * - [isAuthenticated]: True after successful login
 * - [showForgotPINDialog]: True to show forgot PIN modal
 *
 * **Lockout Policy:**
 * - After 3 failed attempts, user is locked out for 60 seconds
 * - Countdown timer automatically decrements
 * - Timer resets after successful login
 *
 * @property pin User's PIN entry
 * @property attemptCount Failed login attempts
 * @property lockoutSecondsRemaining Seconds until lockout lifts
 * @property errorMessage Error to display
 * @property isLoading Authentication in progress
 * @property isAuthenticated Login successful
 * @property showForgotPINDialog Show forgot PIN flow
 */
data class LoginUiState(
    val pin: String = "",
    val attemptCount: Int = 0,
    val lockoutSecondsRemaining: Long = 0L,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val showForgotPINDialog: Boolean = false
)

/**
 * Manages login screen state and authentication.
 *
 * **Architecture:**
 * - Manages PIN entry and validation
 * - Enforces lockout policy (3 failed attempts = 60s lockout)
 * - Handles forgot PIN flow
 * - Integrates with authentication manager
 * - Manages business profile context
 *
 * **Authentication Flow:**
 * ```
 * User enters PIN
 *     ↓
 * Validate format (6 digits)
 *     ↓
 * Call authManager.authenticate(pin)
 *     ↓
 * On success: Set isAuthenticated = true
 * On failure: Increment attemptCount, check for lockout
 * ```
 *
 * **Lockout Behavior:**
 * - After 3 failed attempts, lockout for 60 seconds
 * - UI is disabled during lockout (PIN input blocked)
 * - Countdown timer ticks every second
 * - Timer auto-stops when lockout expires
 *
 * **Usage:**
 * ```kotlin
 * @Composable
 * fun LoginScreen() {
 *     val viewModel: LoginViewModel = hiltViewModel()
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *     val businessProfile by viewModel.businessProfile.collectAsStateWithLifecycle()
 *
 *     when {
 *         uiState.isAuthenticated -> {
 *             // Navigate to main app
 *         }
 *         uiState.lockoutSecondsRemaining > 0 -> {
 *             Text("Locked out. Try again in ${uiState.lockoutSecondsRemaining}s")
 *         }
 *         else -> {
 *             PINPadInput(
 *                 onPINEntered = { viewModel.handlePINEntry(it) },
 *                 onForgotPIN = { viewModel.showForgotPINFlow() }
 *             )
 *         }
 *     }
 * }
 * ```
 *
 * **State Management:**
 * - PIN cleared after each attempt
 * - Attempt count resets after successful login
 * - Lockout timer auto-manages countdown
 *
 * @param authManager Handles PIN authentication
 * @param businessProfileRepository Provides business context
 *
 * @see AuthenticationManager
 * @see BusinessProfileRepository
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authManager: AuthenticationManager,
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    /**
     * Active business profile for the current user.
     *
     * Provides business context (name, logo, etc.) for login screen display.
     * Defaults to empty profile if not yet loaded.
     *
     * **Subscription:** WhileSubscribed with 5-second timeout
     */
    val businessProfile: StateFlow<BusinessProfile> = businessProfileRepository.activeProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BusinessProfile()
        )

    /**
     * Current login screen UI state.
     *
     * Contains all data needed to render login screen:
     * - PIN entry state
     * - Error messages
     * - Loading status
     * - Lockout countdown
     * - Authentication result
     */
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        startLockoutTimer()
    }

    fun onPINChanged(newPin: String) {
        if (_uiState.value.lockoutSecondsRemaining > 0) return
        _uiState.update { it.copy(pin = newPin.filter { c -> c.isDigit() }, errorMessage = null) }
    }

    fun onLoginClicked() {
        val state = _uiState.value
        if (state.pin.isEmpty() || state.isLoading || state.lockoutSecondsRemaining > 0) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authManager.authenticate(state.pin)
            result.fold(
                onSuccess = { authState ->
                    when (authState) {
                        is AuthState.Authenticated -> {
                            _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                        }
                        is AuthState.InvalidPIN -> {
                            val newCount = state.attemptCount + 1
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    pin = "",
                                    attemptCount = newCount,
                                    errorMessage = "Incorrect PIN, try again (attempt $newCount of ${AuthenticationManager.MAX_FAILED_ATTEMPTS})"
                                )
                            }
                        }
                        is AuthState.LockedOut -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    pin = "",
                                    lockoutSecondsRemaining = authState.remainingSeconds,
                                    errorMessage = "Too many attempts. Try again in ${authState.remainingSeconds}s"
                                )
                            }
                        }
                        else -> {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Authentication error: ${e.message}") }
                }
            )
        }
    }

    fun onForgotPINClicked() {
        _uiState.update { it.copy(showForgotPINDialog = true) }
    }

    fun onForgotPINConfirmed() {
        _uiState.update { it.copy(showForgotPINDialog = false) }
        authManager.resetPINAndData()
    }

    fun onForgotPINDismissed() {
        _uiState.update { it.copy(showForgotPINDialog = false) }
    }

    /** Ticks down the lockout timer every second so the UI stays in sync. */
    private fun startLockoutTimer() {
        viewModelScope.launch {
            while (isActive) {
                delay(TIMER_TICK_MS)
                val remaining = _uiState.value.lockoutSecondsRemaining
                if (remaining > 0) {
                    val newRemaining = maxOf(0L, remaining - 1)
                    _uiState.update {
                        it.copy(
                            lockoutSecondsRemaining = newRemaining,
                            errorMessage = if (newRemaining > 0) "Locked. Try again in ${newRemaining}s" else null
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val TIMER_TICK_MS = 1_000L
    }
}
