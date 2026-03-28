package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Invoice template entity for storing reusable invoice layouts.
 *
 * Allows users to save invoice templates with preset line items,
 * terms, and notes to speed up future invoice creation.
 */
@Entity(
    tableName = "invoice_templates",
    foreignKeys = [
        ForeignKey(
            entity = BusinessProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessProfileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class InvoiceTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessProfileId: Long,
    val name: String,
    val description: String? = null,
    val lineItemsJson: String = "[]", // JSON array of line items
    val notes: String? = null,
    val paymentTerms: String? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null, // MONTHLY, QUARTERLY, YEARLY
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

/**
 * Template line item for serialization.
 */
@Serializable
data class TemplateLineItem(
    val description: String,
    val quantity: Double,
    val unitPrice: Long // in cents
)

