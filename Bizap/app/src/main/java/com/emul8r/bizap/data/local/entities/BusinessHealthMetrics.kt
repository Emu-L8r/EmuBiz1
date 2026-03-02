package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Overall business health metrics
 * High-level KPIs for dashboard
 */
@Entity(
    tableName = "business_health_metrics",
    indices = [
        Index(name = "idx_health_business", value = ["businessProfileId"])
    ]
)
data class BusinessHealthMetrics(
    @PrimaryKey
    val businessProfileId: Long,

    // Overall health (0-100)
    val healthScore: Int = 50,
    val healthStatus: String = "NORMAL", // CRITICAL, CAUTION, NORMAL, EXCELLENT

    // Revenue metrics
    val monthlyRecurringRevenue: Double = 0.0,
    val totalOutstandingAmount: Double = 0.0,
    val monthlyAverageRevenue: Double = 0.0,

    // Growth
    val monthOverMonthGrowth: Double = 0.0,
    val yearOverYearGrowth: Double = 0.0,

    // Payment health
    val onTimePaymentRate: Double = 0.0, // % of invoices paid on time
    val averageDaysOutstanding: Int = 0, // DSO
    val overduePercentage: Double = 0.0,

    // Customer health
    val activeCustomerCount: Int = 0,
    val churnedCustomerCount: Int = 0,
    val newCustomersThisMonth: Int = 0,

    // Efficiency
    val invoicesIssuedThisMonth: Int = 0,
    val averageInvoiceValue: Double = 0.0,
    val largestInvoiceValue: Double = 0.0,

    val lastCalculatedAtMs: Long = System.currentTimeMillis()
)




