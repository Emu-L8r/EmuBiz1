package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "invoiceCustomFields",
    indices = [
        Index(value = ["templateId"]),
        Index(value = ["templateId", "displayOrder"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = InvoiceTemplate::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class InvoiceCustomField(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val templateId: String,
    val label: String,                   // "PO Number", "Project Code"
    val fieldType: String,               // TEXT, NUMBER, DATE
    val isRequired: Boolean = false,
    val displayOrder: Int,               // 1, 2, 3, etc.
    val isActive: Boolean = true         // Soft-delete
)

// Enum for field type validation
enum class CustomFieldType {
    TEXT, NUMBER, DATE
}

