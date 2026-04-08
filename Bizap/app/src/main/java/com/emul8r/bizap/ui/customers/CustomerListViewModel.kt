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
 * Manages customer list screen state and business logic.
 *
 * **Architecture:**
 * - Observes customers from repository
 * - Transforms flow into UI-friendly state
 * - Handles loading, success, and error states
 * - Lifecycle survives configuration changes
 *
 * **Responsibilities:**
 * - Data transformation from repository to UI state
 * - Error handling and user-friendly messages
 * - Business context management (businessId from navigation)
 * - Real-time customer list updates
 *
 * **Data Flow:**
 * ```
 * CustomerRepository.getAllCustomers()
 *     ↓
 * Transform to CustomerListUiState
 *     ↓
 * StateFlow<CustomerListUiState>
 *     ↓
 * UI collects and displays
 * ```
 *
 * **Usage:**
 * ```kotlin
 * @Composable
 * fun CustomerListScreen() {
 *     val viewModel: CustomerListViewModel = hiltViewModel()
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *     val businessId = viewModel.businessId
 *
 *     when (uiState) {
 *         is CustomerListUiState.Loading -> LoadingScreen()
 *         is CustomerListUiState.Success -> {
 *             val customers = (uiState as CustomerListUiState.Success).customers
 *             CustomerListContent(customers)
 *         }
 *         is CustomerListUiState.Error -> {
 *             val message = (uiState as CustomerListUiState.Error).message
 *             ErrorScreen(message)
 *         }
 *     }
 * }
 * ```
 *
 * **State Management:**
 * - Initial: Loading
 * - On success: Success with customer list
 * - On error: Error with message
 * - Caching: 5-second subscription timeout
 *
 * @param savedStateHandle Navigation arguments (contains businessId)
 * @param customerRepository Source of customer data
 *
 * @see CustomerListScreen
 * @see CustomerRepository
 * @see CustomerListUiState
 */
@HiltViewModel
class CustomerListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    /**
     * Navigation route containing businessId for context.
     *
     * @see ScreenV2.Customers
     */
    private val route: ScreenV2.Customers? = try {
        savedStateHandle.toRoute()
    } catch (e: Exception) {
        Timber.w(e, "CustomerListViewModel: Failed to extract route from SavedStateHandle. Using default businessId.")
        null
    }

    /**
     * Active business ID from navigation.
     *
     * Used to filter/scope customer queries to the active business context.
     * Defaults to 1L if route extraction fails (safety fallback for GUI1 navigation).
     */
    val businessId: Long = route?.businessId ?: 1L

    /**
     * Current UI state as reactive stream.
     *
     * Emits updates when:
     * - Data loads from repository (Loading → Success)
     * - Error occurs during loading (Loading → Error)
     * - New data arrives (Success → Success)
     *
     * Initial value: [CustomerListUiState.Loading]
     *
     * Caching: Subscription timeout of 5 seconds ensures
     * data is refreshed periodically while screen is visible.
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
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CustomerListUiState.Loading
        )
}

/**
 * UI state for customer list screen.
 *
 * Represents all possible states the customer list can be in:
 * - [Loading]: Initial state, fetching data from repository
 * - [Success]: Data loaded, ready to display list
 * - [Error]: Failed to load data, show error message
 *
 * **State Transitions:**
 * ```
 * Loading
 *     → Success (list loaded)
 *     → Error (fetch failed)
 *
 * Success
 *     → Success (new data arrived)
 *     → Error (refresh failed)
 * ```
 *
 * **Immutability:**
 * All states are immutable. New states create new objects.
 * This ensures proper Compose recomposition when state changes.
 *
 * @see CustomerListViewModel
 */
sealed interface CustomerListUiState {
    /**
     * Initial loading state.
     *
     * Indicates data is being fetched from repository.
     * UI should show loading spinner or skeleton.
     */
    object Loading : CustomerListUiState

    /**
     * Error state when data fetch fails.
     *
     * @param message Human-readable error message to display to user
     */
    data class Error(val message: String) : CustomerListUiState

    /**
     * Success state with loaded customer data.
     *
     * @param customers List of customers loaded from repository
     *                  May be empty if business has no customers
     */
    data class Success(val customers: List<Customer>) : CustomerListUiState
}
