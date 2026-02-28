package com.example.databaser.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PassphraseStatus {
    NOT_SET,
    SET,
    INVALID,
    NEEDS_CREATION
}

class PassphraseManager(private val context: Context) {

    private val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "passphrase_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val passphraseStatus: MutableStateFlow<PassphraseStatus> = MutableStateFlow(PassphraseStatus.NOT_SET)

    init {
        val passphrase = sharedPreferences.getString("passphrase", null)
        if (passphrase != null) {
            passphraseStatus.value = PassphraseStatus.SET
        } else {
            if (databaseExists()) {
                passphraseStatus.value = PassphraseStatus.NOT_SET
            } else {
                passphraseStatus.value = PassphraseStatus.NEEDS_CREATION
            }
        }
    }

    private fun databaseExists(): Boolean {
        return context.getDatabasePath(DATABASE_NAME).exists()
    }

    fun getPassphrase(): String? {
        return sharedPreferences.getString("passphrase", null)
    }

    fun setPassphrase(passphrase: String) {
        sharedPreferences.edit().putString("passphrase", passphrase).apply()
        passphraseStatus.value = PassphraseStatus.SET
    }

    fun checkPassphrase(passphrase: String): Boolean {
        return getPassphrase() == passphrase
    }

    fun clearPassphrase() {
        sharedPreferences.edit().remove("passphrase").apply()
        passphraseStatus.value = PassphraseStatus.NOT_SET
    }
}