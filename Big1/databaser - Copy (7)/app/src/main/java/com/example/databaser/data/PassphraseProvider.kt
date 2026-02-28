package com.example.databaser.data

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.databaser.security.KeystoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom

class PassphraseProvider(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "db_passphrase_prefs"
        private const val PREF_KEY = "db_passphrase_ciphertext_v1"
        private const val KEY_ALIAS = "db_passphrase_key_rsa"
        private const val PASSPHRASE_SIZE = 32 // 256-bit
    }

    private val masterKey by lazy {
        MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun ensurePassphraseExists(): Unit = withContext(Dispatchers.IO) {
        // Ensure keystore key pair exists
        KeystoreHelper.ensureKeyPair(KEY_ALIAS)

        if (!prefs.contains(PREF_KEY)) {
            val raw = ByteArray(PASSPHRASE_SIZE)
            SecureRandom().nextBytes(raw)
            val wrapped = KeystoreHelper.wrapWithPublicKey(KEY_ALIAS, raw)
            val encoded = Base64.encodeToString(wrapped, Base64.NO_WRAP)
            prefs.edit().putString(PREF_KEY, encoded).apply()
            // zero raw
            raw.fill(0)
        }
    }

    suspend fun getRawPassphrase(): ByteArray = withContext(Dispatchers.IO) {
        val encoded = prefs.getString(PREF_KEY, null)
            ?: throw IllegalStateException("Wrapped passphrase not found. Call ensurePassphraseExists() first.")
        val wrapped = Base64.decode(encoded, Base64.NO_WRAP)
        val raw = KeystoreHelper.unwrapWithPrivateKey(KEY_ALIAS, wrapped)
        return@withContext raw
    }

    // Wipe sensitive bytes from given buffer
    fun wipe(bytes: ByteArray?) {
        bytes?.fill(0)
    }
}
