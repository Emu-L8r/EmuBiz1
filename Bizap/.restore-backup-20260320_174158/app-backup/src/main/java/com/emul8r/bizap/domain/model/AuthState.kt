package com.emul8r.bizap.domain.model

/**
 * Represents the current authentication state of the app.
 */
sealed class AuthState {
    /** App has never been set up — show PINSetupScreen. */
    object NotInitialized : AuthState()

    /** User is authenticated and session is active — show main content. */
    object Authenticated : AuthState()

    /** Session has expired (5-min idle) — show LoginScreen. */
    object SessionExpired : AuthState()

    /**
     * Too many failed attempts — show lockout message.
     * @param remainingSeconds seconds until the user may try again.
     */
    data class LockedOut(val remainingSeconds: Long) : AuthState()

    /** Wrong PIN entered — show error, allow retry. */
    object InvalidPIN : AuthState()
}
