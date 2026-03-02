package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.BusinessProfile
import kotlinx.coroutines.flow.Flow

interface BusinessProfileRepository {
    val activeProfile: Flow<BusinessProfile>
    val allProfiles: Flow<List<BusinessProfile>>
    
    suspend fun getActiveBusinessId(): Long
    suspend fun setActiveBusinessId(id: Long)
    
    suspend fun createProfile(profile: BusinessProfile): Long
    suspend fun updateProfile(profile: BusinessProfile)
    suspend fun updateLogoPath(path: String) // Restored for UI compatibility
    suspend fun deleteProfile(id: Long)
}

