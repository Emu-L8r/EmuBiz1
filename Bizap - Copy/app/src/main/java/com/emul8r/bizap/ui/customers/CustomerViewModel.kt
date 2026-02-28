package com.emul8r.bizap.ui.customers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.local.entities.CustomerEntity
import com.emul8r.bizap.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    // Single source of truth for the UI
    val uiState: StateFlow<List<CustomerEntity>> = repository.getAllCustomers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var customerName by mutableStateOf("")
    var businessName by mutableStateOf("")
    var businessNumber by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")
    var address by mutableStateOf("")

    fun saveNewCustomer(onSuccess: () -> Unit = {}) {
        if (customerName.isBlank()) return 
        viewModelScope.launch {
            repository.saveCustomer(
                CustomerEntity(
                    name = customerName,
                    businessName = businessName,
                    businessNumber = businessNumber,
                    phone = phone,
                    email = email,
                    address = address
                )
            )
            clearFields()
            onSuccess()
        }
    }

    private fun clearFields() {
        customerName = ""; businessName = ""; businessNumber = ""
        phone = ""; email = ""; address = ""
    }

    fun addTestCustomer() {
        viewModelScope.launch {
            repository.saveCustomer(
                CustomerEntity(
                    name = "Test Client ${System.currentTimeMillis() % 1000}",
                    businessName = "Emu Enterprises",
                    businessNumber = "123 456",
                    email = "test@example.com",
                    phone = "0400 000 000",
                    address = "123 Perth St, WA"
                )
            )
        }
    }
}
