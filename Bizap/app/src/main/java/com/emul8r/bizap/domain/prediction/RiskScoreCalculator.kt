package com.emul8r.bizap.domain.prediction

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.insights.RiskLevel
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Calculates risk scores for individual invoices and customers.
 *
 * **Risk Factors (Total = 100 points):**
 * - Overdue status: 40 points
 * - Days overdue: 20 points (max)
 * - Customer payment history: 30 points
 * - Invoice amount relative to business size: 10 points
 *
 * **Risk Levels:**
 * - CRITICAL: 70+ points
 * - WARNING: 40-69 points
 * - LOW: <40 points
 */
class RiskScoreCalculator @Inject constructor() {

    /**
     * Calculate risk score for a single invoice.
     *
     * @param invoice The invoice to analyze
     * @param customerPaymentHistory All previous invoices from this customer
     * @param businessMetrics Current business metrics (for relative sizing)
     * @return InvoiceRiskScore with calculated score and risk level
     */
    fun calculateInvoiceRisk(
        invoice: Invoice,
        customerPaymentHistory: List<Invoice> = emptyList(),
        totalBusinessRevenue: Long = 100000L
    ): InvoiceRiskScore {
        Timber.d("🔍 Calculating risk score for invoice ${invoice.id}")

        var riskScore = 0.0
        val factors = mutableListOf<String>()

        // Factor 1: Overdue status (40 points max)
        val overduePoints = calculateOverduePoints(invoice)
        riskScore += overduePoints
        if (overduePoints > 0) factors.add("Overdue: +${overduePoints.toInt()} points")

        // Factor 2: Days overdue (20 points max)
        val daysOverduePoints = calculateDaysOverduePoints(invoice)
        riskScore += daysOverduePoints
        if (daysOverduePoints > 0) factors.add("Days late: +${daysOverduePoints.toInt()} points")

        // Factor 3: Customer payment history (30 points)
        val historyPoints = calculateHistoryPoints(customerPaymentHistory)
        riskScore += historyPoints
        if (historyPoints > 0) factors.add("Payment history: +${historyPoints.toInt()} points")

        // Factor 4: Amount relative to business (10 points)
        val sizePoints = calculateSizePoints(invoice, totalBusinessRevenue)
        riskScore += sizePoints
        if (sizePoints > 0) factors.add("Invoice size: +${sizePoints.toInt()} points")

        // Determine risk level
        val riskLevel = when {
            riskScore >= 70 -> RiskLevel.RISK
            riskScore >= 40 -> RiskLevel.CAUTION
            else -> RiskLevel.HEALTHY
        }

        val score = InvoiceRiskScore(
            invoiceId = invoice.id,
            score = riskScore.coerceIn(0.0, 100.0),
            level = riskLevel,
            factors = factors
        )

        Timber.d(
            "Risk Score Result: Invoice ${invoice.id} = ${String.format("%.1f", score.score)} points ($riskLevel)"
        )

        return score
    }

    /**
     * Overdue status check: 40 points if overdue, 0 if not.
     */
    private fun calculateOverduePoints(invoice: Invoice): Double {
        return if (invoice.status == InvoiceStatus.OVERDUE) {
            40.0
        } else {
            0.0
        }
    }

    /**
     * Days overdue calculation: Up to 20 points based on days past due date.
     * Formula: min(20, daysOverdue * 2)
     */
    private fun calculateDaysOverduePoints(invoice: Invoice): Double {
        return try {
            val today = LocalDate.now()
            val dueDate = Instant.parse(invoice.dueDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val daysOverdue = ChronoUnit.DAYS.between(dueDate, today).toInt()

            if (daysOverdue > 0) {
                minOf(20.0, daysOverdue * 2.0)
            } else {
                0.0
            }
        } catch (e: Exception) {
            Timber.w("Failed to parse invoice due date: ${invoice.dueDate}", e)
            0.0
        }
    }

    /**
     * Customer payment history: 30 points based on late payment percentage.
     * Formula: (latePaymentRate) * 30
     */
    private fun calculateHistoryPoints(customerPaymentHistory: List<Invoice>): Double {
        if (customerPaymentHistory.isEmpty()) {
            return 0.0
        }

        val paidInvoices = customerPaymentHistory.filter { it.status == InvoiceStatus.PAID }

        if (paidInvoices.isEmpty()) {
            return 30.0  // No history, assume worst case
        }

        // Count late payments (paid but after due date)
        val latePayments = paidInvoices.count { invoice ->
            try {
                val dueDate = Instant.parse(invoice.dueDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val today = LocalDate.now()
                ChronoUnit.DAYS.between(dueDate, today) > 0
            } catch (e: Exception) {
                false
            }
        }

        val latePaymentRate = latePayments.toDouble() / paidInvoices.size
        return latePaymentRate * 30.0
    }

    /**
     * Invoice size relative to business: 10 points if invoice > 50% of expected monthly revenue.
     */
    private fun calculateSizePoints(invoice: Invoice, totalBusinessRevenue: Long): Double {
        val percentOfRevenue = invoice.totalAmount.toDouble() / maxOf(1L, totalBusinessRevenue)

        return if (percentOfRevenue > 0.5) {
            10.0
        } else {
            0.0
        }
    }
}

/**
 * Result of invoice risk score calculation.
 */
data class InvoiceRiskScore(
    val invoiceId: Long,
    val score: Double,  // 0-100, higher = riskier
    val level: RiskLevel,
    val factors: List<String> = emptyList()
)


