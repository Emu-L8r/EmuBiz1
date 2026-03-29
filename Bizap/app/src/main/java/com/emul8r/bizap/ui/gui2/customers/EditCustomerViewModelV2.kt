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
 * ViewModel for GUI2 Edit Customer Screen
 */
@HiltViewModel
class EditCustomerViewModelV2 @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val route: ScreenV2.EditCustomer = savedStateHandle.toRoute()
    val businessId: Long = route.businessId
    private val customerId: Long = route.customerId

    // Track loading state for UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val uiState: StateFlow<EditCustomerUiStateV2> = customerRepository
        .getCustomerById(customerId)
        .map { customer ->
            Timber.d("EditCustomerViewModelV2: Loaded customer $customerId")
            if (customer != null) {
                EditCustomerUiStateV2.Success(customer) as EditCustomerUiStateV2
            } else {
                EditCustomerUiStateV2.Error("Customer not found")
            }
        }
        .catch { exception ->
            Timber.e(exception, "EditCustomerViewModelV2: Failed to load customer")
            emit(EditCustomerUiStateV2.Error(exception.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EditCustomerUiStateV2.Loading
        )

    fun updateCustomer(
        customer: Customer,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Timber.d("EditCustomerViewModelV2: Updating customer $customerId")
                customerRepository.updateCustomer(customer)
                Timber.d("✅ EditCustomerViewModelV2: Customer updated successfully")
                onSuccess()
            } catch (e: Exception) {
                Timber.e(e, "❌ EditCustomerViewModelV2: Failed to update customer")
                onError(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }
}


