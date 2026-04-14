package com.emul8r.bizap.auth

import android.app.ActivityManager
import android.content.Context
import com.emul8r.bizap.domain.model.AuthState
import com.emul8r.bizap.domain.repository.AuthenticationRepository
import com.emul8r.bizap.domain.service.AuthenticationManager
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for [AuthenticationManager].
 */
class AuthenticationManagerTest {

    private val authRepo: AuthenticationRepository = mockk(relaxed = true)
    private val mockContext: Context = mockk(relaxed = true)
    private val mockActivityManager: ActivityManager = mockk(relaxed = true)

    private lateinit var manager: AuthenticationManager

    @Before
    fun setUp() {
        every { mockContext.getSystemService(Context.ACTIVITY_SERVICE) } returns mockActivityManager
        manager = AuthenticationManager(mockContext, authRepo)
    }

    // ── setupInitialPIN ───────────────────────────────────────────────────────

    @Test
    fun `setupInitialPIN stores PIN and starts session`() = runTest {
        coEvery { authRepo.setupPIN("1234") } returns Result.success(Unit)

        val result = manager.setupInitialPIN("1234")

        assertTrue(result.isSuccess)
        verify { authRepo.startSession() }
        coVerify { authRepo.setupPIN("1234") }
    }

    @Test
    fun `setupInitialPIN fails for PIN shorter than 4 digits`() = runTest {
        val result = manager.setupInitialPIN("123")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        coVerify(exactly = 0) { authRepo.setupPIN(any()) }
    }

    // ── authenticate ─────────────────────────────────────────────────────────

    @Test
    fun `authenticate returns Authenticated for correct PIN`() = runTest {
        coEvery { authRepo.verifyPIN("1234") } returns Result.success(true)

        val result = manager.authenticate("1234")

        assertTrue(result.isSuccess)
        assertIs<AuthState.Authenticated>(result.getOrThrow())
        verify { authRepo.startSession() }
    }

    @Test
    fun `authenticate returns InvalidPIN for wrong PIN`() = runTest {
        coEvery { authRepo.verifyPIN("9999") } returns Result.success(false)

        val result = manager.authenticate("9999")

        assertTrue(result.isSuccess)
        assertIs<AuthState.InvalidPIN>(result.getOrThrow())
        verify(exactly = 0) { authRepo.startSession() }
    }

    @Test
    fun `authenticate increments failed attempt counter`() = runTest {
        coEvery { authRepo.verifyPIN(any()) } returns Result.success(false)

        // 4 failures → still InvalidPIN
        for (i in 0 until AuthenticationManager.MAX_FAILED_ATTEMPTS - 1) {
            val state = manager.authenticate("9999").getOrThrow()
            assertIs<AuthState.InvalidPIN>(state)
        }

        // 5th failure → LockedOut
        val state = manager.authenticate("9999").getOrThrow()
        assertIs<AuthState.LockedOut>(state)
    }

    @Test
    fun `authenticate returns LockedOut after max failures`() = runTest {
        coEvery { authRepo.verifyPIN(any()) } returns Result.success(false)

        for (i in 0 until AuthenticationManager.MAX_FAILED_ATTEMPTS) {
            manager.authenticate("9999")
        }

        // Now locked out — even correct PIN should return LockedOut
        coEvery { authRepo.verifyPIN("1234") } returns Result.success(true)
        val state = manager.authenticate("1234").getOrThrow()
        assertIs<AuthState.LockedOut>(state)
    }

    @Test
    fun `successful authenticate resets failed attempt counter`() = runTest {
        coEvery { authRepo.verifyPIN("9999") } returns Result.success(false)
        coEvery { authRepo.verifyPIN("1234") } returns Result.success(true)

        // 2 failures
        for (i in 0 until 2) {
            manager.authenticate("9999")
        }

        // Successful login resets counter
        manager.authenticate("1234")

        // Now 4 more failures should not trigger lockout (counter was reset)
        for (i in 0 until AuthenticationManager.MAX_FAILED_ATTEMPTS - 1) {
            val state = manager.authenticate("9999").getOrThrow()
            assertIs<AuthState.InvalidPIN>(state)
        }
    }

    // ── checkSessionValidity ──────────────────────────────────────────────────

    @Test
    fun `checkSessionValidity returns NotInitialized when no PIN set`() {
        every { authRepo.isPINSet() } returns false

        val state = manager.checkSessionValidity()
        assertIs<AuthState.NotInitialized>(state)
    }

    @Test
    fun `checkSessionValidity returns Authenticated when session is valid`() {
        every { authRepo.isPINSet() } returns true
        every { authRepo.isSessionValid() } returns true

        val state = manager.checkSessionValidity()
        assertIs<AuthState.Authenticated>(state)
    }

    @Test
    fun `checkSessionValidity returns SessionExpired when session timed out`() {
        every { authRepo.isPINSet() } returns true
        every { authRepo.isSessionValid() } returns false

        val state = manager.checkSessionValidity()
        assertIs<AuthState.SessionExpired>(state)
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    fun `logout clears session`() {
        val result = manager.logout()

        assertTrue(result.isSuccess)
        verify { authRepo.endSession() }
    }

    // ── resetPINAndData ───────────────────────────────────────────────────────

    @Test
    fun `resetPINAndData clears session and calls clearApplicationUserData`() {
        every { mockActivityManager.clearApplicationUserData() } returns true

        val result = manager.resetPINAndData()

        assertTrue(result.isSuccess)
        verify { authRepo.endSession() }
        verify { mockActivityManager.clearApplicationUserData() }
    }
}



