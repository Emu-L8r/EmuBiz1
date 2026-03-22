package com.emul8r.bizap.data.repository.gui2

import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.model.gui2.BusinessContextV2
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.entities.InvoiceStatusCountV2
import kotlinx.coroutines.flow.Flow
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
     */
    val activeContext: Flow<BusinessContextV2> =
        businessProfileRepository.activeProfile.map { profile ->
            Timber.d("BusinessContextRepositoryV2: active profile = ${profile.id} '${profile.businessName}'")
            profile.toContextV2()
        }

    /**
     * Reactive stream of the active business ID.
     * Use this in ViewModels to avoid hardcoded business ID defaults.
     */
    fun observeActiveBusinessId(): Flow<Long> =
        businessProfileRepository.activeProfile.map { profile ->
            Timber.d("BusinessContextRepositoryV2: active businessId = ${profile.id}")
            profile.id
        }

    /**
     * All business profiles as contexts.
     */
    val allContexts: Flow<List<BusinessContextV2>> =
        businessProfileRepository.allProfiles.map { profiles ->
            profiles.map { it.toContextV2() }
        }

    /**
     * Invoice count by status (SPRINT 3 ADDITION).
     * Exposes invoice status breakdown without requiring ViewModels to import DAOs.
     * Returns counts for: PAID, PARTIALLY_PAID, SENT, OVERDUE, CANCELLED only.
     */
    fun observeInvoiceCountByStatus(businessId: Long): Flow<List<InvoiceStatusCountV2>> =
        invoiceDaoV2.observeInvoiceCountByStatus(businessId)
            .also { Timber.d("BusinessContextRepositoryV2: observing invoice status counts for business $businessId") }

    private fun BusinessProfile.toContextV2() = BusinessContextV2(
        businessId = id,
        businessName = businessName.ifBlank { "My Business" },
        currencyCode = "AUD"
    )
}
