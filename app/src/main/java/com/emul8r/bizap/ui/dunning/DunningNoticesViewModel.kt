package com.emul8r.bizap.ui.dunning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.invoice.model.DunningNotice
import com.emul8r.bizap.domain.invoice.usecase.GenerateDunningNoticesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DunningNoticesViewModel @Inject constructor(
    private val generateDunningNoticesUseCase: GenerateDunningNoticesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DunningUiState>(DunningUiState.Loading)
    val uiState: StateFlow<DunningUiState> = _uiState.asStateFlow()

    private val businessId = 1L

    init {
        loadDunningNotices()
    }

    fun loadDunningNotices() {
        viewModelScope.launch {
            try {
                _uiState.value = DunningUiState.Loading
                val notices = generateDunningNoticesUseCase.execute(businessId)
                _uiState.value = DunningUiState.Success(notices)
                Timber.d("DunningNoticesViewModel: Dunning notices loaded successfully")
            } catch (e: Exception) {
                Timber.e(e, "DunningNoticesViewModel: Failed to load dunning notices")
                _uiState.value = DunningUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun refreshDunningNotices() {
        loadDunningNotices()
    }
}

sealed class DunningUiState {
    object Loading : DunningUiState()
    data class Success(val notices: List<DunningNotice>) : DunningUiState()
    data class Error(val message: String) : DunningUiState()
}