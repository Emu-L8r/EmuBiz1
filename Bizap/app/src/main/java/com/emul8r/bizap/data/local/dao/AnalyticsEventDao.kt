package com.emul8r.bizap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.emul8r.bizap.data.local.entities.AnalyticsEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for analytics events.
 *
 * Provides database operations for:
 * - Inserting new analytics events
 * - Querying events by type, date range, or business
 * - Observing event streams for real-time updates
 * - Calculating aggregate metrics
 *
 * **Thread Safety:**
 * All suspend functions are safe to call from any coroutine context.
 * Flow observables are lifecycle-aware and thread-safe.
 *
 * **Performance:**
 * Indexes are created on (business_id, event_type, timestamp) for optimal queries.
 *
 * @see AnalyticsEventEntity
 */
@Dao
interface AnalyticsEventDao {

    /**
     * Insert a new analytics event.
     *
     * If a duplicate event ID exists, it will be replaced.
     *
     * @param event The event to insert
     * @return Row ID of the inserted event
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: AnalyticsEventEntity): Long

    /**
     * Insert multiple events in a transaction.
     *
     * All events are inserted atomically - either all succeed or all fail.
     *
     * @param events List of events to insert
     * @return List of row IDs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<AnalyticsEventEntity>): List<Long>

    /**
     * Get count of events by type within a time window.
     *
     * Example: Count invoices created this month
     *
     * @param businessId Business to query
     * @param eventType Type of event to count (e.g., "InvoiceCreated")
     * @param sinceMs Only count events after this timestamp
     * @return Count of matching events
     */
    @Query("""
        SELECT COUNT(*) FROM analytics_events 
        WHERE business_id = :businessId 
        AND event_type = :eventType 
        AND timestamp >= :sinceMs
    """)
    suspend fun getEventCountByType(
        businessId: Long,
        eventType: String,
        sinceMs: Long
    ): Int

    /**
     * Get sum of amounts from payment events.
     *
     * Queries event_data to calculate total payments in a period.
     * Used for revenue metrics.
     *
     * @param businessId Business to query
     * @param sinceMs Only sum events after this timestamp
     * @return Total amount in cents
     */
    @Query("""
        SELECT COALESCE(SUM(
            CAST(JSON_EXTRACT(event_data, '$.amount') AS INTEGER)
        ), 0) FROM analytics_events 
        WHERE business_id = :businessId 
        AND event_type = 'PaymentRecorded'
        AND timestamp >= :sinceMs
    """)
    suspend fun getSumPaymentAmount(
        businessId: Long,
        sinceMs: Long
    ): Long

    /**
     * Observe recent events reactively.
     *
     * Returns a Flow that emits events as they're inserted.
     * Updates in real-time with new events.
     *
     * @param businessId Business to observe
     * @param limit Maximum number of events to return
     * @return Flow of event lists
     */
    @Query("""
        SELECT * FROM analytics_events 
        WHERE business_id = :businessId 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    fun observeRecentEvents(businessId: Long, limit: Int = 1000): Flow<List<AnalyticsEventEntity>>

    /**
     * Observe events by type reactively.
     *
     * Filters events to specific type (e.g., "PaymentRecorded")
     * and emits updates as new events are inserted.
     *
     * @param businessId Business to observe
     * @param eventType Type of event to observe
     * @return Flow of matching events
     */
    @Query("""
        SELECT * FROM analytics_events 
        WHERE business_id = :businessId 
        AND event_type = :eventType
        ORDER BY timestamp DESC
    """)
    fun observeEventsByType(businessId: Long, eventType: String): Flow<List<AnalyticsEventEntity>>

    /**
     * Get events within a date range.
     *
     * Useful for reports and trend analysis.
     *
     * @param businessId Business to query
     * @param fromMs Start of range
     * @param toMs End of range
     * @return List of events in the range
     */
    @Query("""
        SELECT * FROM analytics_events 
        WHERE business_id = :businessId 
        AND timestamp >= :fromMs 
        AND timestamp <= :toMs
        ORDER BY timestamp DESC
    """)
    suspend fun getEventsByDateRange(
        businessId: Long,
        fromMs: Long,
        toMs: Long
    ): List<AnalyticsEventEntity>

    /**
     * Delete old events (data cleanup).
     *
     * Removes events older than the specified timestamp.
     * Useful for managing database size.
     *
     * @param beforeMs Delete events older than this timestamp
     * @return Number of events deleted
     */
    @Query("""
        DELETE FROM analytics_events 
        WHERE timestamp < :beforeMs
    """)
    suspend fun deleteOldEvents(beforeMs: Long): Int

    /**
     * Delete all events for a business.
     *
     * Use with caution - this removes all historical data.
     *
     * @param businessId Business whose events to delete
     * @return Number of events deleted
     */
    @Query("DELETE FROM analytics_events WHERE business_id = :businessId")
    suspend fun deleteAllEventsByBusiness(businessId: Long): Int
}

