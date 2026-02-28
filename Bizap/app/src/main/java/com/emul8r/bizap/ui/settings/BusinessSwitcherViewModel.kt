package com.emul8r.bizap.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BusinessSwitcherUiState(
    val profiles: List<BusinessProfile> = emptyList(),
    val activeProfileId: Long = 1L
)

@HiltViewModel
class BusinessSwitcherViewModel @Inject constructor(
    private val repository: BusinessProfileRepository
) : ViewModel() {

    val uiState: StateFlow<BusinessSwitcherUiState> = repository.allProfiles
        .combine(repository.activeProfile) { all, active ->
            BusinessSwitcherUiState(all, active.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BusinessSwitcherUiState()
        )

    fun switchBusiness(id: Long) {
        viewModelScope.launch {
            repository.setActiveBusinessId(id)
        }
    }

    fun createNewBusiness(name: String) {
        viewModelScope.launch {
            val newProfile = BusinessProfile(businessName = name)
            repository.createProfile(newProfile)
        }
    }
}

private fun <T1, T2, R> kotlinx.coroutines.flow.Flow<T1>.combine(
    flow: kotlinx.coroutines.flow.Flow<T2>,
    transform: suspend (T1, T2) -> R
): kotlinx.coroutines.flow.Flow<R> = kotlinx.coroutines.flow.combine(this, flow, transform)
