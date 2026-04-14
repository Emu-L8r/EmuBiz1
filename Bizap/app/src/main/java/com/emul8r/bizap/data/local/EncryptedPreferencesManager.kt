package com.emul8r.bizap.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure preferences manager using EncryptedSharedPreferences.
 *
 * Encrypts sensitive data at rest using Android's security library (Tink).
 * Hardware-backed encryption on supported devices.
 *
 * **Use Cases:**
 * - Store API keys
 * - Store session tokens
 * - Store user auth state
 * - Store PIN hashes
 * - Store any PII
 *
 * **Security:**
 * - AES-256-GCM encryption (Tink default)
 * - Hardware Keystore backed (when available)
 * - Automatic encryption/decryption
 *
 * @param context Application context
 */
@Singleton
class EncryptedPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "bizap_encrypted_prefs"
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Store a string value securely (encrypted).
     *
     * @param key The preference key
     * @param value The value to store (will be encrypted)
     */
    fun putString(key: String, value: String) {
        try {
            encryptedPreferences.edit().putString(key, value).apply()
            Timber.d("✅ Encrypted value stored: $key")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to store encrypted value: $key")
        }
    }

    /**
     * Retrieve a string value (automatically decrypted).
     *
     * @param key The preference key
     * @param defaultValue Value to return if key not found
     * @return The decrypted value or defaultValue
     */
    fun getString(key: String, defaultValue: String = ""): String {
        return try {
            encryptedPreferences.getString(key, defaultValue) ?: defaultValue
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to retrieve encrypted value: $key")
            defaultValue
        }
    }

    /**
     * Store a boolean value securely.
     *
     * @param key The preference key
     * @param value The boolean value to store
     */
    fun putBoolean(key: String, value: Boolean) {
        try {
            encryptedPreferences.edit().putBoolean(key, value).apply()
            Timber.d("✅ Encrypted boolean stored: $key")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to store encrypted boolean: $key")
        }
    }

    /**
     * Retrieve a boolean value (automatically decrypted).
     *
     * @param key The preference key
     * @param defaultValue Value to return if key not found
     * @return The decrypted value or defaultValue
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return try {
            encryptedPreferences.getBoolean(key, defaultValue)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to retrieve encrypted boolean: $key")
            defaultValue
        }
    }

    /**
     * Store a long value securely.
     *
     * @param key The preference key
     * @param value The long value to store
     */
    fun putLong(key: String, value: Long) {
        try {
            encryptedPreferences.edit().putLong(key, value).apply()
            Timber.d("✅ Encrypted long stored: $key")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to store encrypted long: $key")
        }
    }

    /**
     * Retrieve a long value (automatically decrypted).
     *
     * @param key The preference key
     * @param defaultValue Value to return if key not found
     * @return The decrypted value or defaultValue
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return try {
            encryptedPreferences.getLong(key, defaultValue)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to retrieve encrypted long: $key")
            defaultValue
        }
    }

    /**
     * Remove a key from encrypted preferences.
     *
     * @param key The preference key to remove
     */
    fun remove(key: String) {
        try {
            encryptedPreferences.edit().remove(key).apply()
            Timber.d("✅ Encrypted value removed: $key")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to remove encrypted value: $key")
        }
    }

    /**
     * Clear all encrypted preferences.
     * ⚠️ Use with caution - this clears all stored data!
     */
    fun clear() {
        try {
            encryptedPreferences.edit().clear().apply()
            Timber.w("⚠️ All encrypted preferences cleared")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to clear encrypted preferences")
        }
    }

    /**
     * Check if a key exists in encrypted preferences.
     *
     * @param key The preference key to check
     * @return true if key exists, false otherwise
     */
    fun contains(key: String): Boolean {
        return try {
            encryptedPreferences.contains(key)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to check if key exists: $key")
            false
        }
    }
}

/**
 * Convenience constants for common encrypted preference keys.
 */
object EncryptedPreferenceKeys {
    const val API_KEY_EXCHANGE_RATE = "api_key_exchange_rate"
    const val API_KEY_STRIPE = "api_key_stripe"
    const val API_KEY_PAYPAL = "api_key_paypal"
    const val SESSION_TOKEN = "session_token"
    const val USER_EMAIL = "user_email"
    const val PIN_HASH = "pin_hash"
    const val BIOMETRIC_ENABLED = "biometric_enabled"
    const val LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
}

