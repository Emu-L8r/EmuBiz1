package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.PINStorage
import com.emul8r.bizap.data.local.SessionManager
import com.emul8r.bizap.domain.repository.AuthenticationRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [AuthenticationRepository].
 * Delegates PIN operations to [PINStorage] and session operations to [SessionManager].
 */
@Singleton
class AuthenticationRepositoryImpl @Inject constructor(
    private val pinStorage: PINStorage,
    private val sessionManager: SessionManager
) : AuthenticationRepository {

    override fun isPINSet(): Boolean = pinStorage.isPINSet()

    override suspend fun setupPIN(pin: String): Result<Unit> = pinStorage.setupPIN(pin)

    override suspend fun verifyPIN(pin: String): Result<Boolean> = pinStorage.verifyPIN(pin)

    override suspend fun clearPIN(): Result<Unit> = pinStorage.clearPIN()

    override fun startSession() = sessionManager.startSession()

    override fun updateLastInteraction() = sessionManager.updateLastInteraction()

    override fun isSessionValid(): Boolean = sessionManager.isSessionValid()

    override fun endSession() = sessionManager.endSession()

    override fun getTimeUntilAutoLock(): Long = sessionManager.getTimeUntilAutoLock()
}
