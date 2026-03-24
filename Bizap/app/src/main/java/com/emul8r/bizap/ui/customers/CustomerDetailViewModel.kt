package com.emul8r.bizap.ui.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for customer detail screen.
 *
 * Represents all possible states while viewing/editing customer details:
 * - [Loading]: Initial state, fetching customer from repository
 * - [Success]: Customer loaded successfully
 * - [Error]: Failed to load customer
 *
 * @see CustomerDetailViewModel
 */
sealed interface CustomerDetailUiState {
    /**
     * Loading state while fetching customer data.
     *
     * UI should display loading spinner or skeleton screen.
     */
    object Loading : CustomerDetailUiState

    /**
     * Success state with loaded customer.
     *
     * @param customer Customer data loaded from repository
     */
    data class Success(val customer: Customer) : CustomerDetailUiState

    /**
     * Error state when customer load fails.
     *
     * @param message Error message to display to user
     */
    data class Error(val message: String) : CustomerDetailUiState
}

/**
 * Navigation events emitted by CustomerDetailViewModel.
 *
 * One-time events to trigger UI/navigation actions.
 *
 * @see CustomerDetailViewModel.event
 */
sealed interface CustomerDetailEvent {
    /**
     * Emitted after successful customer deletion.
     *
     * UI should navigate back to customer list.
     */
    object CustomerDeleted : CustomerDetailEvent

    /**
     * Emitted after successful customer update.
     *
     * UI can refresh display (though data flows automatically).
     */
    object CustomerUpdated : CustomerDetailEvent
}

/**
 * Manages customer detail screen state and operations.
 *
 * **Architecture:**
 * - Loads individual customer by ID from navigation
 * - Manages customer detail, edit, and delete operations
 * - Emits state for UI to display
 * - Broadcasts events for navigation (delete, update)
 * - Works for both GUI1 and GUI2 implementations
 *
 * **Responsibilities:**
 * - Load customer details from repository
 * - Handle customer updates
 * - Handle customer deletion
 * - Manage error states
 * - Emit navigation events
 *
 * **Data Flow:**
 * ```
 * Navigation (customerId)
 *     ↓
 * Load customer from repository
 *     ↓
 * Transform to UiState
 *     ↓
 * StateFlow<CustomerDetailUiState>
 *     ↓
 * UI displays customer or error
 * ```
 *
 * **Events:**
 * - [CustomerDetailEvent.CustomerDeleted]: Navigate back after delete
 * - [CustomerDetailEvent.CustomerUpdated]: Refresh UI after update
 *
 * **Usage:**
 * ```kotlin
 * @Composable
 * fun CustomerDetailScreen() {
 *     val viewModel: CustomerDetailViewModel = hiltViewModel()
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *
 *     LaunchedEffect(Unit) {
 *         viewModel.event.collect { event ->
 *             when (event) {
 *                 CustomerDetailEvent.CustomerDeleted -> navController.popBackStack()
 *                 CustomerDetailEvent.CustomerUpdated -> {} // Refresh happens automatically
 *             }
 *         }
 *     }
 *
 *     when (uiState) {
 *         CustomerDetailUiState.Loading -> LoadingScreen()
 *         is CustomerDetailUiState.Success -> {
 *             val customer = (uiState as CustomerDetailUiState.Success).customer
 *             CustomerDetailContent(customer) { viewModel.updateCustomer(it) }
 *         }
 *         is CustomerDetailUiState.Error -> {
 *             val message = (uiState as CustomerDetailUiState.Error).message
 *             ErrorScreen(message)
 *         }
 *     }
 * }
 * ```
 *
 * @param savedStateHandle Navigation arguments (contains customerId)
 * @param repository Customer data access and operations
 *
 * @see CustomerDetailUiState
 * @see CustomerDetailEvent
 */
@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CustomerRepository
) : ViewModel() {

    /**
     * Customer ID from navigation parameter.
     *
     * Extracted from SavedStateHandle route.
     * If invalid (0 or missing), loadCustomer() emits error state.
     *
     * @see Screen.CustomerDetail
     */
    val customerId: Long = try {
        val route: Screen.CustomerDetail = savedStateHandle.toRoute()
        route.customerId
    } catch (e: Exception) {
        Timber.w(e, "CustomerDetailViewModel: Failed to extract customerId from route")
        0L  // Invalid ID - will trigger error state
    }

    /**
     * Current UI state as reactive state flow.
     *
     * Emits updates when:
     * - Data loads (Loading → Success)
     * - Error occurs (Any → Error)
     * - Customer updates (Success → Success)
     */
    private val _uiState = MutableStateFlow<CustomerDetailUiState>(CustomerDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    /**
     * One-time navigation events.
     *
     * Emitted when:
     * - Customer deleted (navigate back)
     * - Customer updated (refresh available)
     *
     * @see CustomerDetailEvent
     */
    private val _event = MutableSharedFlow<CustomerDetailEvent>()
    val event = _event.asSharedFlow()

    init {
        if (customerId > 0) {
            loadCustomer(customerId)
        } else {
            _uiState.value = CustomerDetailUiState.Error("Invalid customer ID")
        }
    }

    fun loadCustomer(id: Long) {
        viewModelScope.launch {
            _uiState.value = CustomerDetailUiState.Loading
            repository.getCustomerById(id)
                .catch { e ->
                    Timber.e(e, "Error loading customer")
                    _uiState.value = CustomerDetailUiState.Error("Failed to load customer: ${e.message}")
                }
                .collect { customer ->
                    _uiState.value = if (customer != null) {
                        CustomerDetailUiState.Success(customer)
                    } else {
                        CustomerDetailUiState.Error("Customer not found")
                    }
                }
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomer(id)
                .onSuccess { _event.emit(CustomerDetailEvent.CustomerDeleted) }
                .onFailure { e ->
                    Timber.e(e, "Failed to delete customer")
                    _uiState.value = CustomerDetailUiState.Error("Failed to delete customer: ${e.message}")
                }
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
                .onSuccess { _event.emit(CustomerDetailEvent.CustomerUpdated) }
                .onFailure { e ->
                    Timber.e(e, "Failed to update customer")
                    _uiState.value = CustomerDetailUiState.Error("Failed to update customer: ${e.message}")
                }
        }
    }
}
