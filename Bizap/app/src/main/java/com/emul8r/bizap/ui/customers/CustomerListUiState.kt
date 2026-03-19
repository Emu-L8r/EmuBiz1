package com.emul8r.bizap.ui.customers

import com.emul8r.bizap.domain.model.Customer

sealed class CustomerListUiState {
    object Loading : CustomerListUiState()
    data class Error(val message: String) : CustomerListUiState()
    data class Success(val customers: List<Customer>) : CustomerListUiState()
}
