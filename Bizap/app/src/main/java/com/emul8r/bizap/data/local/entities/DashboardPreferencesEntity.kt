package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Dashboard preferences entity for storing user customizations.
 *
 * Stores:
 * - Widget order (JSON array)
 * - Hidden widgets (JSON array)
 * - Pinned metrics (JSON array)
 */
@Entity(
    tableName = "dashboard_preferences",
    foreignKeys = [
        ForeignKey(
            entity = BusinessProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessProfileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DashboardPreferencesEntity(
    @PrimaryKey
    val businessProfileId: Long,
    val widgetOrder: String = "[]", // JSON array of widget IDs
    val hiddenWidgets: String = "[]", // JSON array of hidden widget IDs
    val pinnedMetrics: String = "[]", // JSON array of pinned metric IDs
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Available dashboard widgets.
 */
enum class DashboardWidget(val displayName: String) {
    REVENUE_SUMMARY("Revenue Summary"),
    PAYMENT_STATUS("Payment Status"),
    RISK_OVERVIEW("Risk Overview"),
    QUICK_ACTIONS("Quick Actions"),
    INVOICE_STATUS("Invoice Status"),
    SEARCH_BAR("Search"),
    OVERDUE_ALERTS("Overdue Alerts")
}

