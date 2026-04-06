package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.InvoicePeriodData

/**
 * Repository interface for invoice trend analytics.
 *
 * Provides aggregated invoice statistics by week or month,
 * decoupling the analytics screens from the data layer DAO types.
 */
interface InvoiceAnalyticsRepository {
    /** Returns invoice statistics grouped by week for the last [months] months. */
    suspend fun getWeeklyInvoiceTrend(businessId: Long, months: Int): List<InvoicePeriodData>

    /** Returns invoice statistics grouped by month for the last [months] months. */
    suspend fun getMonthlyInvoiceTrend(businessId: Long, months: Int): List<InvoicePeriodData>
}
