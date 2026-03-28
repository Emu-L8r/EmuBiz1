package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.gui2.PaymentMetricsV2
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.ui.gui2.common.DateRangeV2

/**
 * Repository for exporting analytics reports in various formats.
 *
 * Provides methods to export revenue and payment metrics as:
 * - PDF reports (professional formatted documents)
 * - CSV data (spreadsheet-ready format)
 */
interface ExportRepository {
    /**
     * Generate PDF report for revenue metrics.
     *
     * @param businessId Business identifier
     * @param businessName Business name for header
     * @param metrics Revenue metrics to include
     * @param dateRange Date range for report
     * @return ByteArray containing PDF data
     */
    suspend fun exportRevenueReportAsPdf(
        businessId: Long,
        businessName: String,
        metrics: RevenueMetricsV2,
        dateRange: DateRangeV2
    ): ByteArray

    /**
     * Generate CSV export for revenue metrics.
     *
     * @param businessId Business identifier
     * @param metrics Revenue metrics to include
     * @param dateRange Date range for report
     * @return String containing CSV data
     */
    suspend fun exportRevenueReportAsCSv(
        businessId: Long,
        metrics: RevenueMetricsV2,
        dateRange: DateRangeV2
    ): String

    /**
     * Generate PDF report for payment metrics.
     *
     * @param businessId Business identifier
     * @param businessName Business name for header
     * @param metrics Payment metrics to include
     * @param dateRange Date range for report
     * @return ByteArray containing PDF data
     */
    suspend fun exportPaymentReportAsPdf(
        businessId: Long,
        businessName: String,
        metrics: PaymentMetricsV2,
        dateRange: DateRangeV2
    ): ByteArray

    /**
     * Generate CSV export for payment metrics.
     *
     * @param businessId Business identifier
     * @param metrics Payment metrics to include
     * @param dateRange Date range for report
     * @return String containing CSV data
     */
    suspend fun exportPaymentReportAsCSv(
        businessId: Long,
        metrics: PaymentMetricsV2,
        dateRange: DateRangeV2
    ): String
}

