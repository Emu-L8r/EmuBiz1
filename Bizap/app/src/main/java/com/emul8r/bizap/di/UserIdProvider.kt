package com.emul8r.bizap.di

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides the current user ID across the entire application.
 *
 * This is the single source of truth for user identification.
 * All repositories and services that need a user ID should inject this provider
 * instead of hardcoding or maintaining their own user ID.
 *
 * TODO: In production, this should integrate with Firebase Authentication:
 * ```
 * val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
 * ```
 */
@Singleton
class UserIdProvider @Inject constructor() {

    /**
     * Get the current user ID.
     *
     * @return Current user ID. Currently returns "current_user" (development default).
     *         In production, should return Firebase UID or actual user identifier.
     */
    fun getCurrentUserId(): String {
        // TODO: Replace with actual auth provider
        // For now, returns development default
        return "current_user"
    }
}

