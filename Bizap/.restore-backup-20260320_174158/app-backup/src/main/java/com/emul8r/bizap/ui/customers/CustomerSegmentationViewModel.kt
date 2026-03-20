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

sealed class CustomerSegmentationUiState {
    object Loading : CustomerSegmentationUiState()
    data class Success(val summary: CustomerAnalyticsSummary) : CustomerSegmentationUiState()
    data class Error(val message: String) : CustomerSegmentationUiState()
}

@HiltViewModel
class CustomerSegmentationViewModel @Inject constructor(
    private val getCustomerAnalyticsUseCase: GetCustomerAnalyticsUseCase,
    private val segmentCustomersUseCase: SegmentCustomersUseCase,
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CustomerSegmentationUiState>(CustomerSegmentationUiState.Loading)
    val uiState: StateFlow<CustomerSegmentationUiState> = _uiState.asStateFlow()

    init {
        loadSegments()
    }

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
