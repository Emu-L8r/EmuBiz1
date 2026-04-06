package com.emul8r.bizap.domain.model

/**
 * A single period's invoice statistics for trend charts.
 *
 * Represents aggregated invoice counts for one time period (week or month),
 * with no dependency on persistence layer types.
 */
data class InvoicePeriodData(
    val periodLabel: String,
    val totalCount: Int,
    val paidCount: Int,
    val sentCount: Int
)
