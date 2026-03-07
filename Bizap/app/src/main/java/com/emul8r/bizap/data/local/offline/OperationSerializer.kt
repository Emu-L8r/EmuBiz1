package com.emul8r.bizap.data.local.offline

import com.emul8r.bizap.domain.model.Invoice
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

/**
 * Handles serialization and deserialization of invoice and payment data for offline queue.
 */
object OperationSerializer {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    fun serializeInvoice(invoice: Invoice): String {
        return try {
            val serialized = json.encodeToString(invoice)
            Timber.d("📦 Serialized invoice: ${invoice.id}")
            serialized
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to serialize invoice")
            throw e
        }
    }
    
    fun deserializeInvoice(serialized: String): Invoice {
        return try {
            json.decodeFromString(serialized)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to deserialize invoice")
            throw e
        }
    }
    
    fun serializePayment(invoiceId: Long, amountPaid: Long): String {
        return try {
            val data = mapOf(
                "invoiceId" to invoiceId,
                "amountPaid" to amountPaid,
                "timestamp" to System.currentTimeMillis()
            )
            json.encodeToString(data)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to serialize payment")
            throw e
        }
    }
    
    fun deserializePayment(serialized: String): Pair<Long, Long> {
        return try {
            val data: Map<String, Long> = json.decodeFromString(serialized)
            Pair(data["invoiceId"] ?: 0L, data["amountPaid"] ?: 0L)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to deserialize payment")
            throw e
        }
    }
}
