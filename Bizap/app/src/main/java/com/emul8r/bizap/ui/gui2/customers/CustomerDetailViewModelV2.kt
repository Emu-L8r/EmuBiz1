package com.emul8r.bizap.ui.gui2.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for GUI2 Customer Detail Screen
 * Loads and manages single customer details.
 */
@HiltViewModel
class CustomerDetailViewModelV2 @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val route: ScreenV2.CustomerDetail = savedStateHandle.toRoute()
    val businessId: Long = route.businessId
    private val customerId: Long = route.customerId

    val uiState: StateFlow<CustomerDetailUiStateV2> = customerRepository
        .getCustomerById(customerId)
        .map { customer ->
            Timber.d("CustomerDetailViewModelV2: Loaded customer $customerId")
            if (customer != null) {
                CustomerDetailUiStateV2.Success(customer) as CustomerDetailUiStateV2
            } else {
                CustomerDetailUiStateV2.Error("Customer not found")
            }
        }
        .catch { exception ->
            Timber.e(exception, "CustomerDetailViewModelV2: Failed to load customer")
            emit(CustomerDetailUiStateV2.Error(exception.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CustomerDetailUiStateV2.Loading
        )

    fun deleteCustomer() {
        viewModelScope.launch {
            try {
                Timber.d("CustomerDetailViewModelV2: Deleting customer $customerId")
                customerRepository.deleteCustomer(customerId)
                Timber.d("CustomerDetailViewModelV2: Customer deleted successfully")
            } catch (e: Exception) {
                Timber.e(e, "CustomerDetailViewModelV2: Failed to delete customer")
            }
        }
    }
}

