package com.emu.emubizwax.domain.repository

import com.emu.emubizwax.domain.model.BusinessInfo
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(): Flow<BusinessInfo>
}
