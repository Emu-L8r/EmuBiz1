package com.emul8r.bizap.ui.auth

import androidx.lifecycle.ViewModel
import com.emul8r.bizap.domain.model.AuthState
import com.emul8r.bizap.domain.service.AuthenticationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Top-level ViewModel responsible for exposing the current [AuthState] to
 * [MainActivity]. The activity observes [authState] to decide which root
 * composable to display (PINSetup, Login, or the main content).
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthenticationManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.SessionExpired)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        refreshAuthState()
    }

    /** Re-evaluates session validity and updates the exposed state. */
    fun refreshAuthState() {
        _authState.value = authManager.checkSessionValidity()
    }
}
