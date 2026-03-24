package com.emul8r.bizap.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.customer.model.CustomerAnalyticsSummary
import com.emul8r.bizap.domain.customer.usecase.GetCustomerAnalyticsUseCase
import com.emul8r.bizap.domain.customer.usecase.SegmentCustomersUseCase
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for customer segmentation/analytics screen.
 *
 * Represents loading, success, and error states for customer segment analysis.
 *
 * @see CustomerSegmentationViewModel
 */
sealed class CustomerSegmentationUiState {
    /**
     * Loading state while computing customer segments.
     *
     * UI displays loading indicator or skeleton.
     */
    object Loading : CustomerSegmentationUiState()

    /**
     * Success state with computed analytics summary.
     *
     * @param summary Customer segmentation and analytics data
     */
    data class Success(val summary: CustomerAnalyticsSummary) : CustomerSegmentationUiState()

    /**
     * Error state when segmentation fails.
     *
     * @param message Error message to display to user
     */
    data class Error(val message: String) : CustomerSegmentationUiState()
}

/**
 * Manages customer segmentation and analytics computations.
 *
 * **Purpose:**
 * Segments customers into groups (e.g., by payment behavior, spend, frequency)
 * and computes analytics summaries for business insights.
 *
 * **Architecture:**
 * - Gets business context from BusinessProfileRepository
 * - Delegates segmentation to SegmentCustomersUseCase
 * - Computes analytics via GetCustomerAnalyticsUseCase
 * - Emits state for UI rendering
 *
 * **Segmentation Dimensions:**
 * - Payment behavior (on-time, late, overdue)
 * - Spend levels (high-value, medium, low)
 * - Frequency (frequent, occasional, dormant)
 * - Customer lifetime value (CLV)
 *
 * **Data Flow:**
 * ```
 * Get active business ID
 *     ↓
 * Segment customers (SegmentCustomersUseCase)
 *     ↓
 * Compute analytics (GetCustomerAnalyticsUseCase)
 *     ↓
 * Transform to UiState
 *     ↓
 * StateFlow<CustomerSegmentationUiState>
 *     ↓
 * UI displays analytics dashboard
 * ```
 *
 * **Usage:**
 * ```kotlin
 * @Composable
 * fun CustomerSegmentationScreen() {
 *     val viewModel: CustomerSegmentationViewModel = hiltViewModel()
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *
 *     when (uiState) {
 *         CustomerSegmentationUiState.Loading -> LoadingScreen()
 *         is CustomerSegmentationUiState.Success -> {
 *             val summary = (uiState as CustomerSegmentationUiState.Success).summary
 *             SegmentationDashboard(summary)
 *         }
 *         is CustomerSegmentationUiState.Error -> {
 *             val message = (uiState as CustomerSegmentationUiState.Error).message
 *             ErrorScreen(message) { viewModel.loadSegments() }
 *         }
 *     }
 * }
 * ```
 *
 * @param getCustomerAnalyticsUseCase Computes analytics summary
 * @param segmentCustomersUseCase Performs customer segmentation
 * @param businessProfileRepository Provides business context
 *
 * @see CustomerAnalyticsSummary
 * @see SegmentCustomersUseCase
 * @see GetCustomerAnalyticsUseCase
 */
@HiltViewModel
class CustomerSegmentationViewModel @Inject constructor(
    private val getCustomerAnalyticsUseCase: GetCustomerAnalyticsUseCase,
    private val segmentCustomersUseCase: SegmentCustomersUseCase,
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    /**
     * Current segmentation UI state.
     *
     * Emits:
     * - Loading: Initial state, computing segments
     * - Success: Analytics summary computed
     * - Error: Segmentation or analytics failed
     */
    private val _uiState = MutableStateFlow<CustomerSegmentationUiState>(CustomerSegmentationUiState.Loading)
    val uiState: StateFlow<CustomerSegmentationUiState> = _uiState.asStateFlow()

    /**
     * Initialization block.
     *
     * Automatically loads customer segments when ViewModel is created.
     */
    init {
        loadSegments()
    }

    /**
     * Loads and segments customers, then computes analytics.
     *
     * **Steps:**
     * 1. Set state to Loading
     * 2. Get active business ID
     * 3. Segment customers via SegmentCustomersUseCase
     * 4. Compute analytics via GetCustomerAnalyticsUseCase
     * 5. Emit Success state with summary
     *
     * **On Error:**
     * - Logs exception
     * - Emits Error state with message
     * - User can retry via loadSegments()
     */
    fun loadSegments() {
        viewModelScope.launch {
            try {
                _uiState.value = CustomerSegmentationUiState.Loading
                val businessId = businessProfileRepository.getActiveBusinessId()
                segmentCustomersUseCase.execute(businessId)
                val summary = getCustomerAnalyticsUseCase.execute(businessId)
                _uiState.value = CustomerSegmentationUiState.Success(summary)
                Timber.d("CustomerSegmentationViewModel: Segments loaded successfully")
            } catch (e: Exception) {
                Timber.e(e, "CustomerSegmentationViewModel: Failed to load segments")
                _uiState.value = CustomerSegmentationUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
