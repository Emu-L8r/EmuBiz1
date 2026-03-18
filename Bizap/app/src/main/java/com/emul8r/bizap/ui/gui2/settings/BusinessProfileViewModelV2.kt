package com.emul8r.bizap.ui.gui2.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BusinessProfileViewModelV2 @Inject constructor(
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    val uiState: StateFlow<BusinessProfileUiStateV2> = businessProfileRepository
        .activeProfile
        .map { profile ->
            Timber.d("BusinessProfileViewModelV2: Loaded profile")
            BusinessProfileUiStateV2.Success(profile) as BusinessProfileUiStateV2
        }
        .catch { exception ->
            Timber.e(exception, "BusinessProfileViewModelV2: Failed to load profile")
            emit(BusinessProfileUiStateV2.Error(exception.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BusinessProfileUiStateV2.Loading
        )

    fun updateBusinessProfile(profile: BusinessProfile) {
        viewModelScope.launch {
            try {
                Timber.d("BusinessProfileViewModelV2: Updating profile")
                businessProfileRepository.updateProfile(profile)
                Timber.d("BusinessProfileViewModelV2: Profile updated successfully")
            } catch (e: Exception) {
                Timber.e(e, "BusinessProfileViewModelV2: Failed to update profile")
            }
        }
    }
}


sealed interface BusinessProfileUiStateV2 {
    object Loading : BusinessProfileUiStateV2
    data class Error(val message: String) : BusinessProfileUiStateV2
    data class Success(val businessProfile: com.emul8r.bizap.domain.model.BusinessProfile) : BusinessProfileUiStateV2
}
