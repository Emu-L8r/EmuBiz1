package com.emul8r.bizap.data.repository

import com.emul8r.bizap.domain.model.TaxConfig
import com.emul8r.bizap.domain.repository.TaxRepository
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [TaxRepository] backed by an in-memory store.
 *
 * Tax rates are validated to be within [0.0, 100.0] and country codes must be
 * valid ISO 3166-1 alpha-2 codes (2 uppercase letters).
 */
@Singleton
class TaxRepositoryImpl @Inject constructor() : TaxRepository {

    private val taxConfigs = ConcurrentHashMap<String, TaxConfig>()

    override suspend fun getTaxRate(countryCode: String, category: String): Result<Double> = runCatching {
        requireValidCountryCode(countryCode)
        val config = taxConfigs.values.firstOrNull {
            it.countryCode.equals(countryCode, ignoreCase = true) &&
                it.category.equals(category, ignoreCase = true)
        }
        requireNotNull(config) { "No tax configuration found for country '$countryCode' and category '$category'" }
        Timber.d("✅ Retrieved tax rate ${config.rate}% for $countryCode/$category")
        config.rate
    }.also { result ->
        result.onFailure { e ->
            Timber.e(e, "❌ Failed to get tax rate for $countryCode/$category")
        }
    }

    override suspend fun getApplicableTaxes(countryCode: String): Result<List<TaxConfig>> = runCatching {
        requireValidCountryCode(countryCode)
        val taxes = taxConfigs.values.filter {
            it.countryCode.equals(countryCode, ignoreCase = true)
        }
        Timber.d("✅ Retrieved ${taxes.size} tax configs for $countryCode")
        taxes
    }.also { result ->
        result.onFailure { e ->
            Timber.e(e, "❌ Failed to get applicable taxes for $countryCode")
        }
    }

    override suspend fun updateTaxRate(config: TaxConfig): Result<Unit> = runCatching {
        requireValidCountryCode(config.countryCode)
        require(config.rate in 0.0..100.0) {
            "Tax rate must be between 0 and 100, was ${config.rate}"
        }
        taxConfigs[config.id] = config
        Timber.d("✅ Updated tax config '${config.name}' (${config.rate}%) for ${config.countryCode}")
    }.also { result ->
        result.onFailure { e ->
            Timber.e(e, "❌ Failed to update tax config ${config.id}")
        }
    }

    override suspend fun deleteTaxConfig(id: String): Result<Unit> = runCatching {
        requireNotNull(taxConfigs.remove(id)) { "Tax configuration with ID '$id' not found" }
        Timber.d("✅ Deleted tax config $id")
    }.also { result ->
        result.onFailure { e ->
            Timber.e(e, "❌ Failed to delete tax config $id")
        }
    }

    private fun requireValidCountryCode(countryCode: String) {
        require(countryCode.length == 2 && countryCode.all { it.isLetter() && it.isUpperCase() }) {
            "Country code must be a 2-letter uppercase ISO 3166-1 alpha-2 code, was '$countryCode'"
        }
    }
}
