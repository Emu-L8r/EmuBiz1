package com.emul8r.bizap.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.emul8r.bizap.data.local.BusinessProfileDao
import com.emul8r.bizap.data.local.entities.BusinessProfileEntity
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class BusinessProfileRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val businessProfileDao: BusinessProfileDao
) : BusinessProfileRepository {

    private object Keys {
        val ACTIVE_BUSINESS_ID = longPreferencesKey("active_business_id")
    }

    /**
     * REACTIVE IDENTITY ENGINE: 
     * Watches DataStore for active ID -> Watches Room for profile changes.
     * This ensures UI updates when business profile is edited.
     * FIXED: Now properly handles missing profiles by fetching first available
     */
    override val activeProfile: Flow<BusinessProfile> = dataStore.data
        .map { it[Keys.ACTIVE_BUSINESS_ID] ?: 1L } // Default to ID 1
        .distinctUntilChanged()
        .flatMapLatest { id ->
            businessProfileDao.getAllProfiles()
                .map { profiles ->
                    // Try to find the requested ID, fallback to first profile, then default
                    profiles.firstOrNull { it.id == id }?.toDomain()
                        ?: profiles.firstOrNull()?.toDomain()
                        ?: BusinessProfile(id = 0, businessName = "Default Business")
                }
                .catch { e ->
                    Timber.e(e, "Error loading business profile $id")
                    emit(BusinessProfile(id = 0, businessName = "Error Loading Profile"))
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

    override suspend fun createProfile(profile: BusinessProfile): Result<Long> = runCatching {
        val id = businessProfileDao.insertProfile(profile.toEntity())
        setActiveBusinessId(id) // Auto-switch to new business
        id
    }

    override suspend fun updateProfile(profile: BusinessProfile): Result<Unit> = runCatching {
        businessProfileDao.insertProfile(profile.toEntity())
        Unit
    }

    override suspend fun deleteProfile(id: Long): Result<Unit> = runCatching {
        // Logic to prevent deleting the last business could be added here
        val entity = businessProfileDao.getProfileById(id)
        entity?.let { businessProfileDao.deleteProfile(it) }
        Unit
    }

    override suspend fun updateLogoPath(path: String): Result<Unit> = runCatching {
        val currentId = getActiveBusinessId()
        val entity = businessProfileDao.getProfileById(currentId)
        entity?.let {
            businessProfileDao.insertProfile(it.copy(logoBase64 = path))
        }
        Unit
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
