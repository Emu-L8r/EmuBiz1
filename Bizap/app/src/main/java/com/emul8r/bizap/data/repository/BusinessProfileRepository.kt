package com.emul8r.bizap.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.emul8r.bizap.domain.model.BusinessProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BusinessProfileRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val NAME = stringPreferencesKey("biz_name")
        val ABN = stringPreferencesKey("biz_abn")
        val EMAIL = stringPreferencesKey("biz_email")
        val PHONE = stringPreferencesKey("biz_phone")
        val ADDRESS = stringPreferencesKey("biz_address")
        val WEBSITE = stringPreferencesKey("biz_website")
        val BSB = stringPreferencesKey("biz_bsb")
        val ACCOUNT_NUMBER = stringPreferencesKey("biz_account_number")
        val ACCOUNT_NAME = stringPreferencesKey("biz_account_name")
        val BANK_NAME = stringPreferencesKey("biz_bank_name")
        val LOGO_BASE64 = stringPreferencesKey("logo_base64")
    }

    val profile: Flow<BusinessProfile> = dataStore.data.map { prefs ->
        BusinessProfile(
            businessName = prefs[Keys.NAME] ?: "",
            abn = prefs[Keys.ABN] ?: "",
            email = prefs[Keys.EMAIL] ?: "",
            phone = prefs[Keys.PHONE] ?: "",
            address = prefs[Keys.ADDRESS] ?: "",
            website = prefs[Keys.WEBSITE] ?: "",
            bsbNumber = prefs[Keys.BSB],
            accountNumber = prefs[Keys.ACCOUNT_NUMBER],
            accountName = prefs[Keys.ACCOUNT_NAME],
            bankName = prefs[Keys.BANK_NAME],
            logoBase64 = prefs[Keys.LOGO_BASE64]
        )
    }

    suspend fun updateProfile(profile: BusinessProfile) {
        dataStore.edit { prefs ->
            prefs[Keys.NAME] = profile.businessName
            prefs[Keys.ABN] = profile.abn
            prefs[Keys.EMAIL] = profile.email
            prefs[Keys.PHONE] = profile.phone
            prefs[Keys.ADDRESS] = profile.address
            prefs[Keys.WEBSITE] = profile.website
            profile.bsbNumber?.let { prefs[Keys.BSB] = it }
            profile.accountNumber?.let { prefs[Keys.ACCOUNT_NUMBER] = it }
            profile.accountName?.let { prefs[Keys.ACCOUNT_NAME] = it }
            profile.bankName?.let { prefs[Keys.BANK_NAME] = it }
            profile.logoBase64?.let { prefs[Keys.LOGO_BASE64] = it }
        }
    }

    suspend fun updateLogoPath(path: String) {
        dataStore.edit { preferences ->
            preferences[Keys.LOGO_BASE64] = path // Now expects Base64 string
        }
    }
}
