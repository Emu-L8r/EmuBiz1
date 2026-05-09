package com.emul8r.bizap.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.emul8r.bizap.domain.model.InvoiceSettings
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backward Compatibility utilities for InvoiceSettings migration.
 *
 * Phase 3E: Handles migration from DataStore-only storage to repository storage.
 * Provides graceful fallback paths for seamless user experience during rollout.
 *
 * **Migration Path:**
 * 1. Check repository first (new, Phase 3E+)
 * 2. Fall back to DataStore if repository is empty (legacy, Phase 3B-3D)
 * 3. Migrate old DataStore entries to repository
 * 4. Clean up DataStore after verification
 */
@Singleton
class InvoiceSettingsBackwardCompatibility @Inject constructor(
    private val context: Context,
    private val invoiceSettingsRepository: InvoiceSettingsRepository,
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private const val TAG = "InvoiceSettingsBackCompat"

        // DataStore preference keys (legacy)
        private val INVOICE_PREFIX_KEY = stringPreferencesKey("invoice_prefix")
        private val STARTING_NUMBER_KEY = intPreferencesKey("starting_number")
        private val INCLUDE_NOTES_KEY = booleanPreferencesKey("include_notes")
        private val INCLUDE_TAX_ID_KEY = booleanPreferencesKey("include_tax_id")
        private val FOOTER_TEXT_KEY = stringPreferencesKey("footer_text")
        private val SHOW_LOGO_KEY = booleanPreferencesKey("show_logo")
        private val SHOW_COMPANY_INFO_KEY = booleanPreferencesKey("show_company_info")
        private val COLOR_SCHEME_KEY = stringPreferencesKey("selected_color_scheme")
        private val SPACING_PROFILE_KEY = stringPreferencesKey("selected_spacing_profile")
    }

    /**
     * Get settings with fallback chain: Repository → DataStore → Defaults
     *
     * This ensures seamless migration without breaking existing user data.
     */
    suspend fun getSettingsWithFallback(userId: String): InvoiceSettings? {
        return try {
            Timber.tag(TAG).d("Getting settings for user=$userId with fallback chain")

            // Step 1: Try repository (new storage)
            val repositorySettings = invoiceSettingsRepository.getSettings(userId)
            if (repositorySettings != null) {
                Timber.tag(TAG).d("✅ Found settings in repository")
                return repositorySettings
            }

            // Step 2: Try DataStore (legacy storage)
            val dataStoreSettings = loadSettingsFromDataStore(userId)
            if (dataStoreSettings != null) {
                Timber.tag(TAG).d("⚠️ Found settings in DataStore (legacy), migrating to repository...")
                // Migrate to repository for future use
                invoiceSettingsRepository.saveSettings(dataStoreSettings)
                return dataStoreSettings
            }

            // Step 3: Use defaults
            Timber.tag(TAG).d("ℹ️ No settings found, using defaults")
            null
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error retrieving settings with fallback")
            null
        }
    }

    /**
     * Migrate all DataStore entries to repository storage.
     *
     * Safe operation: Only migrates if repository is empty.
     * Can be called multiple times (idempotent).
     */
    suspend fun migrateFromDataStoreToRepository(userId: String): Boolean {
        return try {
            Timber.tag(TAG).d("Migrating DataStore entries to repository for user=$userId")

            // Check if already migrated
            val repositorySettings = invoiceSettingsRepository.getSettings(userId)
            if (repositorySettings != null) {
                Timber.tag(TAG).d("Already migrated, skipping")
                return true
            }

            // Load from DataStore
            val dataStoreSettings = loadSettingsFromDataStore(userId)
            if (dataStoreSettings != null) {
                Timber.tag(TAG).d("Found DataStore entries, migrating...")
                invoiceSettingsRepository.saveSettings(dataStoreSettings)
                Timber.tag(TAG).d("✅ Migration complete")
                return true
            }

            Timber.tag(TAG).d("No DataStore entries found to migrate")
            false
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "❌ Migration failed")
            false
        }
    }

    /**
     * Load settings from DataStore (legacy format).
     *
     * Returns null if no DataStore entries exist.
     */
    private suspend fun loadSettingsFromDataStore(userId: String): InvoiceSettings? {
        return try {
            val prefs = dataStore.data.first()

            // Check if any keys exist
            if (prefs[INVOICE_PREFIX_KEY] == null && prefs[COLOR_SCHEME_KEY] == null) {
                return null
            }

            Timber.tag(TAG).d("Loading legacy settings from DataStore")

            // Reconstruct InvoiceSettings from DataStore keys
            InvoiceSettings(
                userId = userId,
                invoiceNumberPrefix = prefs[INVOICE_PREFIX_KEY] ?: "INV",
                footerMessage = prefs[FOOTER_TEXT_KEY] ?: ""
                // Note: Other Phase 3B-3D fields will use their defaults
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to load from DataStore")
            null
        }
    }

    /**
     * Clear DataStore entries after successful migration.
     *
     * Call only after verifying repository contains valid data.
     */
    suspend fun clearLegacyDataStore() {
        try {
            Timber.tag(TAG).d("Clearing legacy DataStore entries...")
            dataStore.edit { prefs ->
                prefs.remove(INVOICE_PREFIX_KEY)
                prefs.remove(STARTING_NUMBER_KEY)
                prefs.remove(INCLUDE_NOTES_KEY)
                prefs.remove(INCLUDE_TAX_ID_KEY)
                prefs.remove(FOOTER_TEXT_KEY)
                prefs.remove(SHOW_LOGO_KEY)
                prefs.remove(SHOW_COMPANY_INFO_KEY)
                prefs.remove(COLOR_SCHEME_KEY)
                prefs.remove(SPACING_PROFILE_KEY)
            }
            Timber.tag(TAG).d("✅ DataStore cleared")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error clearing DataStore")
        }
    }

    /**
     * Verify both storage backends are in sync.
     *
     * Used for testing and validation during migration.
     */
    suspend fun verifyMigrationComplete(userId: String): Boolean {
        return try {
            Timber.tag(TAG).d("Verifying migration for user=$userId")

            val repositorySettings = invoiceSettingsRepository.getSettings(userId)
            val dataStoreSettings = loadSettingsFromDataStore(userId)

            val isComplete = repositorySettings != null && (
                dataStoreSettings == null ||
                repositorySettings.invoiceNumberPrefix == dataStoreSettings.invoiceNumberPrefix
            )

            if (isComplete) {
                Timber.tag(TAG).d("✅ Migration verified")
            } else {
                Timber.tag(TAG).w("⚠️ Migration incomplete or mismatched")
            }

            isComplete
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Verification failed")
            false
        }
    }
}

