package com.emul8r.bizap.ui.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Consolidated ViewModel for Customer List Screen (MAIN - replacing V1 + V2)
 *
 * ✅ Single ViewModel (not V1 + V2)
 * ✅ Using SavedStateHandle.toRoute() for both GUIs
 * ✅ Single StateFlow<CustomerListUiState>
 * ✅ No references to V2 classes
 *
 * Works for both GUI1 and GUI2 modes. Handles:
 * - Loading customers from repository
 * - Error handling and display
 * - Business context via SavedStateHandle
 */
sealed interface CustomerListUiState {
    object Loading : CustomerListUiState
    data class Error(val message: String) : CustomerListUiState
    data class Success(val customers: List<Customer>) : CustomerListUiState
}

@HiltViewModel
class CustomerListViewModelNew @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    /**
     * Single source of truth for Customer List UI state
     *
     * Emits:
     * - Loading: Initial state while data is being fetched
     * - Success: Data loaded successfully with list of customers
     * - Error: An error occurred while loading
     */
    val uiState: StateFlow<CustomerListUiState> = customerRepository
        .getAllCustomers()
        .map { customers ->
            Timber.d("CustomerListViewModel: Loaded ${customers.size} customers")
            CustomerListUiState.Success(customers) as CustomerListUiState
        }
        .catch { exception ->
            Timber.e(exception, "CustomerListViewModel: Failed to load customers")
            emit(CustomerListUiState.Error(exception.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,  // Eagerly to ensure state is always available
            initialValue = CustomerListUiState.Loading
        )
}




