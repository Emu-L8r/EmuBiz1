package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.TaxConfig

/**
 * Domain-level contract for Tax configuration operations.
 *
 * Constraints:
 * - Tax rates must be in the range [0.0, 100.0]
 * - Country codes must be valid ISO 3166-1 alpha-2 codes (2 uppercase letters)
 */
interface TaxRepository {

    /**
     * Retrieves the primary tax rate for a given country and category.
     *
     * @param countryCode ISO 3166-1 alpha-2 country code.
     * @param category Tax category (e.g., "GST", "VAT").
     * @return [Result.success] with the rate on success, or [Result.failure] if the country code
     * is invalid, no config is found, or a data operation fails.
     */
    suspend fun getTaxRate(countryCode: String, category: String): Result<Double>

    /**
     * Retrieves all applicable tax configurations for a given country.
     *
     * @param countryCode ISO 3166-1 alpha-2 country code.
     * @return [Result.success] with the list of applicable taxes, or [Result.failure] if the
     * country code is invalid or a data operation fails.
     */
    suspend fun getApplicableTaxes(countryCode: String): Result<List<TaxConfig>>

    /**
     * Saves or updates a tax configuration.
     *
     * @return [Result.success] on success, or [Result.failure] if the rate is out of the
     * [0.0, 100.0] range, the country code is invalid, or a data operation fails.
     */
    suspend fun updateTaxRate(config: TaxConfig): Result<Unit>

    /**
     * Removes a tax configuration by its ID.
     *
     * @return [Result.success] on success, or [Result.failure] if the ID is not found or a
     * data operation fails.
     */
    suspend fun deleteTaxConfig(id: String): Result<Unit>
}
