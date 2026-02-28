package com.example.databaser.data

import kotlinx.coroutines.flow.Flow

class OfflineBusinessInfoRepository(private val businessInfoDao: BusinessInfoDao) : BusinessInfoRepository {
    override fun getBusinessInfoStream(): Flow<BusinessInfo?> = businessInfoDao.getBusinessInfo()

    override suspend fun insertBusinessInfo(businessInfo: BusinessInfo) = businessInfoDao.insert(businessInfo)

    override suspend fun updateBusinessInfo(businessInfo: BusinessInfo) = businessInfoDao.insert(businessInfo)
}
