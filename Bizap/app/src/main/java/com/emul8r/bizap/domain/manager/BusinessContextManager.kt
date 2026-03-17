package com.emul8r.bizap.domain.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton that maintains the active business context across navigation.
 *
 * Solves the navigation safety problem where [businessId] can be silently lost
 * when a screen is launched without explicit route arguments. The manager acts as
 * an in-memory fallback so that every screen can always resolve the active business.
 *
 * Usage:
 *  - Call [setActiveBusinessId] whenever a user selects or navigates into a business.
 *  - Collect [activeBusinessId] in ViewModels as an additional safety layer.
 *  - In navigation graphs, always prefer explicit route args; use this as a backstop.
 *
 * Note: This state is NOT persisted across process restarts. The persistent source
 * of truth for the selected business remains [BusinessProfileRepository.activeProfile].
 */
@Singleton
class BusinessContextManager @Inject constructor() {

    private val _activeBusinessId = MutableStateFlow<Long?>(null)

    /** Reactive stream of the currently active business ID. Null if none is set. */
    val activeBusinessId: StateFlow<Long?> = _activeBusinessId.asStateFlow()

    /**
     * Set the active business ID. Call this whenever a user navigates into
     * a business-specific screen so that subsequent navigation can resolve the context.
     */
    fun setActiveBusinessId(businessId: Long) {
        Timber.d("BusinessContextManager: setting active businessId=$businessId")
        _activeBusinessId.value = businessId
    }

    /**
     * Returns the active business ID, or null if none has been set.
     */
    fun getActiveBusinessId(): Long? = _activeBusinessId.value

    /**
     * Returns the active business ID, or throws [IllegalStateException] if none is set.
     * Use this in contexts where a missing business ID is a programming error.
     */
    fun requireActiveBusinessId(): Long =
        _activeBusinessId.value
            ?: throw IllegalStateException(
                "No active business context. Call setActiveBusinessId() before navigating."
            )

    /**
     * Clears the active business context. Called on logout or when no business is selected.
     */
    fun clearActiveBusinessId() {
        Timber.d("BusinessContextManager: clearing active businessId")
        _activeBusinessId.value = null
    }
}
