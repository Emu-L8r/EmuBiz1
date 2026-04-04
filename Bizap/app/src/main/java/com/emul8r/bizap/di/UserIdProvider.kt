package com.emul8r.bizap.di

import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides the current user ID across the entire application.
 *
 * This is the single source of truth for user identification.
 * All repositories and services that need a user ID should inject this provider
 * instead of hardcoding or maintaining their own user ID.
 *
 * Returns the Firebase Auth UID when signed in, or `"anonymous"` as a safe fallback.
 */
@Singleton
class UserIdProvider @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    /**
     * Get the current user ID.
     *
     * @return Firebase Auth UID when the user is signed in,
     *         or `"anonymous"` when signed out / auth unavailable.
     */
    fun getCurrentUserId(): String {
        return try {
            firebaseAuth.currentUser?.uid ?: "anonymous"
        } catch (e: Exception) {
            Timber.w(e, "UserIdProvider: Failed to retrieve Firebase Auth UID — falling back to 'anonymous'")
            "anonymous"
        }
    }
}

