package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "invoiceTemplates",
    indices = [
        Index(value = ["businessProfileId"]),
        Index(value = ["businessProfileId", "isDefault"]),
        Index(value = ["businessProfileId", "isActive"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = BusinessProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessProfileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class InvoiceTemplate(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessProfileId: Long,
    val name: String,                    // "Professional", "Minimal", "Custom"
    val designType: String,              // PROFESSIONAL, MINIMAL, BRANDED
    val logoFileName: String? = null,    // "template-123.png" (not full path)
    val primaryColor: String = "#FF5722",
    val secondaryColor: String = "#FFF9C4",
    val fontFamily: String = "SANS_SERIF", // SANS_SERIF, SERIF
    val companyName: String = "",
    val companyAddress: String = "",
    val companyPhone: String = "",
    val companyEmail: String = "",
    val taxId: String? = null,
    val bankDetails: String? = null,
    val hideLineItems: Boolean = false,  // Hide item table if true
    val hidePaymentTerms: Boolean = false,
    val isDefault: Boolean = false,
    val isActive: Boolean = true,        // Soft-delete flag
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)


