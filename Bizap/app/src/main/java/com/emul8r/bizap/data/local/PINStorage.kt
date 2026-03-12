package com.emul8r.bizap.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles secure PIN storage using SHA-256 + random salt.
 *
 * The PIN is NEVER stored in plaintext. Instead:
 * 1. A 16-byte random salt is generated and stored.
 * 2. The PIN is hashed with the salt using SHA-256.
 * 3. Only the salt + hash are persisted in SharedPreferences.
 *
 * Verification re-computes the hash and compares it to the stored value.
 */
@Singleton
class PINStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Returns true if a PIN has been saved for this installation. */
    fun isPINSet(): Boolean = prefs.contains(KEY_PIN_HASH)

    /**
     * Hashes and stores [pin].
     * Calling this again overwrites any previously set PIN.
     */
    fun setupPIN(pin: String): Result<Unit> = runCatching {
        val salt = generateSalt()
        val hash = hashPIN(pin, salt)
        prefs.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hash)
            .apply()
    }

    /**
     * Returns [Result.success(true)] if [pin] matches the stored hash,
     * [Result.success(false)] if it does not, or [Result.failure] on error.
     */
    fun verifyPIN(pin: String): Result<Boolean> = runCatching {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return Result.success(false)
        val salt = prefs.getString(KEY_PIN_SALT, null) ?: return Result.success(false)
        hashPIN(pin, salt) == storedHash
    }

    /**
     * Removes the stored PIN and salt. Called during the "Forgot PIN" flow
     * before wiping all app data.
     */
    fun clearPIN(): Result<Unit> = runCatching {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .apply()
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun generateSalt(): String {
        val bytes = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun hashPIN(pin: String, saltBase64: String): String {
        val saltBytes = Base64.decode(saltBase64, Base64.NO_WRAP)
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        digest.update(saltBytes)
        val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    // ── Constants ─────────────────────────────────────────────────────────────

    companion object {
        private const val PREFS_NAME = "bizap_auth_prefs"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val SALT_BYTES = 16
        private const val HASH_ALGORITHM = "SHA-256"
    }
}
