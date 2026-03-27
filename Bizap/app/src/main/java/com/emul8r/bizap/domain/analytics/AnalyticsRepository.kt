package com.emul8r.bizap.domain.analytics

import kotlinx.coroutines.flow.Flow

/**
 * Domain repository for analytics event logging and querying.
 *
 * This interface abstracts the data layer's analytics functionality,
 * allowing domain layer and UI layer to work with analytics without
 * depending on implementation details (Room, Firebase, etc).
 *
 * **Responsibilities:**
 * - Log analytics events to persistent storage
 * - Query event history for reporting
 * - Calculate aggregate metrics from events
 * - Provide reactive streams for real-time dashboards
 *
 * **Multi-Tenant Safety:**
 * All methods require businessId parameter to ensure data isolation.
 * Cross-business data access is prevented at repository level.
 */
interface AnalyticsRepository {
    /**
     * Log an analytics event to persistent storage.
     *
     * Events are stored for later analysis, trend calculation, and reporting.
     * Logging is asynchronous and does not block the calling thread.
     *
     * @param event The analytics event to log
     * @return Result indicating success or failure of logging
     *
     * @throws IllegalArgumentException if event data is invalid
     *
     * Example:
     * ```kotlin
     * analyticsRepository.logEvent(
     *     InvoiceAnalyticsEvent.InvoiceCreated(
     *         businessId = 123L,
     *         invoiceId = 456L,
     *         amount = 5000L  // cents
     *     )
     * )
     * ```
     */
    suspend fun logEvent(event: InvoiceAnalyticsEvent): Result<Unit>

    /**
     * Get count of events by type within a time window.
     *
     * Useful for dashboard metrics like "invoices created this month"
     * or "payments recorded this week".
     *
     * @param businessId Business to query (multi-tenant isolation)
     * @param eventType Fully qualified event class name (e.g., "InvoiceCreated")
     * @param sinceMs Timestamp in milliseconds - only events after this time are counted
     * @return Count of matching events
     *
     * Example:
     * ```kotlin
     * val monthStart = Calendar.getInstance().apply {
     *     set(Calendar.DAY_OF_MONTH, 1)
     * }.timeInMillis
     *
     * val created = analyticsRepository.getEventCount(
     *     businessId = 123L,
     *     eventType = "InvoiceCreated",
     *     sinceMs = monthStart
     * )
     * ```
     */
    suspend fun getEventCount(
        businessId: Long,
        eventType: String,
        sinceMs: Long
    ): Result<Int>

    /**
     * Get sum of amounts from payment events.
     *
     * Calculates total amount collected in a time period,
     * useful for revenue metrics and collection tracking.
     *
     * @param businessId Business to query (multi-tenant isolation)
     * @param sinceMs Timestamp in milliseconds - only events after this time
     * @return Total amount in cents from all PaymentRecorded events
     *
     * Example:
     * ```kotlin
     * val monthStart = Calendar.getInstance().apply {
     *     set(Calendar.DAY_OF_MONTH, 1)
     * }.timeInMillis
     *
     * val collected = analyticsRepository.getPaymentAmount(
     *     businessId = 123L,
     *     sinceMs = monthStart
     * )
     * ```
     */
    suspend fun getPaymentAmount(
        businessId: Long,
        sinceMs: Long
    ): Result<Long>

    /**
     * Observe recent analytics events reactively.
     *
     * Returns a Flow that emits events logged within the specified time window.
     * Useful for real-time dashboards that need to react to user actions.
     *
     * @param businessId Business to observe (multi-tenant isolation)
     * @param limitMinutes Only observe events from the last N minutes
     * @return Flow that emits lists of events. Updates when new events are logged.
     *
     * Example:
     * ```kotlin
     * analyticsRepository
     *     .observeRecentEvents(businessId = 123L, limitMinutes = 60)
     *     .collect { events ->
     *         updateDashboard(events)
     *     }
     * ```
     */
    fun observeRecentEvents(
        businessId: Long,
        limitMinutes: Int = 60
    ): Flow<List<InvoiceAnalyticsEvent>>

    /**
     * Observe all events of a specific type reactively.
     *
     * Provides a reactive stream of all events matching a type,
     * useful for specialized analytics dashboards and reports.
     *
     * @param businessId Business to observe (multi-tenant isolation)
     * @param eventType Fully qualified event class name
     * @return Flow that emits events of the specified type
     *
     * Example:
     * ```kotlin
     * analyticsRepository
     *     .observeEventsByType(businessId = 123L, eventType = "PaymentRecorded")
     *     .collect { events ->
     *         updatePaymentChart(events)
     *     }
     * ```
     */
    fun observeEventsByType(
        businessId: Long,
        eventType: String
    ): Flow<List<InvoiceAnalyticsEvent>>
}

