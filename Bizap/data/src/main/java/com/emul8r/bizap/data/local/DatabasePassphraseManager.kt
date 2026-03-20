package com.emul8r.bizap.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the SQLCipher database passphrase using Android Keystore for security.
 *
 * The passphrase is encrypted with an AES-256-GCM key stored in the Android Keystore.
 * Only the encrypted ciphertext (not the raw passphrase) is persisted in SharedPreferences.
 * On each app start the passphrase is decrypted on-the-fly by the Keystore key.
 */
@Singleton
class DatabasePassphraseManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val KEY_ALIAS = "bizap_db_key"
        private const val PREFS_NAME = "bizap_db_secure_prefs"
        private const val PREF_ENCRYPTED_PASSPHRASE = "encrypted_db_passphrase"
        private const val PREF_IV = "db_key_iv"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val PASSPHRASE_BYTE_LENGTH = 32
    }

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Returns the database passphrase, generating and securely storing a new one on first call.
     * The caller is responsible for zeroing the returned array when it is no longer needed.
     */
    fun getOrCreatePassphrase(): ByteArray {
        val encryptedB64 = prefs.getString(PREF_ENCRYPTED_PASSPHRASE, null)
        val ivB64 = prefs.getString(PREF_IV, null)

        return if (encryptedB64 != null && ivB64 != null) {
            decryptPassphrase(
                Base64.decode(encryptedB64, Base64.NO_WRAP),
                Base64.decode(ivB64, Base64.NO_WRAP)
            )
        } else {
            val passphrase = generateSecurePassphrase()
            val (encrypted, iv) = encryptPassphrase(passphrase)
            prefs.edit()
                .putString(PREF_ENCRYPTED_PASSPHRASE, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .putString(PREF_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
                .apply()
            passphrase
        }
    }

    private fun generateSecurePassphrase(): ByteArray =
        ByteArray(PASSPHRASE_BYTE_LENGTH).also { SecureRandom().nextBytes(it) }

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
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
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
        return Pair(cipher.doFinal(passphrase), cipher.iv)
    }

    private fun decryptPassphrase(encrypted: ByteArray, iv: ByteArray): ByteArray {
        val key = getOrCreateKeystoreKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(encrypted)
    }
}
