package com.emul8r.bizap.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.model.BusinessProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusinessProfileViewModel @Inject constructor(
    private val repository: BusinessProfileRepository
) : ViewModel() {

    val profileState: StateFlow<BusinessProfile> = repository.profile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BusinessProfile()
        )

    fun updateProfile(newProfile: BusinessProfile) {
        viewModelScope.launch {
            repository.updateProfile(newProfile)
        }
    }
}
