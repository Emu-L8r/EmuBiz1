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

/**
 * UI state for PIN setup screen (first-time authentication setup).
 *
 * **Fields:**
 * - [pin]: First PIN entry (filtered to digits only)
 * - [confirmPin]: PIN confirmation entry
 * - [errorMessage]: Validation or setup error to display
 * - [isLoading]: True while setting up PIN
 * - [isComplete]: True after successful setup
 *
 * **Validation Rules:**
 * - Minimum length: [AuthenticationManager.MIN_PIN_LENGTH] digits
 * - Pin and confirmPin must match exactly
 * - Both fields auto-filtered to digits only
 *
 * @property pin First PIN entry
 * @property confirmPin PIN confirmation
 * @property errorMessage Validation error message
 * @property isLoading Setup in progress
 * @property isComplete Setup successful
 */
data class PINSetupUiState(
    val pin: String = "",
    val confirmPin: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isComplete: Boolean = false
)

/**
 * Manages PIN setup screen state and authentication initialization.
 *
 * **Purpose:**
 * First-time PIN setup for new users. Handles initial authentication credential creation.
 *
 * **Architecture:**
 * - Manages PIN entry and confirmation
 * - Validates PIN format and matching
 * - Calls AuthenticationManager to store PIN
 * - Emits state for UI rendering
 *
 * **PIN Validation:**
 * ```
 * User enters PIN
 *     ↓
 * Auto-filter to digits (removes non-numeric)
 *     ↓
 * Check minimum length (MIN_PIN_LENGTH digits)
 *     ↓
 * Verify PIN matches confirmation
 *     ↓
 * Call authManager.setupInitialPIN(pin)
 *     ↓
 * On success: isComplete = true
 * On failure: Show error message
 * ```
 *
 * **Usage:**
 * ```kotlin
 * @Composable
 * fun PINSetupScreen() {
 *     val viewModel: PINSetupViewModel = hiltViewModel()
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *
 *     when {
 *         uiState.isComplete -> {
 *             // Navigate to main app
 *             LaunchedEffect(Unit) { navController.navigate(MainRoute) }
 *         }
 *         else -> {
 *             Column {
 *                 Text("Create your PIN")
 *                 PINInputField(
 *                     value = uiState.pin,
 *                     onValueChanged = { viewModel.onPINChanged(it) }
 *                 )
 *                 PINInputField(
 *                     value = uiState.confirmPin,
 *                     label = "Confirm PIN",
 *                     onValueChanged = { viewModel.onConfirmPINChanged(it) }
 *                 )
 *                 if (uiState.errorMessage != null) {
 *                     Text(uiState.errorMessage, color = Color.Red)
 *                 }
 *                 Button(
 *                     onClick = { viewModel.onSetupClicked() },
 *                     enabled = !uiState.isLoading
 *                 ) {
 *                     if (uiState.isLoading) LoadingIndicator() else Text("Setup PIN")
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param authManager Handles PIN storage and authentication
 *
 * @see AuthenticationManager
 * @see LoginViewModel
 */
@HiltViewModel
class PINSetupViewModel @Inject constructor(
    private val authManager: AuthenticationManager
) : ViewModel() {

    /**
     * Current PIN setup UI state.
     *
     * Contains all data for PIN setup form:
     * - Initial PIN entry
     * - Confirmation PIN entry
     * - Validation errors
     * - Loading state
     * - Completion status
     */
    private val _uiState = MutableStateFlow(PINSetupUiState())
    val uiState: StateFlow<PINSetupUiState> = _uiState.asStateFlow()

    /**
     * Handles PIN entry change.
     *
     * **Behavior:**
     * - Auto-filters to digits only (removes non-numeric characters)
     * - Clears any existing error message
     * - Updates UI state
     *
     * @param newPin New PIN value from user input
     */
    fun onPINChanged(newPin: String) {
        _uiState.update { it.copy(pin = newPin.filter { c -> c.isDigit() }, errorMessage = null) }
    }

    /**
     * Handles PIN confirmation entry change.
     *
     * **Behavior:**
     * - Auto-filters to digits only
     * - Clears any existing error message
     * - Updates UI state
     *
     * @param newPin Confirmation PIN value from user input
     */
    fun onConfirmPINChanged(newPin: String) {
        _uiState.update { it.copy(confirmPin = newPin.filter { c -> c.isDigit() }, errorMessage = null) }
    }

    /**
     * Initiates PIN setup when user clicks submit button.
     *
     * **Validation Steps:**
     * 1. Check PIN length ≥ MIN_PIN_LENGTH
     * 2. Verify PIN matches confirmation
     * 3. Call authManager.setupInitialPIN(pin)
     *
     * **Error Handling:**
     * - Length error: "PIN must be at least X digits"
     * - Mismatch error: "PINs do not match"
     * - Setup error: Shows auth manager error message
     *
     * **On Success:**
     * - Sets isComplete = true
     * - UI navigates to main app
     *
     * **On Failure:**
     * - Shows error message
     * - User can retry
     */
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
