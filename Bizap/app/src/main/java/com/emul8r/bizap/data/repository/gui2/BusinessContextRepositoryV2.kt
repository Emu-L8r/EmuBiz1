package com.emul8r.bizap.data.repository.gui2

import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.model.gui2.BusinessContextV2
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.entities.InvoiceStatusCountV2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GUI2 business context repository.
 * Exposes the active business profile as a typed BusinessContextV2.
 * Used to pass explicit businessId through the GUI2 navigation graph.
 *
 * SPRINT 3 FIX: Now also exposes invoice status counts through repository interface
 * instead of requiring direct DAO access from ViewModels. This ensures ViewModels
 * only depend on repository abstractions, not data layer DAOs.
 */
@Singleton
class BusinessContextRepositoryV2 @Inject constructor(
    private val businessProfileRepository: BusinessProfileRepository,
    private val invoiceDaoV2: InvoiceDaoV2
) {
    /**
     * Reactive stream of the active business context.
     * Uses distinctUntilChanged to prevent redundant UI updates and logging.
     */
    val activeContext: Flow<BusinessContextV2> =
        businessProfileRepository.activeProfile
            .distinctUntilChanged()
            .map { profile ->
                Timber.d("BusinessContextRepositoryV2: active profile = ${profile.id} '${profile.businessName}'")
                profile.toContextV2()
            }

    /**
     * Reactive stream of the active business ID.
     * Use this in ViewModels to avoid hardcoded business ID defaults.
     */
    fun observeActiveBusinessId(): Flow<Long> =
        businessProfileRepository.activeProfile
            .map { it.id }
            .distinctUntilChanged()
            .also {
                // Side effect for logging when ID actually changes
                Timber.d("BusinessContextRepositoryV2: active businessId stream initialized")
            }

    /**
     * All business profiles as contexts.
     * Uses distinctUntilChanged to prevent redundant emissions and UI updates.
     */
    val allContexts: Flow<List<BusinessContextV2>> =
        businessProfileRepository.allProfiles
            .map { profiles ->
                profiles.map { it.toContextV2() }
            }
            .distinctUntilChanged()

    /**
     * Invoice count by status (SPRINT 3 ADDITION).
     * Exposes invoice status breakdown without requiring ViewModels to import DAOs.
     * Returns counts for: PAID, PARTIALLY_PAID, SENT, OVERDUE, CANCELLED only.
     *
     * Performance: distinctUntilChanged() prevents redundant UI updates when
     * invoice counts haven't changed, reducing database queries and recompositions.
     */
    fun observeInvoiceCountByStatus(businessId: Long): Flow<List<InvoiceStatusCountV2>> =
        invoiceDaoV2.observeInvoiceCountByStatus(businessId)
            .distinctUntilChanged()

    private fun BusinessProfile.toContextV2() = BusinessContextV2(
        businessId = id,
        businessName = businessName.ifBlank { "My Business" },
        currencyCode = "AUD"
    )
}
