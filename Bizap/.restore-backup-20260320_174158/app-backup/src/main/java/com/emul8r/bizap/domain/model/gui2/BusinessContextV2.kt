package com.emul8r.bizap.domain.model.gui2

/**
 * Business context passed explicitly through GUI2 navigation.
 * Eliminates ambiguity about the active business.
 */
data class BusinessContextV2(
    val businessId: Long,
    val businessName: String,
    val currencyCode: String = "AUD"
)
