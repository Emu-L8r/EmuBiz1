package com.emu.emubizwax.ui.customers

import android.util.Patterns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emu.emubizwax.domain.model.Customer
import com.emu.emubizwax.domain.usecase.GetCustomerByIdUseCase
import com.emu.emubizwax.domain.usecase.SaveCustomerUseCase
import com.emu.emubizwax.ui.navigation.EditCustomer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerDetailsViewModel @Inject constructor(
    private val getCustomerByIdUseCase: GetCustomerByIdUseCase,
    private val saveCustomerUseCase: SaveCustomerUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(Customer())
    val uiState = _uiState.asStateFlow()

    val isFormValid = _uiState.map {
        it.name.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(it.email).matches()
    }

    init {
        savedStateHandle.get<EditCustomer>("customerId")?.customerId?.let {
            viewModelScope.launch {
                getCustomerByIdUseCase(it)?.collect { customer ->
                    if (customer != null) {
                        _uiState.update { customer }
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onBusinessNameChange(businessName: String) {
        _uiState.update { it.copy(businessName = businessName) }
    }

    fun onBusinessNumberChange(businessNumber: String) {
        _uiState.update { it.copy(businessNumber = businessNumber) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun saveCustomer() {
        viewModelScope.launch {
            saveCustomerUseCase(uiState.value)
        }
    }
}
