package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class DocumentStatus {
    DRAFT,
    ARCHIVED,
    SENT,
    PAID
}

@Entity(
    tableName = "generated_documents",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["relatedInvoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("relatedInvoiceId"),
        Index(value = ["relatedInvoiceId", "fileType"], unique = true)
    ]
)
data class GeneratedDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val relatedInvoiceId: Long,
    val fileName: String,
    val absolutePath: String,
    val fileType: String, // "Invoice" or "Quote"
    val createdAt: Long = System.currentTimeMillis(),
    val status: DocumentStatus = DocumentStatus.ARCHIVED,
    val statusUpdatedAt: Long = System.currentTimeMillis()
)
