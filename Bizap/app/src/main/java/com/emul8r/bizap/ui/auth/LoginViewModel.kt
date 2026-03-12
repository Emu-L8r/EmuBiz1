package com.emul8r.bizap.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.AuthState
import com.emul8r.bizap.domain.service.AuthenticationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val pin: String = "",
    val attemptCount: Int = 0,
    val lockoutSecondsRemaining: Long = 0L,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val showForgotPINDialog: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authManager: AuthenticationManager
) : ViewModel() {

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
