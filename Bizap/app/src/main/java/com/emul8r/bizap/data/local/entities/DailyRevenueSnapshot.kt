package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Daily aggregated revenue snapshot
 * Created daily for fast trending queries
 */
@Entity(
    tableName = "daily_revenue_snapshots",
    indices = [
        Index(name = "idx_daily_business", value = ["businessProfileId"]),
        Index(name = "idx_daily_date", value = ["dateString"])
    ]
)
data class DailyRevenueSnapshot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val businessProfileId: Long,
    val dateString: String, // YYYY-MM-DD format
    val dateMs: Long,

    // Revenue aggregates
    val totalRevenue: Long = 0,                    // Cents
    val invoiceCount: Int = 0,
    val paidInvoiceCount: Int = 0,
    val draftInvoiceCount: Int = 0,
    val averageInvoiceAmount: Long = 0,           // Cents

    // By currency
    val currencyBreakdown: String = "{}", // JSON: {AUD: 1000, USD: 500}

    // Growth metrics
    val dayOverDayGrowth: Double = 0.0, // Percentage
    val weekOverWeekGrowth: Double = 0.0,

    val snapshotCreatedAtMs: Long = System.currentTimeMillis()
)




