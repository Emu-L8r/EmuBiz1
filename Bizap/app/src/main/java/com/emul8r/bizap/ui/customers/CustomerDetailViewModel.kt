package com.emul8r.bizap.ui.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
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
 * Consolidated ViewModel for CustomerDetailScreen.
 * Serves both GUI1 (loadCustomer via parameter) and GUI2 (SavedStateHandle route).
 * Replaces: CustomerDetailViewModelV2 (GUI2).
 */
@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CustomerRepository
) : ViewModel() {

    // V2 navigation: extract customerId from SavedStateHandle route.
    // V1 navigation: returns null; loadCustomer(id) is called explicitly from the composable.
    private val routeCustomerId: Long? = runCatching {
        savedStateHandle.toRoute<ScreenV2.CustomerDetail>().customerId
    }.getOrNull()

    private val _customerId = MutableStateFlow(routeCustomerId)

    private val _uiState = MutableStateFlow<CustomerDetailUiState>(CustomerDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<CustomerDetailEvent>()
    val event = _event.asSharedFlow()

    init {
        // When customerId is available (either from route or loadCustomer), start loading.
        viewModelScope.launch {
            _customerId.filterNotNull().collect { id ->
                loadCustomerInternal(id)
            }
        }
    }

    private suspend fun loadCustomerInternal(id: Long) {
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

    /** Called by GUI1 screen which passes customerId explicitly as a composable parameter. */
    fun loadCustomer(id: Long) {
        _customerId.value = id
    }

    /**
     * Deletes the customer.
     * @param id Explicit customer ID (used by GUI1). If null, uses the ID from navigation route (GUI2).
     */
    fun deleteCustomer(id: Long? = null) {
        val customerId = id ?: _customerId.value ?: return
        viewModelScope.launch {
            repository.deleteCustomer(customerId)
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
