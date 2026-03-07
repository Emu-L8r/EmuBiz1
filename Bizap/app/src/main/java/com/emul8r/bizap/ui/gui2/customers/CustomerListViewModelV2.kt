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
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for GUI2 Customer List Screen
 * Observes customers for the given business.
 */
@HiltViewModel
class CustomerListViewModelV2 @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val route: ScreenV2.Dashboard = savedStateHandle.toRoute()
    val businessId: Long = route.businessId

    val uiState: StateFlow<CustomerListUiStateV2> = customerRepository
        .getAllCustomers()
        .map { customers ->
            Timber.d("CustomerListViewModelV2: Loaded ${customers.size} customers")
            CustomerListUiStateV2.Success(customers) as CustomerListUiStateV2
        }
        .catch { exception ->
            Timber.e(exception, "CustomerListViewModelV2: Failed to load customers")
            emit(CustomerListUiStateV2.Error(exception.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CustomerListUiStateV2.Loading
        )
}

