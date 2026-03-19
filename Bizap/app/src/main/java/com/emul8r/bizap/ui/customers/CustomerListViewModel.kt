package com.emul8r.bizap.ui.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Consolidated UI state for CustomerListScreen.
 * Shared between GUI1 and GUI2 implementations.
 * Replaces: CustomerListUiStateV2 (GUI2).
 */
sealed interface CustomerListUiState {
    object Loading : CustomerListUiState
    data class Error(val message: String) : CustomerListUiState
    data class Success(val customers: List<Customer>) : CustomerListUiState
}

/**
 * Consolidated ViewModel for CustomerListScreen.
 * Serves the GUI2 CustomerListScreenV2Content.
 * Replaces: CustomerListViewModelV2 (GUI2).
 */
@HiltViewModel
class CustomerListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val route: ScreenV2.Customers = savedStateHandle.toRoute()
    val businessId: Long = route.businessId

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
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CustomerListUiState.Loading
        )
}
