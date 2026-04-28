package com.emul8r.bizap.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.BuildConfig
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.model.BusinessProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Consolidated ViewModel for Business Profile Screen
 *
 * Works for both GUI1 and GUI2 modes. Handles:
 * - Loading and displaying business profile
 * - Updating profile information
 * - Test data seeding for debugging
 */
@HiltViewModel
class BusinessProfileViewModel @Inject constructor(
    @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,  // Used for consistency with routing
    private val repository: BusinessProfileRepository
) : ViewModel() {

    /**
     * Single source of truth for Business Profile state
     *
     * Emits the current business profile. Starts with an empty profile
     * and updates when the repository emits changes.
     */
    val profileState: StateFlow<BusinessProfile> = repository.activeProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BusinessProfile()
        )

    fun updateProfile(newProfile: BusinessProfile) {
        viewModelScope.launch {
            repository.updateProfile(newProfile)
                .onSuccess {
                    Timber.d("BusinessProfileViewModel: Successfully updated profile")
                }
                .onFailure { e ->
                    Timber.e(e, "BusinessProfileViewModel: Failed to update business profile")
                }
        }
    }

    /**
     * PRAGMATIC SEEDING FIX: Manual trigger for business profile test data.
     * Only available in DEBUG builds.
     */
    fun seedTestBusinessProfile() {
        if (!BuildConfig.DEBUG) return

        viewModelScope.launch {
            Timber.d("🐛 DEBUG BUTTON CLICKED: Seeding test business profile...")
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
                .onSuccess { Timber.d("✅ TEST BUSINESS PROFILE LOADED") }
                .onFailure { e -> Timber.e(e, "❌ Seeding failed: ${e.message}") }
        }
    }
}
