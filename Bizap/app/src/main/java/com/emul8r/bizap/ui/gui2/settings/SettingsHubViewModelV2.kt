package com.emul8r.bizap.ui.gui2.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsHubViewModelV2 @Inject constructor(
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiStateV2> = businessProfileRepository
        .activeProfile
        .map { profile ->
            Timber.d("SettingsHubViewModelV2: Loaded business profile")
            SettingsUiStateV2.Success(profile) as SettingsUiStateV2
        }
        .catch { exception ->
            Timber.e(exception, "SettingsHubViewModelV2: Failed to load profile")
            emit(SettingsUiStateV2.Error(exception.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiStateV2.Loading
        )
}

