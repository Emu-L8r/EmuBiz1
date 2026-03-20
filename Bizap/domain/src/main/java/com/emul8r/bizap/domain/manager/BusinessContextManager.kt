package com.emul8r.bizap.domain.manager

import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory manager for the currently active business context.
 *
 * Holds the active business ID as a [StateFlow] so all ViewModels and
 * screens can reactively respond when the user switches businesses.
 *
 * Initialise by calling [setActiveBusinessId] after login / business
 * selection.  Use [requireActiveBusinessId] when a business ID is
 * mandatory; it throws [IllegalStateException] if none has been set.
 *
 * Business profile data (name, currency, etc.) is fetched directly
 * from [BusinessProfileRepository] when needed; this class only tracks
 * *which* business is active.
 */
@Singleton
class BusinessContextManager @Inject constructor(
    private val businessProfileRepository: BusinessProfileRepository
) {
    private val _activeBusinessId = MutableStateFlow<Long?>(null)

    /**
     * The currently active business ID, or `null` if none has been set yet.
     */
    val activeBusinessId: StateFlow<Long?> = _activeBusinessId.asStateFlow()

    /**
     * Set the active business ID (e.g. after login or when the user
     * selects a different business in the switcher screen).
     */
    fun setActiveBusinessId(businessId: Long) {
        Timber.d("BusinessContextManager: switching to businessId=$businessId")
        _activeBusinessId.value = businessId
    }

    /**
     * Returns the current active business ID, or `null` if none is set.
     */
    fun getActiveBusinessId(): Long? = _activeBusinessId.value

    /**
     * Returns the current active business ID.
     *
     * @throws IllegalStateException if no business ID has been set yet.
     */
    fun requireActiveBusinessId(): Long =
        _activeBusinessId.value
            ?: throw IllegalStateException(
                "No active business ID set. Call setActiveBusinessId() before using requireActiveBusinessId()."
            )

    /**
     * Clears the active business ID, resetting it to null.
     * Use this during logout or when the user deselects their business.
     */
    fun clearActiveBusinessId() {
        Timber.d("BusinessContextManager: clearing active business ID")
        _activeBusinessId.value = null
    }
}
