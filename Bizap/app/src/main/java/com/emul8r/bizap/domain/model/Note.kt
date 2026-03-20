package com.emul8r.bizap.domain.model

/**
 * Note domain model - can be linked to either a Customer or Invoice.
 * Only one of customerId or invoiceId should be set.
 * 
 * This is a pure domain model without infrastructure concerns.
 * The data layer will map this to/from the Room entity.
 */
data class Note(
    val id: Long = 0,
    val businessProfileId: Long,
    val customerId: Long? = null,  // If note is for a customer
    val invoiceId: Long? = null,   // If note is for an invoice
    val title: String,
    val content: String,
    val isCurrent: Boolean = true, // Marks whether this note is still active/current
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true   // Soft delete flag
)

