package com.emul8r.bizap.data.repository.analytics

import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.domain.model.gui2.PaymentMetricsV2
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.domain.model.gui2.RiskMetricsV2
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates human-readable diagnostic reports for analytics data.
 *
 * Use this class from the settings/debug screen to diagnose metric discrepancies
 * in production. Every diagnostic run is logged via Timber for audit purposes.
 *
 * Reports include:
 *   - Consistency check (outstanding + collected vs total billed)
 *   - Status breakdown with counts
 *   - Risk tier summary
 *   - Revenue period comparison
 *   - Any validation warnings or errors
 */
@Singleton
class AnalyticsDiagnostics @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2,
    private val calculator: AnalyticsCalculator,
    private val validator: AnalyticsValidator
) {

    /**
     * Structured result of a single diagnostic run.
     */
    data class DiagnosticReport(
        val businessId: Long,
        val generatedAtMs: Long,
        val consistencyCheck: ConsistencyCheck,
        val statusCounts: Map<String, Int>,
        val riskSummary: RiskSummary,
        val revenueSummary: RevenueSummary,
        val warnings: List<String>,
        val errors: List<String>
    ) {
        val isHealthy: Boolean get() = errors.isEmpty()

        override fun toString(): String = buildString {
            appendLine("=== Analytics Diagnostic Report (businessId=$businessId) ===")
            appendLine("Generated: ${java.util.Date(generatedAtMs)}")
            appendLine()
            appendLine("--- Consistency ---")
            appendLine("  outstanding=${consistencyCheck.outstandingCents}¢ + collected=${consistencyCheck.collectedCents}¢ = ${consistencyCheck.calculatedTotalCents}¢")
            appendLine("  totalBilled=${consistencyCheck.totalBilledCents}¢  valid=${consistencyCheck.isConsistent}")
            appendLine()
            appendLine("--- Status Counts ---")
            statusCounts.forEach { (status, count) -> appendLine("  $status: $count") }
            appendLine()
            appendLine("--- Risk Summary ---")
            appendLine("  highRisk=${riskSummary.highRiskCount} atRisk=${riskSummary.atRiskCount} healthy=${riskSummary.healthyCount}")
            appendLine()
            appendLine("--- Revenue Summary ---")
            appendLine("  MTD=${revenueSummary.mtdCents}¢ YTD=${revenueSummary.ytdCents}¢ weekly=${revenueSummary.weeklyCents}¢")
            if (warnings.isNotEmpty()) {
                appendLine()
                appendLine("--- Warnings ---")
                warnings.forEach { appendLine("  ⚠ $it") }
            }
            if (errors.isNotEmpty()) {
                appendLine()
                appendLine("--- Errors ---")
                errors.forEach { appendLine("  ✗ $it") }
            }
        }
    }

    data class ConsistencyCheck(
        val outstandingCents: Long,
        val collectedCents: Long,
        val calculatedTotalCents: Long,
        val totalBilledCents: Long,
        val isConsistent: Boolean
    )

    data class RiskSummary(
        val highRiskCount: Int,
        val atRiskCount: Int,
        val healthyCount: Int
    )

    data class RevenueSummary(
        val mtdCents: Long,
        val ytdCents: Long,
        val weeklyCents: Long
    )

    /**
     * Generates a full diagnostic report for [businessId] by reading live data from the DAO.
     *
     * This is a suspend function intended for debug/diagnostic use only — do not call in
     * the hot path of normal UI rendering.
     *
     * @param businessId The business profile to diagnose.
     * @return A [DiagnosticReport] with all metrics and any detected issues.
     */
    suspend fun generateReport(businessId: Long): DiagnosticReport {
        Timber.d("AnalyticsDiagnostics: generating report for businessId=$businessId")

        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()

        val outstanding = invoiceDaoV2.observeOutstandingAmount(businessId).first()
        val collected = invoiceDaoV2.observeCollectedAmount(businessId).first()
        val totalBilled = outstanding + collected  // derived total for consistency check
        val statusCounts = invoiceDaoV2.observeInvoiceCountByStatus(businessId).first()
        val overdueCount = invoiceDaoV2.observeOverdueCount(businessId).first()
        val highRisk = invoiceDaoV2.observeHighRiskInvoiceCount(businessId).first()
        val atRisk = invoiceDaoV2.observeAtRiskInvoiceCount(businessId).first()
        val healthy = invoiceDaoV2.observeHealthyInvoiceCount(businessId).first()
        val now = System.currentTimeMillis()
        val mtdStart = CalendarUtils.startOfCurrentMonth(now)
        val ytdStart = CalendarUtils.startOfCurrentYear(now)
        val weekStart = now - CalendarUtils.SEVEN_DAYS_MS
        val mtd = invoiceDaoV2.observeMTDRevenue(businessId, mtdStart, now).first()
        val ytd = invoiceDaoV2.observeYTDRevenue(businessId, ytdStart, now).first()
        val weekly = invoiceDaoV2.observeWeeklyRevenue(businessId, weekStart, now).first()

        // Payment metrics validation
        val paymentValidation = validator.validatePaymentMetrics(outstanding, collected, totalBilled)
        if (!paymentValidation.isValid) {
            paymentValidation.error?.let { errors.add("Payment: $it") }
        }
        warnings.addAll(paymentValidation.warnings.map { "Payment: $it" })

        // Revenue metrics validation
        val revenueValidation = validator.validateRevenueMetrics(mtd, ytd, weekly)
        if (!revenueValidation.isValid) {
            revenueValidation.error?.let { errors.add("Revenue: $it") }
        }
        warnings.addAll(revenueValidation.warnings.map { "Revenue: $it" })

        val report = DiagnosticReport(
            businessId = businessId,
            generatedAtMs = System.currentTimeMillis(),
            consistencyCheck = ConsistencyCheck(
                outstandingCents = outstanding,
                collectedCents = collected,
                calculatedTotalCents = outstanding + collected,
                totalBilledCents = totalBilled,
                isConsistent = paymentValidation.isValid
            ),
            statusCounts = statusCounts.associate { it.status to it.count },
            riskSummary = RiskSummary(
                highRiskCount = highRisk,
                atRiskCount = atRisk,
                healthyCount = healthy
            ),
            revenueSummary = RevenueSummary(
                mtdCents = mtd,
                ytdCents = ytd,
                weeklyCents = weekly
            ),
            warnings = warnings,
            errors = errors
        )

        Timber.i("AnalyticsDiagnostics:\n$report")
        return report
    }
}
