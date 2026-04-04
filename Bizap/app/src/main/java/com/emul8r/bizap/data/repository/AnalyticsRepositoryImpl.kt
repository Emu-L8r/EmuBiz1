package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.AnalyticsEventDao
import com.emul8r.bizap.data.local.entities.AnalyticsEventEntity
import com.emul8r.bizap.domain.analytics.AnalyticsRepository
import com.emul8r.bizap.domain.analytics.InvoiceAnalyticsEvent
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data layer implementation of AnalyticsRepository using Room database.
 *
 * Persists analytics events to local database for:
 * - Trend calculation
 * - Report generation
 * - User behavior analysis
 * - Audit trails
 *
 * **Serialization Strategy:**
 * Events are stored as JSON with a `"type"` discriminator field so they can be
 * deserialised back to the correct `InvoiceAnalyticsEvent` subclass.
 *
 * Example stored JSON:
 * ```json
 * { "type": "InvoiceCreated", "businessId": 1, "invoiceId": 42, "amount": 5000, "timestamp": 1700000000 }
 * ```
 *
 * **Thread Safety:**
 * All suspend functions handle their own coroutine context.
 * Flow observables are safe for multi-threaded access.
 *
 * **Error Handling:**
 * Database errors are logged and propagated via Result.failure().
 * No data is silently lost.
 *
 * @param analyticsEventDao Database access for events
 */
@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val analyticsEventDao: AnalyticsEventDao
) : AnalyticsRepository {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(InvoiceAnalyticsEvent::class.java, InvoiceAnalyticsEventDeserializer())
        .create()

    private val dispatcher = Dispatchers.IO

    /**
     * Log an analytics event to persistent storage.
     *
     * Serializes the event to JSON with a `type` discriminator field and persists to database.
     * Failures are logged but do not throw exceptions.
     *
     * @param event The event to log
     * @return Result indicating success or failure
     */
    override suspend fun logEvent(event: InvoiceAnalyticsEvent): Result<Unit> =
        withContext(dispatcher) {
            runCatching {
                // Include a "type" discriminator so we can deserialise polymorphically later
                val json = gson.toJsonTree(event).asJsonObject.also {
                    it.addProperty("type", event::class.simpleName)
                }.toString()

                val eventEntity = AnalyticsEventEntity(
                    businessId = event.businessId,
                    eventType = event::class.simpleName ?: "Unknown",
                    eventData = json,
                    timestamp = event.timestamp,
                    createdAt = System.currentTimeMillis()
                )
                analyticsEventDao.insertEvent(eventEntity)
                Timber.d("✅ Event logged: ${event::class.simpleName} for business ${event.businessId}")
            }.onFailure { e ->
                Timber.e(e, "❌ Failed to log event for business ${event.businessId}")
            }
        }

    /**
     * Get count of events by type within a time window.
     *
     * @param businessId Business to query
     * @param eventType Type of event to count
     * @param sinceMs Timestamp threshold
     * @return Count of matching events
     */
    override suspend fun getEventCount(
        businessId: Long,
        eventType: String,
        sinceMs: Long
    ): Result<Int> = withContext(dispatcher) {
        runCatching {
            val count = analyticsEventDao.getEventCountByType(businessId, eventType, sinceMs)
            Timber.d("📊 Event count ($eventType): $count")
            count
        }.onFailure { e ->
            Timber.e(e, "❌ Failed to get event count for business $businessId")
        }
    }

    /**
     * Get sum of payment amounts.
     *
     * Queries PaymentRecorded events and sums their amounts.
     *
     * @param businessId Business to query
     * @param sinceMs Timestamp threshold
     * @return Total payment amount in cents
     */
    override suspend fun getPaymentAmount(
        businessId: Long,
        sinceMs: Long
    ): Result<Long> = withContext(dispatcher) {
        runCatching {
            val total = analyticsEventDao.getSumPaymentAmount(businessId, sinceMs)
            Timber.d("💰 Total payments: $total cents")
            total
        }.onFailure { e ->
            Timber.e(e, "❌ Failed to get payment amount for business $businessId")
        }
    }

    /**
     * Observe recent events reactively.
     *
     * Returns a Flow that emits updated lists as events are added.
     * Automatically filters by business for multi-tenant safety.
     *
     * @param businessId Business to observe
     * @param limitMinutes Only events from last N minutes (converts to timestamp)
     * @return Flow of event lists
     */
    override fun observeRecentEvents(
        businessId: Long,
        limitMinutes: Int
    ): Flow<List<InvoiceAnalyticsEvent>> {
        val limitMs = System.currentTimeMillis() - (limitMinutes * 60 * 1000)

        return analyticsEventDao.observeRecentEvents(businessId)
            .map { entities ->
                entities
                    .filter { it.timestamp >= limitMs }
                    .mapNotNull { deserializeEvent(it.eventData) }
            }
    }

    /**
     * Observe events of a specific type.
     *
     * Filters to specific event type (e.g., PaymentRecorded).
     * Returns Flow that updates as new events arrive.
     *
     * @param businessId Business to observe
     * @param eventType Type of event to observe
     * @return Flow of matching events
     */
    override fun observeEventsByType(
        businessId: Long,
        eventType: String
    ): Flow<List<InvoiceAnalyticsEvent>> {
        return analyticsEventDao.observeEventsByType(businessId, eventType)
            .map { entities ->
                entities.mapNotNull { deserializeEvent(it.eventData) }
            }
    }

    /**
     * Deserialize a JSON event string back to InvoiceAnalyticsEvent.
     *
     * Uses the `type` discriminator field stored at serialisation time to route
     * to the correct concrete subclass.
     *
     * @param json Serialized event JSON containing a `"type"` field
     * @return Event object or null if parsing fails
     */
    internal fun deserializeEvent(json: String): InvoiceAnalyticsEvent? {
        return try {
            gson.fromJson(json, InvoiceAnalyticsEvent::class.java)
        } catch (e: Exception) {
            Timber.w(e, "Failed to deserialize analytics event from JSON: $json")
            null
        }
    }

    /**
     * Polymorphic deserializer for [InvoiceAnalyticsEvent] sealed class.
     *
     * Reads the `"type"` field to determine which concrete subclass to instantiate.
     */
    private class InvoiceAnalyticsEventDeserializer : JsonDeserializer<InvoiceAnalyticsEvent> {
        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): InvoiceAnalyticsEvent? {
            val obj = json.asJsonObject
            val type = obj.get("type")?.asString ?: return null

            // Type strings match the simple class names of InvoiceAnalyticsEvent sealed subclasses.
            // If a subclass is added or renamed, add the corresponding entry here and update logEvent().
            return when (type) {
                "InvoiceCreated"  -> context.deserialize(obj, InvoiceAnalyticsEvent.InvoiceCreated::class.java)
                "InvoiceViewed"   -> context.deserialize(obj, InvoiceAnalyticsEvent.InvoiceViewed::class.java)
                "StatusChanged"   -> context.deserialize(obj, InvoiceAnalyticsEvent.StatusChanged::class.java)
                "PaymentRecorded" -> context.deserialize(obj, InvoiceAnalyticsEvent.PaymentRecorded::class.java)
                else -> {
                    Timber.w("InvoiceAnalyticsEventDeserializer: unknown event type '$type'")
                    null
                }
            }
        }
    }
}

/**
 * Hilt Module for AnalyticsRepository dependency injection.
 *
 * Binds the implementation to the interface for automatic injection.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        impl: AnalyticsRepositoryImpl
    ): com.emul8r.bizap.domain.analytics.AnalyticsRepository
}
