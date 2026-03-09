package com.emul8r.bizap.domain.service

import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.domain.model.InvoiceStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for all revenue calculations across the app.
 *
 * CRITICAL RULE: Revenue = SUM(amountPaid) WHERE status = 'PAID' AND isActive = 1
 * - PAID invoices only (not SENT, PARTIALLY_PAID, DRAFT, etc.)
 * - amountPaid must be > 0
 * - isActive = 1 (exclude soft-deleted invoices)
 * - businessId filter for multi-tenant safety
 *
 * All dashboards (GUI1, GUI2) and reports must use this service.
 * This ensures consistent financial metrics across the app.
 */
@Singleton
class RevenueCalculator @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2
) {
    companion object {
        private const val TAG = "RevenueCalculator"
    }

    /**
     * Observe Month-To-Date (MTD) revenue.
     * Includes all PAID invoices from first day of current month to today.
     * Unit: CENTS (Long)
     */
    fun observeMTDRevenue(businessId: Long): Flow<Long> {
        val today = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = today }

        // Set to first day of month, 00:00:00
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
        ).map { amountCents ->
            Timber.d("$TAG: MTD revenue for business $businessId: ${amountCents}c (${amountCents.toDouble() / 100.0})")
            amountCents
        }
    }

    /**
     * Observe Year-To-Date (YTD) revenue.
     * Includes all PAID invoices from January 1 of current year to today.
     * Unit: CENTS (Long)
     */
    fun observeYTDRevenue(businessId: Long): Flow<Long> {
        val today = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = today }

        // Set to January 1, 00:00:00
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
        ).map { amountCents ->
            Timber.d("$TAG: YTD revenue for business $businessId: ${amountCents}c (${amountCents.toDouble() / 100.0})")
            amountCents
        }
    }

    /**
     * Observe total revenue (all time) for a business.
     * Includes all PAID invoices ever created.
     * Unit: CENTS (Long)
     */
    fun observeTotalRevenue(businessId: Long): Flow<Long> {
        return invoiceDaoV2.observeRevenueInDateRange(
            businessId = businessId,
            startDateMillis = 0L,  // Beginning of time
            endDateMillis = System.currentTimeMillis(),
            status = InvoiceStatus.PAID.name
        ).map { amountCents ->
            Timber.d("$TAG: Total revenue for business $businessId: ${amountCents}c (${amountCents.toDouble() / 100.0})")
            amountCents
        }
    }

    /**
     * Observe revenue for a specific customer.
     * Includes all PAID invoices for this customer.
     * Unit: CENTS (Long)
     */
    fun observeCustomerRevenue(customerId: Long, businessId: Long): Flow<Long> {
        return invoiceDaoV2.observeCustomerPaidAmount(customerId, businessId)
            .map { amountCents ->
                Timber.d("$TAG: Customer $customerId revenue (business $businessId): ${amountCents}c (${amountCents.toDouble() / 100.0})")
                amountCents
            }
    }

    /**
     * Observe revenue for a specific date range.
     * Includes all PAID invoices within the range.
     * Unit: CENTS (Long)
     */
    fun observeRevenueInDateRange(
        businessId: Long,
        startDateMillis: Long,
        endDateMillis: Long
    ): Flow<Long> {
        return invoiceDaoV2.observeRevenueInDateRange(
            businessId = businessId,
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            status = InvoiceStatus.PAID.name
        ).map { amountCents ->
            Timber.d("$TAG: Revenue for business $businessId in date range: ${amountCents}c (${amountCents.toDouble() / 100.0})")
            amountCents
        }
    }

    /**
     * Convert cents to dollars for display.
     * Always use this when formatting revenue for UI.
     */
    fun centsTodollars(cents: Long): Double = cents.toDouble() / 100.0
}

