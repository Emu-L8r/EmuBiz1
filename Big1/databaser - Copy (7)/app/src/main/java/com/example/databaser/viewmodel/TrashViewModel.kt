package com.example.databaser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.databaser.MyApplication
import com.example.databaser.data.Customer
import com.example.databaser.data.CustomerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrashViewModel(private val customerRepository: CustomerRepository) : ViewModel() {

    val deletedCustomers: StateFlow<List<Customer>> = customerRepository.getDeletedCustomersStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun restoreCustomer(customer: Customer) {
        viewModelScope.launch {
            customerRepository.restoreCustomer(customer)
        }
    }

    fun permanentlyDeleteCustomer(customer: Customer) {
        viewModelScope.launch {
            customerRepository.permanentlyDeleteCustomer(customer)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as MyApplication
                return TrashViewModel(
                    application.container.customerRepository
                ) as T
            }
        }
    }
}
