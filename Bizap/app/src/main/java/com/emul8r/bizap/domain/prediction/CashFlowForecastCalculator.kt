package com.emul8r.bizap.domain.prediction

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.insights.CashFlowPrediction
import com.emul8r.bizap.domain.model.insights.RiskLevel
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Calculates cash flow forecasts for the next 30/60/90 days.
 *
 * **Algorithm:**
 * - Projected inflows: Sum of all invoices due and unpaid within forecast period
 * - Projected outflows: 0 (expenses not tracked - can be added later)
 * - Risk level: Based on projected balance
 * - Confidence: Based on data recency and amount of outstanding invoices
 *
 * **Usage:**
 * ```kotlin
 * val calculator = CashFlowForecastCalculator()
 * val forecast = calculator.forecast(invoices, days = 30)
 * ```
 */
class CashFlowForecastCalculator @Inject constructor() {

    /**
     * Predict cash flow for the next N days.
     *
     * @param invoices All invoices to analyze
     * @param days Forecast horizon (typically 30, 60, or 90)
     * @return CashFlowPrediction with projected balance, inflows, outflows, and risk level
     */
    fun forecast(
        invoices: List<Invoice>,
        days: Int = 30
    ): CashFlowPrediction {
        Timber.d("📊 CashFlowForecastCalculator: Starting forecast for $days days with ${invoices.size} invoices")

        val today = LocalDate.now()
        val forecastDate = today.plusDays(days.toLong())

        // Calculate projected inflows (expected payments for invoices due within forecast period)
        val projectedInflows = calculateProjectedInflows(invoices, today, forecastDate)

        // Calculate projected outflows (currently 0, can be extended for expense tracking)
        val projectedOutflows = calculateProjectedOutflows(invoices, today, forecastDate)

        // Calculate projected balance
        val projectedBalance = projectedInflows - projectedOutflows

        // Determine risk level
        val riskLevel = determineRiskLevel(projectedBalance)

        // Calculate confidence score
        val confidence = calculateConfidence(invoices, projectedInflows)

        // Generate recommendations
        val recommendations = generateRecommendations(projectedBalance, projectedInflows, invoices)

        Timber.d(
            """
            📊 Forecast Results:
               - Projected Inflows: $${"%.2f".format(projectedInflows / 100.0)}
               - Projected Outflows: $${"%.2f".format(projectedOutflows / 100.0)}
               - Projected Balance: $${"%.2f".format(projectedBalance / 100.0)}
               - Risk Level: $riskLevel
               - Confidence: ${String.format("%.1f%%", confidence)}
            """.trimIndent()
        )

        return CashFlowPrediction(
            predictedBalance = projectedBalance,
            confidence = confidence,
            forecastDays = days,
            projectedInflows = projectedInflows,
            projectedOutflows = projectedOutflows,
            riskLevel = riskLevel,
            recommendations = recommendations
        )
    }

    /**
     * Calculate expected inflows from invoices.
     * Only includes unpaid invoices due within the forecast period.
     */
    private fun calculateProjectedInflows(
        invoices: List<Invoice>,
        today: LocalDate,
        forecastDate: LocalDate
    ): Long {
        return invoices
            .filter { invoice ->
                // Include only unpaid invoices
                invoice.status != InvoiceStatus.PAID &&
                invoice.status != InvoiceStatus.CANCELLED &&
                // That are due within forecast period
                isInvoiceDueWithinPeriod(invoice, today, forecastDate)
            }
            .sumOf { invoice ->
                // Sum remaining balance (total - paid)
                invoice.totalAmount - invoice.amountPaid
            }
    }

    /**
     * Calculate projected outflows (expenses).
     * Currently returns 0 - can be extended for expense tracking in future.
     */
    private fun calculateProjectedOutflows(
        invoices: List<Invoice>,
        today: LocalDate,
        forecastDate: LocalDate
    ): Long {
        // TODO: Add expense tracking when available
        // For now, assume no outflows
        return 0L
    }

    /**
     * Check if invoice due date falls within forecast period.
     */
    private fun isInvoiceDueWithinPeriod(
        invoice: Invoice,
        today: LocalDate,
        forecastDate: LocalDate
    ): Boolean {
        return try {
            val dueDate = Instant.parse(invoice.dueDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            dueDate >= today && dueDate <= forecastDate
        } catch (e: Exception) {
            Timber.w("Failed to parse invoice due date: ${invoice.dueDate}", e)
            false
        }
    }

    /**
     * Determine risk level based on projected balance.
     */
    private fun determineRiskLevel(projectedBalance: Long): RiskLevel {
        return when {
            projectedBalance < 0 -> {
                Timber.w("🔴 CRITICAL: Negative cash flow projected: $${"%.2f".format(projectedBalance / 100.0)}")
                RiskLevel.RISK
            }
            projectedBalance < 50000 -> {
                Timber.w("🟡 CAUTION: Low projected cash balance: $${"%.2f".format(projectedBalance / 100.0)}")
                RiskLevel.CAUTION
            }
            else -> {
                Timber.d("✅ HEALTHY: Strong projected cash balance: $${"%.2f".format(projectedBalance / 100.0)}")
                RiskLevel.HEALTHY
            }
        }
    }

    /**
     * Calculate confidence score based on data quality.
     * - Higher confidence with more outstanding invoices
     * - Lower confidence with aged data
     */
    private fun calculateConfidence(invoices: List<Invoice>, projectedInflows: Long): Double {
        // Base confidence is 80%
        var confidence = 80.0

        // More outstanding invoices = higher confidence (up to 95%)
        val unpaidCount = invoices.count { it.status != InvoiceStatus.PAID }
        val confidenceBoost = (unpaidCount.toDouble() / maxOf(1.0, invoices.size.toDouble())) * 15.0
        confidence += minOf(15.0, confidenceBoost)

        // If projected inflows are very low, reduce confidence
        if (projectedInflows < 10000) {
            confidence -= 10.0
        }

        return confidence.coerceIn(50.0, 95.0)
    }

    /**
     * Generate actionable recommendations based on forecast.
     */
    private fun generateRecommendations(
        projectedBalance: Long,
        projectedInflows: Long,
        invoices: List<Invoice>
    ): List<String> {
        val recommendations = mutableListOf<String>()

        // Add balance summary
        recommendations.add("Expected cash balance: $${"%.2f".format(projectedBalance / 100.0)}")

        // Risk-based recommendations
        when {
            projectedBalance < 0 -> {
                recommendations.add("⚠️ ALERT: Negative cash flow projected")
                recommendations.add("💡 Action: Accelerate customer payment collections")
                recommendations.add("💡 Action: Review payment terms with customers")
            }
            projectedBalance < 50000 -> {
                recommendations.add("🟡 Low cash balance expected")
                recommendations.add("💡 Consider payment reminders to accelerate collections")
                recommendations.add("💡 Monitor cash flow closely")
            }
            else -> {
                recommendations.add("✅ Healthy cash position projected")
                recommendations.add("💡 Opportunity: Consider strategic investments")
            }
        }

        // Add inflows summary
        recommendations.add("Projected inflows: $${"%.2f".format(projectedInflows / 100.0)}")

        // Overdue invoice warnings
        val overdueCount = invoices.count { it.status == InvoiceStatus.OVERDUE }
        if (overdueCount > 0) {
            recommendations.add("⚠️ $overdueCount overdue invoices need attention")
        }

        return recommendations
    }
}

