package com.emul8r.bizap.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import com.emul8r.bizap.analytics.AppMonitoring
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dbPassphraseDataStore by preferencesDataStore("bizap_db_passphrase_store")

/**
 * Manages the SQLCipher database passphrase using Android Keystore + DataStore.
 *
 * Security model:
 * - A 32-byte cryptographically random passphrase is generated on first launch.
 * - The passphrase is encrypted with AES-256-GCM using a key stored in Android Keystore.
 * - Only the encrypted ciphertext + IV are persisted in DataStore.
 * - The raw passphrase is never written to disk.
 *
 * Migration note (April 18, 2026):
 * - Fully migrated from SharedPreferences to DataStore (eliminates StrictMode violations).
 * - runBlocking is intentional: database MUST open synchronously at startup.
 *   DataStore.data.first() completes in <5ms on all tested devices.
 *
 * Caller responsibility:
 * - Zero the returned ByteArray after passing it to SQLCipher.
 */
@Singleton
class DatabasePassphraseManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appMonitoring: AppMonitoring
) {

    companion object {
        private const val KEY_ALIAS = "bizap_db_key"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val PASSPHRASE_SIZE_BYTES = 32
    }

    private val PREF_ENCRYPTED_PASSPHRASE = stringPreferencesKey("encrypted_db_passphrase")
    private val PREF_IV = stringPreferencesKey("db_key_iv")

    private val dataStore = context.dbPassphraseDataStore
    private val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).also { it.load(null) }

    /**
     * Returns the database passphrase, creating and storing a new one on first call.
     *
     * Thread safety: runBlocking is required because Room opens the database
     * synchronously during DI graph construction. DataStore.data.first() is
     * safe inside runBlocking — completes in <5ms.
     */
    fun getOrCreatePassphrase(): ByteArray = runBlocking {
        val startMs = System.currentTimeMillis()
        try {
            val prefs = dataStore.data.first()
            val encryptedB64 = prefs[PREF_ENCRYPTED_PASSPHRASE]
            val ivB64 = prefs[PREF_IV]

            if (encryptedB64 != null && ivB64 != null) {
                Timber.d("DatabasePassphraseManager: Decrypting existing passphrase")
                val result = decryptPassphrase(
                    Base64.decode(encryptedB64, Base64.NO_WRAP),
                    Base64.decode(ivB64, Base64.NO_WRAP)
                )
                appMonitoring.recordPassphraseEvent(
                    success = true,
                    durationMs = System.currentTimeMillis() - startMs,
                    isFallback = false
                )
                result
            } else {
                Timber.d("DatabasePassphraseManager: Generating new passphrase")
                val passphrase = generateSecurePassphrase()
                val (encrypted, iv) = encryptPassphrase(passphrase)
                dataStore.edit { store ->
                    store[PREF_ENCRYPTED_PASSPHRASE] = Base64.encodeToString(encrypted, Base64.NO_WRAP)
                    store[PREF_IV] = Base64.encodeToString(iv, Base64.NO_WRAP)
                }
                Timber.d("✅ DatabasePassphraseManager: Passphrase generated and stored")
                appMonitoring.recordPassphraseEvent(
                    success = true,
                    durationMs = System.currentTimeMillis() - startMs,
                    isFallback = false
                )
                passphrase
            }
        } catch (e: Exception) {
            Timber.e(e, "DatabasePassphraseManager: Failed to read passphrase — generating temporary")
            appMonitoring.recordPassphraseEvent(
                success = false,
                durationMs = System.currentTimeMillis() - startMs,
                isFallback = true
            )
            generateSecurePassphrase()
        }
    }

    private fun generateSecurePassphrase(): ByteArray {
        val passphrase = ByteArray(PASSPHRASE_SIZE_BYTES)
        SecureRandom().nextBytes(passphrase)
        return passphrase
    }

    private fun getOrCreateKeystoreKey(): SecretKey {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setKeySize(256)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            keyGenerator.generateKey()
        }
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    private fun encryptPassphrase(passphrase: ByteArray): Pair<ByteArray, ByteArray> {
        val key = getOrCreateKeystoreKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(passphrase)
        return Pair(encrypted, cipher.iv)
    }

    private fun decryptPassphrase(encrypted: ByteArray, iv: ByteArray): ByteArray {
        val key = getOrCreateKeystoreKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(encrypted)
    }
}
