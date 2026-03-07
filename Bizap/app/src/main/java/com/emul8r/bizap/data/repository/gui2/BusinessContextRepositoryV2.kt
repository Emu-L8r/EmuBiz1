package com.emul8r.bizap.data.repository.gui2

import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.model.gui2.BusinessContextV2
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GUI2 business context repository.
 * Exposes the active business profile as a typed BusinessContextV2.
 * Used to pass explicit businessId through the GUI2 navigation graph.
 */
@Singleton
class BusinessContextRepositoryV2 @Inject constructor(
    private val businessProfileRepository: BusinessProfileRepository
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
     * All business profiles as contexts.
     */
    val allContexts: Flow<List<BusinessContextV2>> =
        businessProfileRepository.allProfiles.map { profiles ->
            profiles.map { it.toContextV2() }
        }

    private fun BusinessProfile.toContextV2() = BusinessContextV2(
        businessId = id,
        businessName = businessName.ifBlank { "My Business" },
        currencyCode = "AUD"
    )
}
