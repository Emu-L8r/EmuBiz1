package com.emul8r.bizap.ui.dunning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.invoice.model.DunningNotice
import com.emul8r.bizap.domain.invoice.usecase.GenerateDunningNoticesUseCase
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DunningNoticesViewModel @Inject constructor(
    private val generateDunningNoticesUseCase: GenerateDunningNoticesUseCase,
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DunningUiState>(DunningUiState.Loading)
    val uiState: StateFlow<DunningUiState> = _uiState.asStateFlow()

    init {
        refreshDunningNotices()
    }

    fun refreshDunningNotices() {
        viewModelScope.launch {
            try {
                _uiState.value = DunningUiState.Loading
                val businessId = businessProfileRepository.getActiveBusinessId()
                val notices = generateDunningNoticesUseCase.execute(businessId)
                _uiState.value = DunningUiState.Success(notices)
            } catch (e: Exception) {
                Timber.e(e, "Failed to load dunning notices")
                _uiState.value = DunningUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}

sealed class DunningUiState {
    object Loading : DunningUiState()
    data class Success(val notices: List<DunningNotice>) : DunningUiState()
    data class Error(val message: String) : DunningUiState()
}
