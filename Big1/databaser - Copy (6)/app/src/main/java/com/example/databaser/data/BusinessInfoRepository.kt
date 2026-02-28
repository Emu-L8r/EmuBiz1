package com.example.databaser.data

import kotlinx.coroutines.flow.Flow

interface BusinessInfoRepository {
    fun getBusinessInfoStream(): Flow<BusinessInfo?>
    suspend fun insertBusinessInfo(businessInfo: BusinessInfo)
    suspend fun updateBusinessInfo(businessInfo: BusinessInfo)
}
