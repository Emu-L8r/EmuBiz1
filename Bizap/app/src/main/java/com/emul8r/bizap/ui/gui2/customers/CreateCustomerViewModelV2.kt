package com.emul8r.bizap.ui.gui2.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for GUI2 Create Customer Screen
 */
@HiltViewModel
class CreateCustomerViewModelV2 @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    fun createCustomer(
        customer: Customer,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Timber.d("CreateCustomerViewModelV2: Creating customer ${customer.name}")
                customerRepository.saveCustomer(customer)
                Timber.d("CreateCustomerViewModelV2: Customer created successfully")
                onSuccess()
            } catch (e: Exception) {
                Timber.e(e, "CreateCustomerViewModelV2: Failed to create customer")
                onError(e.message ?: "Unknown error")
            }
        }
    }
}

