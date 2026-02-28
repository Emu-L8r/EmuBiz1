package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.BusinessProfile
import kotlinx.coroutines.flow.Flow

interface BusinessProfileRepository {
    val profile: Flow<BusinessProfile>
    suspend fun updateProfile(profile: BusinessProfile)
    suspend fun updateLogoPath(path: String)
}
