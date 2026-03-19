package com.emul8r.bizap.ui.settings

import com.emul8r.bizap.domain.model.BusinessProfile

sealed class BusinessProfileUiState {
    object Loading : BusinessProfileUiState()
    data class Error(val message: String) : BusinessProfileUiState()
    data class Success(val businessProfile: BusinessProfile) : BusinessProfileUiState()
}
