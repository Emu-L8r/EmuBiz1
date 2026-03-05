package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.BusinessProfile
import kotlinx.coroutines.flow.Flow

interface BusinessProfileRepository {
    val activeProfile: Flow<BusinessProfile>
    val allProfiles: Flow<List<BusinessProfile>>
    
    suspend fun getActiveBusinessId(): Long
    suspend fun setActiveBusinessId(id: Long)
    
    suspend fun createProfile(profile: BusinessProfile): Result<Long>
    suspend fun updateProfile(profile: BusinessProfile): Result<Unit>
    suspend fun updateLogoPath(path: String): Result<Unit>
    suspend fun deleteProfile(id: Long): Result<Unit>
}
