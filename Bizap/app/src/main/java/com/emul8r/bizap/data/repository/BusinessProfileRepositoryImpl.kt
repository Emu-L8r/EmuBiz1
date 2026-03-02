package com.emul8r.bizap.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.emul8r.bizap.data.local.dao.BusinessProfileDao
import com.emul8r.bizap.data.local.entities.BusinessProfileEntity
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class BusinessProfileRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val businessProfileDao: BusinessProfileDao
) : BusinessProfileRepository {

    private object Keys {
        val ACTIVE_BUSINESS_ID = longPreferencesKey("active_business_id")
    }

    /**
     * REACTIVE IDENTITY ENGINE: 
     * Watches DataStore for active ID -> Fetches full profile from Room.
     */
    override val activeProfile: Flow<BusinessProfile> = dataStore.data
        .map { it[Keys.ACTIVE_BUSINESS_ID] ?: 1L } // Default to seeded ID 1
        .flatMapLatest { id ->
            // Note: Since Dao.getProfileById is suspend, we convert to flow here
            flow {
                val entity = businessProfileDao.getProfileById(id)
                emit(entity?.toDomain() ?: BusinessProfile(id = 1, businessName = "Unknown"))
            }
        }

    override val allProfiles: Flow<List<BusinessProfile>> = businessProfileDao.getAllProfiles()
        .map { list -> list.map { it.toDomain() } }

    override suspend fun getActiveBusinessId(): Long {
        return dataStore.data.map { it[Keys.ACTIVE_BUSINESS_ID] ?: 1L }.first()
    }

    override suspend fun setActiveBusinessId(id: Long) {
        dataStore.edit { it[Keys.ACTIVE_BUSINESS_ID] = id }
    }

    override suspend fun createProfile(profile: BusinessProfile): Long {
        val id = businessProfileDao.insertProfile(profile.toEntity())
        setActiveBusinessId(id) // Auto-switch to new business
        return id
    }

    override suspend fun updateProfile(profile: BusinessProfile) {
        businessProfileDao.insertProfile(profile.toEntity())
    }

    override suspend fun deleteProfile(id: Long) {
        // Logic to prevent deleting the last business could be added here
        val entity = businessProfileDao.getProfileById(id)
        entity?.let { businessProfileDao.deleteProfile(it) }
    }

    override suspend fun updateLogoPath(path: String) {
        val currentId = getActiveBusinessId()
        val entity = businessProfileDao.getProfileById(currentId)
        entity?.let {
            businessProfileDao.insertProfile(it.copy(logoBase64 = path))
        }
    }

    // --- Mappers ---

    private fun BusinessProfileEntity.toDomain() = BusinessProfile(
        id = id,
        businessName = businessName,
        abn = abn,
        email = email,
        phone = phone,
        address = address,
        website = website,
        bsbNumber = bsbNumber,
        accountNumber = accountNumber,
        accountName = accountName,
        bankName = bankName,
        logoBase64 = logoBase64,
        signatureUri = signatureUri,
        isTaxRegistered = isTaxRegistered,
        defaultTaxRate = defaultTaxRate
    )

    private fun BusinessProfile.toEntity() = BusinessProfileEntity(
        id = id,
        businessName = businessName,
        abn = abn,
        email = email,
        phone = phone,
        address = address,
        website = website,
        bsbNumber = bsbNumber,
        accountNumber = accountNumber,
        accountName = accountName,
        bankName = bankName,
        logoBase64 = logoBase64,
        signatureUri = signatureUri,
        isTaxRegistered = isTaxRegistered,
        defaultTaxRate = defaultTaxRate
    )
}

