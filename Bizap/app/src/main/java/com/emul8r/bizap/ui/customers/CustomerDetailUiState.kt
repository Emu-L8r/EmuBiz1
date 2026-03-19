package com.emul8r.bizap.ui.customers

import com.emul8r.bizap.domain.model.Customer

sealed class CustomerDetailUiState {
    object Loading : CustomerDetailUiState()
    object NotFound : CustomerDetailUiState()
    data class Error(val message: String) : CustomerDetailUiState()
    data class Success(val customer: Customer) : CustomerDetailUiState()
}
