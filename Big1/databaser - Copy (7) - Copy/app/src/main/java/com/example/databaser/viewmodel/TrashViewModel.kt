package com.example.databaser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.databaser.data.Customer
import com.example.databaser.data.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(private val customerRepository: CustomerRepository) : ViewModel() {

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
}
