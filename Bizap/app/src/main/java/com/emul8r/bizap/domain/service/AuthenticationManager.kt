package com.emul8r.bizap.domain.service

import android.app.ActivityManager
import android.content.Context
import com.emul8r.bizap.domain.model.AuthState
import com.emul8r.bizap.domain.repository.AuthenticationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central authentication orchestrator.
 *
 * Responsibilities:
 * - PIN setup (first launch)
 * - PIN verification (login)
 * - Session validity checks (auto-lock)
 * - Logout
 * - "Forgot PIN" — clears ALL app data and restarts the process
 *
 * Failed-attempt tracking and lockout logic live here so they survive
 * screen rotations (singleton scope).
 */
@Singleton
class AuthenticationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthenticationRepository
) {

    private var failedAttempts: Int = 0
    private var lockoutStartTime: Long = 0L

    // ── First-launch setup ────────────────────────────────────────────────────

    /**
     * Stores a hashed version of [pin] for future logins.
     * PIN must be at least 4 digits (caller should validate; this enforces it too).
     */
    suspend fun setupInitialPIN(pin: String): Result<Unit> {
        if (pin.length < MIN_PIN_LENGTH) {
            return Result.failure(IllegalArgumentException("PIN must be at least $MIN_PIN_LENGTH digits"))
        }
        return authRepository.setupPIN(pin).also { result ->
            if (result.isSuccess) {
                Timber.d("AuthenticationManager: PIN set up successfully")
                authRepository.startSession()
            }
        }
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    /**
     * Attempts to authenticate with [pin].
     *
     * Returns:
     * - [AuthState.Authenticated] — correct PIN, session started
     * - [AuthState.InvalidPIN] — wrong PIN, attempt counter incremented
     * - [AuthState.LockedOut] — 5+ failures, shows remaining lockout seconds
     */
    suspend fun authenticate(pin: String): Result<AuthState> {
        // Check if still in lockout window
        if (isLockedOut()) {
            val remaining = getLockoutRemainingSeconds()
            Timber.w("AuthenticationManager: locked out, $remaining s remaining")
            return Result.success(AuthState.LockedOut(remaining))
        }

        return authRepository.verifyPIN(pin).map { isValid ->
            if (isValid) {
                failedAttempts = 0
                lockoutStartTime = 0L
                authRepository.startSession()
                Timber.d("AuthenticationManager: authenticated successfully")
                AuthState.Authenticated
            } else {
                failedAttempts++
                Timber.w("AuthenticationManager: wrong PIN, attempt #$failedAttempts")
                if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                    lockoutStartTime = System.currentTimeMillis()
                    Timber.w("AuthenticationManager: too many failures — locked out")
                    AuthState.LockedOut(LOCKOUT_DURATION_MS / 1000)
                } else {
                    AuthState.InvalidPIN
                }
            }
        }
    }

    // ── Session ───────────────────────────────────────────────────────────────

    /**
     * Checks the current session state. Should be called:
     * - On app foreground
     * - On every navigation action
     *
     * Returns:
     * - [AuthState.NotInitialized] — PIN never configured
     * - [AuthState.Authenticated] — valid session
     * - [AuthState.SessionExpired] — session timed out, re-login required
     * - [AuthState.LockedOut] — still in lockout window
     *
     * **Note:** Now includes error handling for DataStore operations.
     */
    fun checkSessionValidity(): AuthState {
        return try {
            when {
                !authRepository.isPINSet() -> AuthState.NotInitialized
                isLockedOut() -> AuthState.LockedOut(getLockoutRemainingSeconds())
                authRepository.isSessionValid() -> AuthState.Authenticated
                else -> AuthState.SessionExpired
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error checking session validity, defaulting to SessionExpired")
            AuthState.SessionExpired
        }
    }

    /** Delegates touch events to the session manager to reset the idle timer. */
    fun updateLastInteraction() = authRepository.updateLastInteraction()

    /** Returns milliseconds until the session auto-locks. */
    fun getTimeUntilAutoLock(): Long = authRepository.getTimeUntilAutoLock()

    // ── Logout ────────────────────────────────────────────────────────────────

    /**
     * Ends the current session. The PIN is NOT cleared — user must re-enter
     * their existing PIN to access the app again.
     */
    fun logout(): Result<Unit> = runCatching {
        authRepository.endSession()
        Timber.d("AuthenticationManager: session ended (logout)")
    }

    // ── Forgot PIN ────────────────────────────────────────────────────────────

    /**
     * Clears the PIN and wipes ALL app data, then restarts the process.
     * This is the "nuclear" option — the user loses all local data.
     *
     * [ActivityManager.clearApplicationUserData] terminates the process;
     * the OS relaunches MainActivity, which will find no PIN set and show
     * [PINSetupScreen].
     */
    fun resetPINAndData(): Result<Unit> = runCatching {
        authRepository.endSession()
        Timber.i("AuthenticationManager: resetting PIN and clearing all app data")
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.clearApplicationUserData()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun isLockedOut(): Boolean {
        if (lockoutStartTime == 0L) return false
        return (System.currentTimeMillis() - lockoutStartTime) < LOCKOUT_DURATION_MS
    }

    private fun getLockoutRemainingSeconds(): Long {
        val elapsed = System.currentTimeMillis() - lockoutStartTime
        return maxOf(0L, (LOCKOUT_DURATION_MS - elapsed) / 1000)
    }

    // ── Constants ─────────────────────────────────────────────────────────────

    companion object {
        const val MIN_PIN_LENGTH = 4
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 30_000L // 30 seconds
    }
}
