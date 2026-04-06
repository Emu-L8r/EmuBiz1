package com.emul8r.bizap.ui.gui2.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.usecase.CreateCustomerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for GUI2 Create Customer Screen
 *
 * **Responsibilities:**
 * - Manage form state (name, email, phone, address, etc.)
 * - Validate form inputs (business logic, NOT UI layer)
 * - Persist customer to database
 * - Handle errors with specific messages
 *
 * **Architecture:**
 * - UI layer (CreateCustomerScreenV2) renders only
 * - ALL form state in uiState StateFlow
 * - ALL validation logic here (not in onClick)
 * - NO callbacks - UI observes StateFlow reactively
 */
@HiltViewModel
class CreateCustomerViewModelV2 @Inject constructor(
    private val createCustomerUseCase: CreateCustomerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCustomerUiState())
    val uiState: StateFlow<CreateCustomerUiState> = _uiState.asStateFlow()

    /**
     * Update customer name field.
     */
    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    /**
     * Update business name field.
     */
    fun updateBusinessName(businessName: String) {
        _uiState.update { it.copy(businessName = businessName) }
    }

    /**
     * Update email field.
     */
    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    /**
     * Update phone field.
     */
    fun updatePhone(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    /**
     * Update address field.
     */
    fun updateAddress(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    /**
     * Update notes field.
     */
    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    /**
     * Create customer - Business logic entry point.
     *
     * **Validation:**
     * - Name: Required, not blank
     * - Email: Optional, but if provided must be valid format
     *
     * **Error Handling:**
     * - Specific field errors (nameError, emailError)
     * - General errors (generalError)
     */
    fun createCustomer() {
        val currentState = _uiState.value

        // Clear previous errors
        _uiState.update { it.copy(nameError = null, emailError = null, generalError = null) }

        // Validate name
        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            Timber.w("Customer creation validation failed: name is blank")
            return
        }

        // Validate email format (if provided)
        if (currentState.email.isNotBlank() &&
            (!currentState.email.contains("@") || !currentState.email.contains("."))) {
            _uiState.update {
                it.copy(emailError = "Please enter a valid email address (e.g., user@example.com)")
            }
            Timber.w("Customer creation validation failed: invalid email format")
            return
        }

        _uiState.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            try {
                Timber.d("Creating customer via UseCase: name=${currentState.name}, email=${currentState.email}")

                // Delegate to UseCase
                val result = createCustomerUseCase.execute(
                    name = currentState.name,
                    businessName = currentState.businessName,
                    email = currentState.email,
                    phone = currentState.phone,
                    address = currentState.address,
                    notes = currentState.notes
                )

                result.onSuccess { id ->
                    Timber.i("✅ Customer created successfully with ID: $id")
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            customerCreated = true,
                            // Reset form after success
                            name = "",
                            businessName = "",
                            email = "",
                            phone = "",
                            address = "",
                            notes = ""
                        )
                    }
                }.onFailure { error ->
                    Timber.e(error, "❌ Failed to create customer")
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            generalError = error.message ?: "Failed to create customer. Please try again."
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Unexpected error creating customer")
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        generalError = e.message ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }
}
