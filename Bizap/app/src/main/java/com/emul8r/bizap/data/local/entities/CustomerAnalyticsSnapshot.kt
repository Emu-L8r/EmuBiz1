package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Customer-level analytics snapshot.
 * Tracks customer health, segment, and churn risk.
 */
@Entity(
    tableName = "customer_analytics_snapshots",
    indices = [
        Index(name = "idx_cust_analytics_business", value = ["businessProfileId"]),
        Index(name = "idx_cust_analytics_ltv", value = ["customerLifetimeValue"]),
        Index(name = "idx_cust_analytics_segment", value = ["segment"]),
        Index(name = "idx_cust_analytics_churn", value = ["isPredictedToChurn"])
    ]
)
data class CustomerAnalyticsSnapshot(
    @PrimaryKey
    val customerId: Long,
    val businessProfileId: Long,
    val customerName: String,
    val customerEmail: String?,

    // Revenue metrics
    val totalRevenue: Long = 0,         // Cents
    val invoiceCount: Int = 0,
    val paidInvoiceCount: Int = 0,
    val overdueInvoiceCount: Int = 0,
    val averageInvoiceAmount: Long = 0, // Cents

    // Customer value (LTV)
    val customerLifetimeValue: Long = 0, // Cents
    val estimatedLTV: Long = 0,          // Cents, future-looking metric
    val isTopCustomer: Boolean = false,

    // Segmentation & Behavior
    val segment: String = "NEW", // NEW, LOYAL, AT_RISK, DORMANT
    val purchaseVelocity: Double = 0.0,
    val averageDaysBetweenPurchases: Double = 0.0,
    val daysSinceLastPurchase: Int = 0,

    // Churn Risk
    val churnRiskScore: Double = 0.0,
    val isPredictedToChurn: Boolean = false,
    val churnRiskFactors: String = "[]", // JSON array of factors

    // Activity
    val lastInvoiceDateMs: Long? = null,
    val lastPaymentDateMs: Long? = null,
    val isActive: Boolean = true,
    val riskScore: Int = 0,

    val snapshotCreatedAtMs: Long = System.currentTimeMillis(),
    val lastUpdatedMs: Long = System.currentTimeMillis()
)

