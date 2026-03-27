package com.emul8r.bizap.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for analytics events.
 *
 * Stores all invoice-related analytics events captured throughout the app.
 * Events are used for:
 * - Calculating trends and metrics
 * - User behavior analysis
 * - Reporting and insights
 * - Audit trails
 *
 * **Data Consistency:**
 * - business_id ensures multi-tenant isolation
 * - timestamp enables chronological analysis
 * - event_type allows filtering and aggregation
 * - event_data stores serialized event details
 *
 * @property id Unique identifier (auto-generated)
 * @property businessId Foreign key to BusinessProfileEntity for multi-tenant safety
 * @property eventType Type of event (InvoiceCreated, PaymentRecorded, etc)
 * @property eventData Serialized event payload (JSON)
 * @property timestamp When the event occurred (milliseconds)
 * @property createdAt When the record was inserted (milliseconds)
 */
@Entity(tableName = "analytics_events")
data class AnalyticsEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "business_id")
    val businessId: Long,

    @ColumnInfo(name = "event_type")
    val eventType: String,

    @ColumnInfo(name = "event_data")
    val eventData: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)

