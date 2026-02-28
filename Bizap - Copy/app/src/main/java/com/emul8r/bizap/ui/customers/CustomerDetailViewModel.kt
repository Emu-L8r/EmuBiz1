package com.emul8r.bizap.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.local.entities.CustomerEntity
import com.emul8r.bizap.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CustomerDetailUiState {
    object Loading : CustomerDetailUiState
    data class Success(val customer: CustomerEntity) : CustomerDetailUiState
    data class Error(val message: String) : CustomerDetailUiState
}

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CustomerDetailUiState>(CustomerDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadCustomer(id: Long) {
        viewModelScope.launch {
            _uiState.value = CustomerDetailUiState.Loading
            val customer = repository.getCustomerById(id)
            _uiState.value = if (customer != null) {
                CustomerDetailUiState.Success(customer)
            } else {
                CustomerDetailUiState.Error("Customer not found")
            }
        }
    }
}
