package com.emul8r.bizap.domain.model

import java.util.UUID

/**
 * Represents a tax configuration for a specific country and category.
 *
 * @param id Unique identifier for this tax configuration.
 * @param countryCode ISO 3166-1 alpha-2 country code (e.g., "AU", "US").
 * @param category Tax category or type (e.g., "GST", "VAT", "SALES_TAX").
 * @param name Human-readable name for this tax (e.g., "Australian GST").
 * @param rate Tax rate as a percentage in the range [0.0, 100.0].
 */
data class TaxConfig(
    val id: String = UUID.randomUUID().toString(),
    val countryCode: String,
    val category: String,
    val name: String,
    val rate: Double
)
