package com.emul8r.bizap.auth

import com.emul8r.bizap.data.local.SessionManager
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [SessionManager].
 */
class SessionManagerTest {

    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        sessionManager = SessionManager()
    }

    // ── startSession / isSessionValid ─────────────────────────────────────────

    @Test
    fun `session is invalid before startSession`() {
        assertFalse(sessionManager.isSessionValid())
    }

    @Test
    fun `session is valid immediately after startSession`() {
        sessionManager.startSession()
        assertTrue(sessionManager.isSessionValid())
    }

    // ── endSession ────────────────────────────────────────────────────────────

    @Test
    fun `endSession clears session`() {
        sessionManager.startSession()
        sessionManager.endSession()
        assertFalse(sessionManager.isSessionValid())
    }

    // ── updateLastInteraction ─────────────────────────────────────────────────

    @Test
    fun `updateLastInteraction is a no-op when no session is active`() {
        sessionManager.updateLastInteraction()
        // Should not throw and session should remain invalid
        assertFalse(sessionManager.isSessionValid())
    }

    // ── getTimeUntilAutoLock ──────────────────────────────────────────────────

    @Test
    fun `getTimeUntilAutoLock returns 0 when no session active`() {
        assertEquals(0L, sessionManager.getTimeUntilAutoLock())
    }

    @Test
    fun `getTimeUntilAutoLock is positive immediately after startSession`() {
        sessionManager.startSession()
        assertTrue(sessionManager.getTimeUntilAutoLock() > 0L)
    }

    @Test
    fun `getTimeUntilAutoLock is 0 after endSession`() {
        sessionManager.startSession()
        sessionManager.endSession()
        assertEquals(0L, sessionManager.getTimeUntilAutoLock())
    }

    @Test
    fun `getTimeUntilAutoLock is close to SESSION_TIMEOUT_MS right after startSession`() {
        sessionManager.startSession()
        val remaining = sessionManager.getTimeUntilAutoLock()
        // Allow 200ms tolerance for test execution time
        assertTrue(remaining > SessionManager.SESSION_TIMEOUT_MS - 200)
        assertTrue(remaining <= SessionManager.SESSION_TIMEOUT_MS)
    }
}
