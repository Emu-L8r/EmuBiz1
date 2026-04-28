package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.AuthState

/**
 * Repository interface for PIN authentication and session management.
 */
interface AuthenticationRepository {

    /** Returns true if a PIN has been configured for this app installation. */
    suspend fun isPINSet(): Boolean

    /**
     * Stores a new PIN (hashed). Used on first launch.
     * @return [Result.success] on success, [Result.failure] if an error occurred.
     */
    suspend fun setupPIN(pin: String): Result<Unit>

    /**
     * Checks whether the supplied PIN matches the stored hash.
     * @return [Result.success(true)] if correct, [Result.success(false)] if wrong,
     *         or [Result.failure] on storage error.
     */
    suspend fun verifyPIN(pin: String): Result<Boolean>

    /**
     * Removes the stored PIN. Called during "Forgot PIN" flow before clearing
     * all app data.
     */
    suspend fun clearPIN(): Result<Unit>

    // ── Session management ────────────────────────────────────────────────────

    /** Starts a new authenticated session (records current time). */
    fun startSession()

    /** Updates the last-interaction timestamp. Called on every user touch/scroll. */
    fun updateLastInteraction()

    /** Returns true if an active session exists AND it has not timed out. */
    fun isSessionValid(): Boolean

    /** Terminates the current session (logout). */
    fun endSession()

    /**
     * Returns milliseconds remaining before the session auto-locks.
     * Returns 0 if there is no active session or it has already expired.
     */
    fun getTimeUntilAutoLock(): Long
}
