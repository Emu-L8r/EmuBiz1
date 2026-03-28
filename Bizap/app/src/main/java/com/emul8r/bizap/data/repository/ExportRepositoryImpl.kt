package com.emul8r.bizap.data.repository

import com.emul8r.bizap.domain.model.gui2.PaymentMetricsV2
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.domain.repository.ExportRepository
import com.emul8r.bizap.ui.gui2.common.DateRangeV2
import com.emul8r.bizap.utils.CentsFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Implementation of ExportRepository for generating PDF and CSV reports.
 *
 * Provides methods to export analytics data in professional formats
 * suitable for sharing with stakeholders or archival.
 */
class ExportRepositoryImpl @Inject constructor() : ExportRepository {

    override suspend fun exportRevenueReportAsPdf(
        businessId: Long,
        businessName: String,
        metrics: RevenueMetricsV2,
        dateRange: DateRangeV2
    ): ByteArray = withContext(Dispatchers.Default) {
        try {
            val reportData = buildRevenueReportData(businessName, metrics, dateRange)
            generatePdfFromText(reportData)
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate revenue PDF report")
            "Error generating revenue report: ${e.message}".toByteArray()
        }
    }

    override suspend fun exportRevenueReportAsCSv(
        businessId: Long,
        metrics: RevenueMetricsV2,
        dateRange: DateRangeV2
    ): String = withContext(Dispatchers.Default) {
        try {
            buildRevenueCsvData(metrics, dateRange)
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate revenue CSV export")
            "Error,${e.message}"
        }
    }

    override suspend fun exportPaymentReportAsPdf(
        businessId: Long,
        businessName: String,
        metrics: PaymentMetricsV2,
        dateRange: DateRangeV2
    ): ByteArray = withContext(Dispatchers.Default) {
        try {
            val reportData = buildPaymentReportData(businessName, metrics, dateRange)
            generatePdfFromText(reportData)
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate payment PDF report")
            "Error generating payment report: ${e.message}".toByteArray()
        }
    }

    override suspend fun exportPaymentReportAsCSv(
        businessId: Long,
        metrics: PaymentMetricsV2,
        dateRange: DateRangeV2
    ): String = withContext(Dispatchers.Default) {
        try {
            buildPaymentCsvData(metrics, dateRange)
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate payment CSV export")
            "Error,${e.message}"
        }
    }

    // ===== REVENUE REPORT BUILDERS =====

    private fun buildRevenueReportData(
        businessName: String,
        metrics: RevenueMetricsV2,
        dateRange: DateRangeV2
    ): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val now = dateFormat.format(Date())

        return """
            REVENUE ANALYTICS REPORT
            ════════════════════════════════════════════════════════════════
            
            Business: $businessName
            Report Date: $now
            Period: ${dateRange.startDate} to ${dateRange.endDate}
            
            ════════════════════════════════════════════════════════════════
            REVENUE SUMMARY
            ════════════════════════════════════════════════════════════════
            
            Month-to-Date (MTD):      ${CentsFormatter.formatCents(metrics.mtdRevenue)}
            Year-to-Date (YTD):       ${CentsFormatter.formatCents(metrics.ytdRevenue)}
            Last 7 Days:              ${CentsFormatter.formatCents(metrics.weeklyRevenue)}
            All-Time Paid:            ${CentsFormatter.formatCents(metrics.totalPaidRevenue)}
            
            ════════════════════════════════════════════════════════════════
            KEY METRICS
            ════════════════════════════════════════════════════════════════
            
            Report generated on $now
            Total metrics included: 4
            
            ════════════════════════════════════════════════════════════════
        """.trimIndent()
    }

    private fun buildRevenueCsvData(
        metrics: RevenueMetricsV2,
        dateRange: DateRangeV2
    ): String {
        return buildString {
            appendLine("Metric,Value,Currency")
            appendLine("Month-to-Date,${metrics.mtdRevenue / 100.0},USD")
            appendLine("Year-to-Date,${metrics.ytdRevenue / 100.0},USD")
            appendLine("Last 7 Days,${metrics.weeklyRevenue / 100.0},USD")
            appendLine("All-Time Paid,${metrics.totalPaidRevenue / 100.0},USD")
            appendLine("")
            appendLine("Report Period,${dateRange.startDate},${dateRange.endDate}")
            appendLine("Generated,${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
        }
    }

    // ===== PAYMENT REPORT BUILDERS =====

    private fun buildPaymentReportData(
        businessName: String,
        metrics: PaymentMetricsV2,
        dateRange: DateRangeV2
    ): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val now = dateFormat.format(Date())

        return """
            PAYMENT ANALYTICS REPORT
            ════════════════════════════════════════════════════════════════
            
            Business: $businessName
            Report Date: $now
            Period: ${dateRange.startDate} to ${dateRange.endDate}
            
            ════════════════════════════════════════════════════════════════
            COLLECTION SUMMARY
            ════════════════════════════════════════════════════════════════
            
            Outstanding Amount:       ${CentsFormatter.formatCents(metrics.outstandingAmount)}
            Collected Amount:         ${CentsFormatter.formatCents(metrics.collectedAmount)}
            Collection Rate:          ${String.format("%.1f%%", metrics.collectionRate)}
            Avg Days to Payment:      ${String.format("%.1f days", metrics.averageDaysToPayment)}
            
            ════════════════════════════════════════════════════════════════
            FINANCIAL HEALTH
            ════════════════════════════════════════════════════════════════
            
            Collection rate indicates the percentage of invoiced amount that
            has been collected. Days to Payment shows the average time from
            invoice creation to payment receipt.
            
            ════════════════════════════════════════════════════════════════
        """.trimIndent()
    }

    private fun buildPaymentCsvData(
        metrics: PaymentMetricsV2,
        dateRange: DateRangeV2
    ): String {
        return buildString {
            appendLine("Metric,Value,Unit")
            appendLine("Outstanding Amount,${metrics.outstandingAmount / 100.0},USD")
            appendLine("Collected Amount,${metrics.collectedAmount / 100.0},USD")
            appendLine("Collection Rate,${metrics.collectionRate},%")
            appendLine("Average Days to Payment,${metrics.averageDaysToPayment},days")
            appendLine("")
            appendLine("Report Period,${dateRange.startDate},${dateRange.endDate}")
            appendLine("Generated,${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
        }
    }

    // ===== PDF GENERATION =====

    private fun generatePdfFromText(text: String): ByteArray {
        // For now, return as plain text in PDF format
        // In production, use a proper PDF library like iText or pdfbox
        return text.toByteArray()
    }
}

