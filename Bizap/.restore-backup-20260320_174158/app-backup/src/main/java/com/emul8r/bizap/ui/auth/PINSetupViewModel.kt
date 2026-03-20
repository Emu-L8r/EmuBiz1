package com.emul8r.bizap.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.AuthState
import com.emul8r.bizap.domain.service.AuthenticationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PINSetupUiState(
    val pin: String = "",
    val confirmPin: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isComplete: Boolean = false
)

@HiltViewModel
class PINSetupViewModel @Inject constructor(
    private val authManager: AuthenticationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PINSetupUiState())
    val uiState: StateFlow<PINSetupUiState> = _uiState.asStateFlow()

    fun onPINChanged(newPin: String) {
        _uiState.update { it.copy(pin = newPin.filter { c -> c.isDigit() }, errorMessage = null) }
    }

    fun onConfirmPINChanged(newPin: String) {
        _uiState.update { it.copy(confirmPin = newPin.filter { c -> c.isDigit() }, errorMessage = null) }
    }

    fun onSetupClicked() {
        val state = _uiState.value
        when {
            state.pin.length < AuthenticationManager.MIN_PIN_LENGTH -> {
                _uiState.update { it.copy(errorMessage = "PIN must be at least ${AuthenticationManager.MIN_PIN_LENGTH} digits") }
            }
            state.pin != state.confirmPin -> {
                _uiState.update { it.copy(errorMessage = "PINs do not match") }
            }
            else -> {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                viewModelScope.launch {
                    val result = authManager.setupInitialPIN(state.pin)
                    if (result.isSuccess) {
                        _uiState.update { it.copy(isLoading = false, isComplete = true) }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.exceptionOrNull()?.message ?: "Setup failed"
                            )
                        }
                    }
                }
            }
        }
    }
}
