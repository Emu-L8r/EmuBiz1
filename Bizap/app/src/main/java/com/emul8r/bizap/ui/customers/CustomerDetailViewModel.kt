package com.emul8r.bizap.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
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

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CustomerDetailUiState>(CustomerDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<CustomerDetailEvent>()
    val event = _event.asSharedFlow()

    fun loadCustomer(id: Long) {
        viewModelScope.launch {
            _uiState.value = CustomerDetailUiState.Loading
            val customer = repository.getCustomerById(id).firstOrNull()
            _uiState.value = if (customer != null) {
                CustomerDetailUiState.Success(customer)
            } else {
                CustomerDetailUiState.Error("Customer not found")
            }
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteCustomer(id)
                _event.emit(CustomerDetailEvent.CustomerDeleted)
            } catch (e: Exception) {
                _uiState.value = CustomerDetailUiState.Error("Failed to delete customer: ${e.message}")
            }
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                repository.updateCustomer(customer)
                _event.emit(CustomerDetailEvent.CustomerUpdated)
            } catch (e: Exception) {
                _uiState.value = CustomerDetailUiState.Error("Failed to update customer: ${e.message}")
            }
        }
    }
}

