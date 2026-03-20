package com.emul8r.bizap.data.repository.analytics

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Event bus that signals when analytics caches should be invalidated.
 *
 * Rather than relying on implicit Room Flow reactivity alone, this bus lets
 * write-side operations (create invoice, record payment, change status) emit
 * explicit events so that analytics repositories can react immediately.
 *
 * Consumers should call [observeEvents] and trigger a data refresh whenever
 * a relevant [AnalyticsEvent] is received.
 *
 * A replay buffer of 1 is used so that late subscribers receive the most
 * recent event and don't miss an invalidation that occurred before subscription.
 */
@Singleton
class AnalyticsEventBus @Inject constructor() {

    /** Sealed hierarchy of events that affect analytics state. */
    sealed class AnalyticsEvent {
        /** A new invoice was created for [businessId]. */
        data class InvoiceCreated(val businessId: Long, val invoiceId: Long) : AnalyticsEvent()

        /** An invoice status changed (e.g. DRAFT → SENT, SENT → PAID). */
        data class InvoiceStatusChanged(
            val businessId: Long,
            val invoiceId: Long,
            val newStatus: String
        ) : AnalyticsEvent()

        /** A payment was recorded against [invoiceId]. */
        data class PaymentRecorded(
            val businessId: Long,
            val invoiceId: Long,
            val amount: Long
        ) : AnalyticsEvent()

        /** An invoice was deleted from [businessId]. */
        data class InvoiceDeleted(val businessId: Long, val invoiceId: Long) : AnalyticsEvent()
    }

    private val _events = MutableSharedFlow<AnalyticsEvent>(replay = 1, extraBufferCapacity = 64)

    /** Observe all analytics-invalidating events. */
    val events: SharedFlow<AnalyticsEvent> = _events.asSharedFlow()

    /** Emit an [event] to all current observers. */
    suspend fun emit(event: AnalyticsEvent) {
        Timber.d("AnalyticsEventBus: emit $event")
        _events.emit(event)
    }

    /** Emit an [event] without suspending (best-effort; drops if buffer full). */
    fun tryEmit(event: AnalyticsEvent): Boolean {
        Timber.d("AnalyticsEventBus: tryEmit $event")
        return _events.tryEmit(event)
    }
}
