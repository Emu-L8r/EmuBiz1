package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.Fts4

/**
 * Full-Text Search (FTS4) virtual table for Invoice search.
 *
 * Enables fast, natural-language search across:
 * - Invoice numbers
 * - Customer names
 * - Line item descriptions
 *
 * Usage:
 * ```kotlin
 * @Query("""
 *     SELECT * FROM InvoiceFTS
 *     WHERE InvoiceFTS MATCH :query
 * """)
 * suspend fun searchInvoices(query: String): List<InvoiceFTS>
 * ```
 *
 * Query syntax (SQLite FTS4):
 * - "invoice 001" — phrase match
 * - "john OR acme" — OR search
 * - "consulting -draft" — exclude words
 *
 * Performance: Indexed searches O(log n) vs table scans O(n)
 *
 * Note: FTS tables are virtual and read-only. Maintain via triggers
 * in Room migrations when Invoice table is updated.
 */
@Entity(tableName = "InvoiceFTS")
@Fts4(contentEntity = InvoiceEntity::class)
data class InvoiceFTS(
    val invoiceNumber: String,
    val customerName: String,
    val description: String,
    val notes: String?
)


