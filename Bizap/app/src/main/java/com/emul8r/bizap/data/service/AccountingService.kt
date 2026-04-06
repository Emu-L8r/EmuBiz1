package com.emul8r.bizap.data.service

import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.dao.PaymentDaoV2
import com.emul8r.bizap.domain.model.InvoiceStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for all accounting calculations.
 *
 * RULES (Enforced Everywhere):
 * 1. All queries MUST filter by businessId (multi-tenant safety)
 * 2. All queries MUST filter isActive = 1 (soft-delete safety)
 * 3. Outstanding = SUM(totalAmount - amountPaid) WHERE status IN [SENT, PARTIALLY_PAID, OVERDUE]
 * 4. Collected = SUM(amountPaid) WHERE status IN [PAID, PARTIALLY_PAID]
 * 5. Revenue = SUM(amountPaid) WHERE status = PAID (cash basis)
 * 6. MTD = First day of month 00:00 to today 23:59
 * 7. YTD = Jan 1 00:00 to today 23:59
 * 8. All amounts in CENTS (Long), never dollars
 * 9. Excludes DRAFT invoices from all financial metrics
 * 10. Date filtering uses invoice.date field (creation date), not updated_at
 */
@Singleton
class AccountingService @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2,
    private val paymentDaoV2: PaymentDaoV2
) {
    companion object {
        private const val TAG = "AccountingService"
    }

    // ===== OUTSTANDING AMOUNT =====
    /**
     * Outstanding = Total Billed - Total Paid (for non-PAID invoices)
     * Excludes: DRAFT, PAID (they have no outstanding)
     * Unit: CENTS (Long)
     */
    fun observeOutstandingAmount(businessId: Long): Flow<Long> {
        return invoiceDaoV2.observeOutstandingAmountForStatuses(
            businessId = businessId,
            statuses = listOf(
                InvoiceStatus.SENT.name,
                InvoiceStatus.PARTIALLY_PAID.name,
                InvoiceStatus.OVERDUE.name
            )
        ).map { amount ->
            Timber.d("%s: Outstanding for business %d: %dc", TAG, businessId, amount)
            amount
        }
    }

    // ===== COLLECTED AMOUNT =====
    /**
     * Collected = SUM(amountPaid)
     * Includes: PAID, PARTIALLY_PAID (any invoice with partial or full payment)
     * Unit: CENTS (Long)
     */
    fun observeCollectedAmount(businessId: Long): Flow<Long> {
        return invoiceDaoV2.observeCollectedAmountForStatuses(
            businessId = businessId,
            statuses = listOf(
                InvoiceStatus.PAID.name,
                InvoiceStatus.PARTIALLY_PAID.name
            )
        ).map { amount ->
            Timber.d("%s: Collected for business %d: %dc", TAG, businessId, amount)
            amount
        }
    }

    // ===== BILLED AMOUNT =====
    /**
     * Billed = SUM(totalAmount)
     * Includes: All invoices except DRAFT
     * Unit: CENTS (Long)
     */
    fun observeBilledAmount(businessId: Long): Flow<Long> {
        return invoiceDaoV2.observeBilledAmount(
            businessId = businessId,
            excludeStatuses = listOf(InvoiceStatus.DRAFT.name)
        ).map { amount ->
            Timber.d("%s: Billed for business %d: %dc", TAG, businessId, amount)
            amount
        }
    }

    // ===== COLLECTION RATE =====
    /**
     * Collection Rate = (Collected / Billed) * 100
     * Returns: Percentage (0-100)
     * Safety: Returns 0 if Billed is 0
     */
    fun observeCollectionRate(businessId: Long): Flow<Double> {
        return invoiceDaoV2.observeCollectionMetrics(businessId).map { metrics ->
            val collectionRate = if (metrics.billedAmount > 0) {
                (metrics.collectedAmount.toDouble() / metrics.billedAmount.toDouble()) * 100
            } else {
                0.0
            }
            Timber.d("%s: Collection rate for business %d: %.2f%%", TAG, businessId, collectionRate)
            collectionRate
        }
    }

    // ===== MONTH-TO-DATE REVENUE =====
    /**
     * MTD Revenue = SUM(amountPaid) WHERE invoice.date >= first day of month AND status = PAID
     * Excludes: DRAFT invoices, non-PAID
     * Unit: CENTS (Long)
     * Date: Uses calendar boundaries (00:00 to 23:59)
     */
    fun observeMTDRevenue(businessId: Long): Flow<Long> {
        val today = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = today }
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val monthStartMillis = calendar.timeInMillis

        return invoiceDaoV2.observeRevenueInDateRange(
            businessId = businessId,
            startDateMillis = monthStartMillis,
            endDateMillis = today,
            status = InvoiceStatus.PAID.name
        ).map { amount ->
            Timber.d("%s: MTD revenue for business %d: %dc", TAG, businessId, amount)
            amount
        }
    }

    // ===== YEAR-TO-DATE REVENUE =====
    /**
     * YTD Revenue = SUM(amountPaid) WHERE invoice.date >= Jan 1 of current year AND status = PAID
     * Excludes: DRAFT invoices, non-PAID
     * Unit: CENTS (Long)
     */
    fun observeYTDRevenue(businessId: Long): Flow<Long> {
        val today = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = today }
        calendar.set(Calendar.MONTH, Calendar.JANUARY)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val yearStartMillis = calendar.timeInMillis

        return invoiceDaoV2.observeRevenueInDateRange(
            businessId = businessId,
            startDateMillis = yearStartMillis,
            endDateMillis = today,
            status = InvoiceStatus.PAID.name
        ).map { amount ->
            Timber.d("%s: YTD revenue for business %d: %dc", TAG, businessId, amount)
            amount
        }
    }

    // ===== INVOICE COUNT METRICS =====
    /**
     * Count of unpaid invoices (outstanding).
     * Includes: SENT, PARTIALLY_PAID, OVERDUE
     */
    fun observeUnpaidInvoiceCount(businessId: Long): Flow<Int> {
        return invoiceDaoV2.observeInvoiceCountForStatuses(
            businessId = businessId,
            statuses = listOf(
                InvoiceStatus.SENT.name,
                InvoiceStatus.PARTIALLY_PAID.name,
                InvoiceStatus.OVERDUE.name
            )
        )
    }

    /**
     * Count of paid invoices.
     */
    fun observePaidInvoiceCount(businessId: Long): Flow<Int> {
        return invoiceDaoV2.observeInvoiceCountForStatuses(
            businessId = businessId,
            statuses = listOf(InvoiceStatus.PAID.name)
        )
    }

    /**
     * Count of all official invoices (excludes DRAFT).
     */
    fun observeTotalInvoiceCount(businessId: Long): Flow<Int> {
        return invoiceDaoV2.observeInvoiceCountForStatuses(
            businessId = businessId,
            statuses = listOf(
                InvoiceStatus.SENT.name,
                InvoiceStatus.PARTIALLY_PAID.name,
                InvoiceStatus.OVERDUE.name,
                InvoiceStatus.PAID.name
            )
        )
    }
}
