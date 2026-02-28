package com.emul8r.bizap.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.BuildConfig
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

    /**
     * PRAGMATIC SEEDING FIX: Manual trigger for business profile test data.
     */
    fun seedTestBusinessProfile() {
        if (!BuildConfig.DEBUG) return

        viewModelScope.launch {
            Log.d("BusinessProfileViewModel", "🐛 DEBUG BUTTON CLICKED: Seeding test business profile...")
            try {
                val testProfile = BusinessProfile(
                    businessName = "Emu Consulting Pty Ltd",
                    abn = "12 345 678 901",
                    email = "contact@emuconsulting.com.au",
                    phone = "(02) 8999 1234",
                    address = "Level 10, 123 Business Avenue, Sydney NSW 2000",
                    website = "www.emuconsulting.com.au",
                    bankName = "Commonwealth Bank",
                    accountName = "Emu Consulting Trust",
                    bsbNumber = "062-000",
                    accountNumber = "1234 5678"
                )
                
                repository.updateProfile(testProfile)
                Log.d("BusinessProfileViewModel", "✅ TEST BUSINESS PROFILE LOADED")
            } catch (e: Exception) {
                Log.e("BusinessProfileViewModel", "❌ Seeding failed: ${e.message}", e)
            }
        }
    }
}
