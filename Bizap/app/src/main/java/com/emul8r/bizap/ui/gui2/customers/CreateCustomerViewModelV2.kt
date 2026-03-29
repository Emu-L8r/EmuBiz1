package com.emul8r.bizap.ui.gui2.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for GUI2 Create Customer Screen
 *
 * Features:
 * - Customer creation with validation
 * - Loading state tracking
 * - Error handling with user feedback
 */
@HiltViewModel
class CreateCustomerViewModelV2 @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    // Track loading state for UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun createCustomer(
        customer: Customer,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ) {
        // Email is now optional - validate name only
        if (customer.name.isBlank()) {
            onError("Customer name is required")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                Timber.d("CreateCustomerViewModelV2: Creating customer ${customer.name}")
                val result = customerRepository.insert(customer)

                result.onSuccess { id ->
                    Timber.d("✅ CreateCustomerViewModelV2: Customer created successfully with ID $id")
                    onSuccess()
                }.onFailure { error ->
                    Timber.e(error, "❌ CreateCustomerViewModelV2: Failed to create customer")
                    onError(error.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ CreateCustomerViewModelV2: Exception creating customer")
                onError(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
