package com.emul8r.bizap.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Customer-level analytics snapshot
 * Tracks customer health and value
 */
@Entity(
    tableName = "customer_analytics_snapshots",
    indices = [
        Index(name = "idx_cust_analytics_business", value = ["businessProfileId"]),
        Index(name = "idx_cust_analytics_ltv", value = ["customerLifetimeValue"])
    ]
)
data class CustomerAnalyticsSnapshot(
    @PrimaryKey
    val customerId: Int,

    val businessProfileId: Int,
    val customerName: String,
    val customerEmail: String?,

    // Revenue metrics
    val totalRevenue: Double = 0.0,
    val invoiceCount: Int = 0,
    val paidInvoiceCount: Int = 0,
    val overdueInvoiceCount: Int = 0,
    val averageInvoiceAmount: Double = 0.0,

    // Customer value
    val customerLifetimeValue: Double = 0.0,
    val isTopCustomer: Boolean = false, // Top 20%

    // Payment behavior
    val averageDaysToPayment: Int = 0,
    val paymentRate: Double = 0.0, // % of invoices paid
    val lastInvoiceDateMs: Long? = null,
    val lastPaymentDateMs: Long? = null,

    // Health
    val isActive: Boolean = true, // Had invoice in last 90 days
    val riskScore: Int = 0, // 0-100, higher = riskier

    val snapshotCreatedAtMs: Long = System.currentTimeMillis()
)

