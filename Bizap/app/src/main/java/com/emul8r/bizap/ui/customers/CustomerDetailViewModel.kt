package com.emul8r.bizap.ui.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface CustomerDetailUiState {
    object Loading : CustomerDetailUiState
    data class Success(val customer: Customer) : CustomerDetailUiState
    data class Error(val message: String) : CustomerDetailUiState
}

sealed interface CustomerDetailEvent {
    object CustomerDeleted : CustomerDetailEvent
    object CustomerUpdated : CustomerDetailEvent
}

/**
 * Consolidated ViewModel for Customer Detail Screen
 *
 * Works for both GUI1 and GUI2 modes. Handles:
 * - Loading individual customer details
 * - Updating customer information
 * - Deleting customers
 * - Error handling and events
 */
@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CustomerRepository
) : ViewModel() {

    // Extract customer ID from SavedStateHandle (works for both GUI1 and GUI2)
    val customerId: Long = try {
        val route: Screen.CustomerDetail = savedStateHandle.toRoute()
        route.customerId
    } catch (e: Exception) {
        Timber.w(e, "CustomerDetailViewModel: Failed to extract customerId from route")
        0L  // Invalid ID - will trigger error state
    }

    private val _uiState = MutableStateFlow<CustomerDetailUiState>(CustomerDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<CustomerDetailEvent>()
    val event = _event.asSharedFlow()

    init {
        if (customerId > 0) {
            loadCustomer(customerId)
        } else {
            _uiState.value = CustomerDetailUiState.Error("Invalid customer ID")
        }
    }

    fun loadCustomer(id: Long) {
        viewModelScope.launch {
            _uiState.value = CustomerDetailUiState.Loading
            repository.getCustomerById(id)
                .catch { e ->
                    Timber.e(e, "Error loading customer")
                    _uiState.value = CustomerDetailUiState.Error("Failed to load customer: ${e.message}")
                }
                .collect { customer ->
                    _uiState.value = if (customer != null) {
                        CustomerDetailUiState.Success(customer)
                    } else {
                        CustomerDetailUiState.Error("Customer not found")
                    }
                }
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomer(id)
                .onSuccess { _event.emit(CustomerDetailEvent.CustomerDeleted) }
                .onFailure { e ->
                    Timber.e(e, "Failed to delete customer")
                    _uiState.value = CustomerDetailUiState.Error("Failed to delete customer: ${e.message}")
                }
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
                .onSuccess { _event.emit(CustomerDetailEvent.CustomerUpdated) }
                .onFailure { e ->
                    Timber.e(e, "Failed to update customer")
                    _uiState.value = CustomerDetailUiState.Error("Failed to update customer: ${e.message}")
                }
        }
    }
}
