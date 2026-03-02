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

@HiltViewModel
class BusinessProfileViewModel @Inject constructor(
    private val repository: BusinessProfileRepository
) : ViewModel() {

    val profileState: StateFlow<BusinessProfile> = repository.activeProfile
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

    fun seedTestBusinessProfile() {
        viewModelScope.launch {
            val testProfile = BusinessProfile(
                businessName = "Emu Consulting Pty Ltd",
                abn = "12 345 678 901",
                email = "contact@emuconsulting.com.au",
                phone = "(02) 8999 1234",
                address = "Level 10, 123 Business Avenue, Sydney NSW 2000",
                website = "www.emuconsulting.com.au",
                bsbNumber = "062-000",
                accountNumber = "12345678",
                accountName = "Emu Consulting Operating Acct",
                bankName = "Commonwealth Bank"
            )
            repository.updateProfile(testProfile)
        }
    }
}
