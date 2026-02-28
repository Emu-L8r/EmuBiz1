package com.emu.emubizwax.data.repository

import com.emu.emubizwax.domain.model.BusinessInfo
import com.emu.emubizwax.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor() : UserRepository {
    override fun getUserProfile(): Flow<BusinessInfo> {
        // For now, return a hardcoded profile.
        // In a real app, this would come from DataStore or a remote API.
        return flowOf(BusinessInfo())
    }
}
