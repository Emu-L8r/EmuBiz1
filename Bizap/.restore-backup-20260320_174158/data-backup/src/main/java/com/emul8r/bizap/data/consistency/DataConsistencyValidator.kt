package com.emul8r.bizap.data.consistency

import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.dao.AnalyticsDao
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates that snapshot data is consistent with the invoices table (source of truth).
 *
 * Uses a 1¢ tolerance threshold for floating-point comparison.
 * Run on app startup and periodically to detect and surface stale snapshots.
 */
@Singleton
class DataConsistencyValidator @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val analyticsDao: AnalyticsDao
) {
    companion object {
        /** Tolerance in cents (1¢) for floating-point comparison. */
        private const val TOLERANCE_CENTS = 1L

        private const val TAG = "DataConsistencyValidator"
    }

    /**
     * Runs a daily health check comparing snapshot totals against calculated invoice totals.
     * Logs warnings when discrepancies exceed the tolerance threshold.
     *
     * @param businessId The business profile to validate.
     * @return [ConsistencyReport] describing any discrepancies found.
     */
    suspend fun runDailyHealthCheck(businessId: Long): ConsistencyReport {
        Timber.d("$TAG: Running daily health check for businessId=$businessId")

        return try {
            val calculatedRevenue = invoiceDao.observeTotalPaidRevenue(businessId).first()
            val snapshotRevenue = analyticsDao.getTotalPaidRevenueLong(businessId)

            val revenueDiff = kotlin.math.abs(calculatedRevenue - snapshotRevenue)
            val isConsistent = revenueDiff <= TOLERANCE_CENTS

            if (!isConsistent) {
                Timber.w(
                    "$TAG: ⚠️ SNAPSHOT INCONSISTENCY DETECTED for businessId=$businessId — " +
                        "calculated=${calculatedRevenue}¢, snapshot=${snapshotRevenue}¢, " +
                        "diff=${revenueDiff}¢"
                )
            } else {
                Timber.d(
                    "$TAG: ✅ Snapshots consistent for businessId=$businessId " +
                        "(revenue=${calculatedRevenue}¢)"
                )
            }

            ConsistencyReport(
                businessId = businessId,
                isConsistent = isConsistent,
                calculatedRevenueCents = calculatedRevenue,
                snapshotRevenueCents = snapshotRevenue,
                revenueDiffCents = revenueDiff,
                checkedAtMs = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Health check failed for businessId=$businessId")
            ConsistencyReport(
                businessId = businessId,
                isConsistent = false,
                error = e.message,
                checkedAtMs = System.currentTimeMillis()
            )
        }
    }
}

/**
 * Result of a data consistency check.
 */
data class ConsistencyReport(
    val businessId: Long,
    val isConsistent: Boolean,
    val calculatedRevenueCents: Long = 0L,
    val snapshotRevenueCents: Long = 0L,
    val revenueDiffCents: Long = 0L,
    val error: String? = null,
    val checkedAtMs: Long = System.currentTimeMillis()
)
