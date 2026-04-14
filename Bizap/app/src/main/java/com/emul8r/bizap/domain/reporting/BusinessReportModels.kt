package com.emul8r.bizap.domain.reporting

import com.emul8r.bizap.domain.model.Invoice
import java.time.LocalDate

/**
 * Data models for advanced business reports.
 *
 * Supports multiple report types:
 * - Cash flow forecasts
 * - Risk analysis
 * - Invoice aging
 * - Customer performance
 * - Revenue forecasts
 */

/**
 * Base interface for all reports.
 */
interface BusinessReport {
    val reportName: String
    val generatedDate: LocalDate
    val businessId: Long
}

/**
 * Cash Flow Report - Shows projected cash flow for different time periods.
 *
 * **Metrics**:
 * - 30-day forecast
 * - 60-day forecast
 * - 90-day forecast
 * - Confidence levels
 * - Risk assessment
 */
data class CashFlowReport(
    override val reportName: String = "Cash Flow Forecast Report",
    override val generatedDate: LocalDate = LocalDate.now(),
    override val businessId: Long,
    val forecast30Days: CashFlowMetrics,
    val forecast60Days: CashFlowMetrics,
    val forecast90Days: CashFlowMetrics,
    val recommendations: List<String> = emptyList()
) : BusinessReport

data class CashFlowMetrics(
    val periodDays: Int,
    val projectedBalance: Long,
    val projectedInflows: Long,
    val projectedOutflows: Long,
    val confidence: Double,
    val riskLevel: String
)

/**
 * Risk Analysis Report - Identifies high-risk invoices and customers.
 *
 * **Metrics**:
 * - Total at-risk invoices
 * - Critical risk invoices
 * - Risk by customer
 * - Risk factors breakdown
 */
data class RiskAnalysisReport(
    override val reportName: String = "Risk Analysis Report",
    override val generatedDate: LocalDate = LocalDate.now(),
    override val businessId: Long,
    val totalAtRiskInvoices: Int,
    val criticalRiskInvoices: Int,
    val warningRiskInvoices: Int,
    val riskByCustomer: Map<String, Int>,
    val topRiskFactors: List<RiskFactor>
) : BusinessReport

data class RiskFactor(
    val factor: String,
    val occurrences: Int,
    val impact: String
)

/**
 * Invoice Aging Report - Shows overdue and upcoming due invoices.
 *
 * **Metrics**:
 * - Current invoices (due within 7 days)
 * - Overdue 1-30 days
 * - Overdue 31-60 days
 * - Overdue 60+ days
 */
data class InvoiceAgingReport(
    override val reportName: String = "Invoice Aging Report",
    override val generatedDate: LocalDate = LocalDate.now(),
    override val businessId: Long,
    val currentDue: List<AgingBucket>,
    val overdue1to30: List<AgingBucket>,
    val overdue31to60: List<AgingBucket>,
    val overdue60Plus: List<AgingBucket>,
    val totalOverdueAmount: Long
) : BusinessReport

data class AgingBucket(
    val invoiceId: Long,
    val customerId: Long,
    val customerName: String,
    val invoiceAmount: Long,
    val dueDate: LocalDate,
    val daysOverdue: Int
)

/**
 * Customer Performance Report - Analyzes customer payment patterns.
 *
 * **Metrics**:
 * - Total invoices per customer
 * - Payment rate
 * - Average payment delay
 * - Lifetime value
 */
data class CustomerPerformanceReport(
    override val reportName: String = "Customer Performance Report",
    override val generatedDate: LocalDate = LocalDate.now(),
    override val businessId: Long,
    val customers: List<CustomerMetrics>
) : BusinessReport

data class CustomerMetrics(
    val customerId: Long,
    val customerName: String,
    val totalInvoices: Int,
    val paidInvoices: Int,
    val paymentRate: Double, // percentage
    val averagePaymentDelayDays: Int,
    val lifetimeValue: Long,
    val totalOutstanding: Long
)

/**
 * Revenue Forecast Report - Projects future revenue based on current invoices.
 *
 * **Metrics**:
 * - This month revenue (actual + projected)
 * - Next 30 days
 * - Next 60 days
 * - Next 90 days
 */
data class RevenueForecastReport(
    override val reportName: String = "Revenue Forecast Report",
    override val generatedDate: LocalDate = LocalDate.now(),
    override val businessId: Long,
    val currentMonthProjection: RevenueProjection,
    val next30Days: RevenueProjection,
    val next60Days: RevenueProjection,
    val next90Days: RevenueProjection,
    val totalProjectedRevenue: Long
) : BusinessReport

data class RevenueProjection(
    val periodName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val projectedAmount: Long,
    val invoiceCount: Int,
    val confidence: Double
)

