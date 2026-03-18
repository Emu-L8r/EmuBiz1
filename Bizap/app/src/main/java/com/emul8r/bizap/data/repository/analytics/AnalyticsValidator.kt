package com.emul8r.bizap.data.repository.analytics

import timber.log.Timber
import kotlin.math.absoluteValue

/**
 * Validates mathematical invariants for analytics metrics before they reach the UI.
 *
 * Invariants enforced:
 *   - outstanding + collected ≈ totalBilled (±$1 / 100 cents tolerance)
 *   - No negative monetary amounts
 *   - collectionRate bounded [0.0, 100.0]
 *   - agingBuckets.sum() ≈ outstanding
 *
 * Provided by Hilt via [com.emul8r.bizap.di.AnalyticsModule].
 */
class AnalyticsValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val error: String? = null,
        val warnings: List<String> = emptyList()
    )

    companion object {
        /** Allowable rounding error in cents (±$1). */
        private const val CENTS_TOLERANCE = 100L
        private const val COLLECTION_RATE_MIN = 0.0
        private const val COLLECTION_RATE_MAX = 100.0
    }

    /**
     * Validates payment metrics mathematical invariants.
     *
     * @param outstanding Total unpaid amount in cents (SENT + PARTIALLY_PAID + OVERDUE).
     * @param collected   Total collected amount in cents.
     * @param totalBilled Total billed amount in cents (outstanding + collected should ≈ this).
     * @return [ValidationResult] indicating whether the data is consistent.
     */
    fun validatePaymentMetrics(
        outstanding: Long,
        collected: Long,
        totalBilled: Long
    ): ValidationResult {
        val warnings = mutableListOf<String>()

        // Check: No negative amounts
        if (outstanding < 0L || collected < 0L) {
            val msg = "Negative amounts detected: outstanding=$outstanding collected=$collected"
            Timber.w("AnalyticsValidator: $msg")
            return ValidationResult(isValid = false, error = msg)
        }

        // Check: outstanding + collected ≈ totalBilled (±tolerance)
        val calculatedTotal = outstanding + collected
        if ((calculatedTotal - totalBilled).absoluteValue > CENTS_TOLERANCE) {
            val msg = "outstanding($outstanding) + collected($collected) = $calculatedTotal ≠ totalBilled($totalBilled)"
            Timber.w("AnalyticsValidator: $msg")
            return ValidationResult(isValid = false, error = msg)
        }

        if (calculatedTotal != totalBilled) {
            val diff = (calculatedTotal - totalBilled).absoluteValue
            warnings.add("Minor rounding discrepancy: ${diff}¢ (within tolerance)")
        }

        return ValidationResult(isValid = true, warnings = warnings)
    }

    /**
     * Validates revenue metrics consistency.
     *
     * @param mtdRevenue   Month-to-date revenue in cents.
     * @param ytdRevenue   Year-to-date revenue in cents.
     * @param weeklyRevenue Last-7-days revenue in cents.
     * @return [ValidationResult] indicating whether the metrics are consistent.
     */
    fun validateRevenueMetrics(
        mtdRevenue: Long,
        ytdRevenue: Long,
        weeklyRevenue: Long
    ): ValidationResult {
        val warnings = mutableListOf<String>()

        if (mtdRevenue < 0L || ytdRevenue < 0L || weeklyRevenue < 0L) {
            val msg = "Negative revenue: mtd=$mtdRevenue ytd=$ytdRevenue weekly=$weeklyRevenue"
            Timber.w("AnalyticsValidator: $msg")
            return ValidationResult(isValid = false, error = msg)
        }

        // MTD should not exceed YTD
        if (mtdRevenue > ytdRevenue) {
            val msg = "MTD revenue ($mtdRevenue) exceeds YTD revenue ($ytdRevenue)"
            Timber.w("AnalyticsValidator: $msg")
            warnings.add(msg)
        }

        return ValidationResult(isValid = true, warnings = warnings)
    }

    /**
     * Validates that collection rate is within [0, 100].
     *
     * @param collectionRate The rate as a percentage value (0.0–100.0).
     * @return [ValidationResult] indicating whether the rate is valid.
     */
    fun validateCollectionRate(collectionRate: Double): ValidationResult {
        if (collectionRate < COLLECTION_RATE_MIN || collectionRate > COLLECTION_RATE_MAX) {
            val msg = "collectionRate=$collectionRate out of bounds [0, 100]"
            Timber.w("AnalyticsValidator: $msg")
            return ValidationResult(isValid = false, error = msg)
        }
        return ValidationResult(isValid = true)
    }

    /**
     * Validates that aging bucket totals match the outstanding amount.
     *
     * @param buckets     Map of bucket label to amount in cents.
     * @param outstanding Total outstanding amount in cents.
     * @return [ValidationResult] indicating whether the buckets sum correctly.
     */
    fun validateAgingBuckets(
        buckets: Map<String, Long>,
        outstanding: Long
    ): ValidationResult {
        val bucketSum = buckets.values.sum()
        if ((bucketSum - outstanding).absoluteValue > CENTS_TOLERANCE) {
            val msg = "agingBuckets.sum()=$bucketSum ≠ outstanding=$outstanding"
            Timber.w("AnalyticsValidator: $msg")
            return ValidationResult(isValid = false, error = msg)
        }
        return ValidationResult(isValid = true)
    }
}
