package com.emul8r.bizap.security

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

private const val DATASTORE_NAME = "brute_force_protection"
private val FAILED_PIN_ATTEMPTS = intPreferencesKey("failed_pin_attempts")
private val LAST_PIN_ATTEMPT_TIME = longPreferencesKey("last_pin_attempt_time")

private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

/**
 * Brute Force Protection for PIN Authentication
 *
 * Locks access after 5 failed PIN attempts for 30 seconds.
 * This prevents brute force attacks on the 4-digit PIN (10,000 combinations).
 */
class BruteForceProtection(private val context: Context) {

    /**
     * Record a failed PIN attempt
     * Resets counter if 30 seconds have passed since last attempt
     */
    suspend fun recordFailedAttempt() {
        try {
            val prefs = context.dataStore.data.first()
            val attempts = prefs[FAILED_PIN_ATTEMPTS] ?: 0
            val lastAttemptTime = prefs[LAST_PIN_ATTEMPT_TIME] ?: 0L

            // Reset if 30 seconds have passed
            val timeSinceLastAttempt = System.currentTimeMillis() - lastAttemptTime
            val currentAttempts = if (timeSinceLastAttempt > 30000) 1 else attempts + 1

            context.dataStore.edit { preferences ->
                preferences[FAILED_PIN_ATTEMPTS] = currentAttempts
                preferences[LAST_PIN_ATTEMPT_TIME] = System.currentTimeMillis()
            }

            Timber.d("BruteForceProtection: Failed attempt recorded. Total: $currentAttempts/5")
        } catch (e: Exception) {
            Timber.e(e, "BruteForceProtection: Failed to record attempt")
        }
    }

    /**
     * Check if the system is currently locked
     * Returns true if 5+ failed attempts within last 30 seconds
     */
    suspend fun isLocked(): Boolean {
        return try {
            val prefs = context.dataStore.data.first()
            val attempts = prefs[FAILED_PIN_ATTEMPTS] ?: 0
            val lastAttemptTime = prefs[LAST_PIN_ATTEMPT_TIME] ?: 0L

            val timeSinceLastAttempt = System.currentTimeMillis() - lastAttemptTime
            val locked = attempts >= 5 && timeSinceLastAttempt < 30000

            if (locked) {
                Timber.w("BruteForceProtection: System locked. Attempts: $attempts/5")
            }
            locked
        } catch (e: Exception) {
            Timber.e(e, "BruteForceProtection: Failed to check lock status")
            false
        }
    }

    /**
     * Get remaining lock time in seconds
     * Returns 0 if not locked
     */
    suspend fun getRemainingLockTimeSeconds(): Int {
        return try {
            val prefs = context.dataStore.data.first()
            val attempts = prefs[FAILED_PIN_ATTEMPTS] ?: 0
            val lastAttemptTime = prefs[LAST_PIN_ATTEMPT_TIME] ?: 0L

            if (attempts >= 5) {
                val timeSinceLastAttempt = System.currentTimeMillis() - lastAttemptTime
                val remainingTime = 30000 - timeSinceLastAttempt
                if (remainingTime > 0) {
                    return (remainingTime / 1000).toInt() + 1
                }
            }
            0
        } catch (e: Exception) {
            Timber.e(e, "BruteForceProtection: Failed to get lock time")
            0
        }
    }

    /**
     * Reset failed attempts after successful authentication
     */
    suspend fun resetAttempts() {
        try {
            context.dataStore.edit { preferences ->
                preferences[FAILED_PIN_ATTEMPTS] = 0
                preferences[LAST_PIN_ATTEMPT_TIME] = 0L
            }
            Timber.d("BruteForceProtection: Attempts reset after successful login")
        } catch (e: Exception) {
            Timber.e(e, "BruteForceProtection: Failed to reset attempts")
        }
    }

    /**
     * Get remaining failed attempts before lock
     */
    suspend fun getRemainingAttempts(): Int {
        return try {
            val prefs = context.dataStore.data.first()
            val attempts = prefs[FAILED_PIN_ATTEMPTS] ?: 0
            val lastAttemptTime = prefs[LAST_PIN_ATTEMPT_TIME] ?: 0L

            // Reset if 30 seconds have passed
            val timeSinceLastAttempt = System.currentTimeMillis() - lastAttemptTime
            val currentAttempts = if (timeSinceLastAttempt > 30000) 0 else attempts

            (5 - currentAttempts).coerceAtLeast(0)
        } catch (e: Exception) {
            Timber.e(e, "BruteForceProtection: Failed to get remaining attempts")
            5
        }
    }
}

