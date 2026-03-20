package com.emul8r.bizap.data.local

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks user session lifetime.
 *
 * A session is started via [startSession] after a successful PIN entry and
 * automatically expires after [SESSION_TIMEOUT_MS] of inactivity.
 * Every user interaction (touch, scroll, etc.) should call
 * [updateLastInteraction] to reset the idle timer.
 */
@Singleton
class SessionManager @Inject constructor() {

    private var sessionActive: Boolean = false
    private var lastInteractionTime: Long = 0L

    // ── Public API ────────────────────────────────────────────────────────────

    /** Marks the start of an authenticated session. */
    fun startSession() {
        sessionActive = true
        lastInteractionTime = now()
    }

    /**
     * Refreshes the idle timer.
     * Should be called from [MainActivity.dispatchTouchEvent] on every touch event.
     * No-op if no session is active.
     */
    fun updateLastInteraction() {
        if (sessionActive) {
            lastInteractionTime = now()
        }
    }

    /**
     * Returns true if a session is active AND the user has interacted within
     * the last [SESSION_TIMEOUT_MS] milliseconds.
     */
    fun isSessionValid(): Boolean {
        if (!sessionActive) return false
        return (now() - lastInteractionTime) < SESSION_TIMEOUT_MS
    }

    /** Clears the session (logout or PIN reset). */
    fun endSession() {
        sessionActive = false
        lastInteractionTime = 0L
    }

    /**
     * Returns milliseconds until the session auto-locks.
     * Returns 0 when no session is active or it has already expired.
     */
    fun getTimeUntilAutoLock(): Long {
        if (!sessionActive) return 0L
        val elapsed = now() - lastInteractionTime
        return maxOf(0L, SESSION_TIMEOUT_MS - elapsed)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun now(): Long = System.currentTimeMillis()

    // ── Constants ─────────────────────────────────────────────────────────────

    companion object {
        /** 5-minute session timeout. */
        const val SESSION_TIMEOUT_MS = 5 * 60 * 1000L
    }
}
