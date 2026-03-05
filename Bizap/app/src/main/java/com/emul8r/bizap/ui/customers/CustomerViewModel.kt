package com.emul8r.bizap.ui.customers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.BuildConfig
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.validation.ValidationRules
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class CustomerFormState(
    val validationError: String? = null,
    val error: String? = null
)

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    // Single source of truth for the UI
    val uiState: StateFlow<List<Customer>> = repository.getAllCustomers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _formState = MutableStateFlow(CustomerFormState())
    val formState: StateFlow<CustomerFormState> = _formState.asStateFlow()

    var customerName by mutableStateOf("")
    var businessName by mutableStateOf("")
    var businessNumber by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")
    var address by mutableStateOf("")

    fun saveNewCustomer(onSuccess: () -> Unit = {}) {
        if (customerName.isBlank()) return
        val customer = Customer(
            name = customerName,
            businessName = businessName.ifBlank { null },
            businessNumber = businessNumber.ifBlank { null },
            phone = phone.ifBlank { null },
            email = email.ifBlank { null },
            address = address.ifBlank { null }
        )
        val validation = ValidationRules.validateCustomer(customer)
        if (validation.isFailure()) {
            _formState.update { it.copy(validationError = validation.getErrorOrNull()) }
            return
        }
        _formState.update { it.copy(validationError = null) }
        viewModelScope.launch {
            repository.insert(customer)
                .onSuccess {
                    clearFields()
                    onSuccess()
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to save customer")
                    _formState.update { it.copy(error = e.message) }
                }
        }
    }

    fun clearFormError() {
        _formState.update { it.copy(validationError = null, error = null) }
    }

    private fun clearFields() {
        customerName = ""; businessName = ""; businessNumber = ""
        phone = ""; email = ""; address = ""
        _formState.update { CustomerFormState() }
    }

    /**
     * PRAGMATIC SEEDING FIX: Manual trigger for test data.
     */
    @androidx.annotation.VisibleForTesting
    internal fun seedTestCustomers() {
        if (!BuildConfig.DEBUG) {
            Timber.w("seedTestCustomers called in non-debug build, ignoring")
            return
        }
        viewModelScope.launch {
            Timber.d("📥 Seeding database with test customers...")
            val testCustomers = listOf(
                Customer(
                    name = "UNREALCUSTOMER1",
                    email = "test@unrealcustomer1.com.au",
                    phone = "(02) 9999 1111",
                    address = "123 Test Street, Sydney NSW 2000"
                ),
                Customer(
                    name = "UNREALCUSTOMER2",
                    email = "accounts@unreal2.com",
                    phone = "(03) 8888 2222",
                    address = "456 Fake Avenue, Melbourne VIC 3000"
                ),
                Customer(
                    name = "UNREALCUSTOMER3",
                    email = "hello@unreal3.io",
                    phone = "(08) 7777 3333",
                    address = "789 Mock Lane, Perth WA 6000"
                )
            )
            testCustomers.forEach { customer ->
                repository.insert(customer)
                    .onFailure { e -> Timber.e(e, "❌ Seeding failed for ${customer.name}") }
            }
            Timber.d("✅ All test customers seeded!")
        }
    }

    fun addTestCustomer() {
        viewModelScope.launch {
            repository.insert(
                Customer(
                    name = "Test Client ${System.currentTimeMillis() % 1000}",
                    businessName = "Emu Enterprises",
                    businessNumber = "123 456",
                    email = "test@example.com",
                    phone = "0400 000 000",
                    address = "123 Perth St, WA"
                )
            ).onFailure { e -> Timber.e(e, "Failed to add test customer") }
        }
    }
}
