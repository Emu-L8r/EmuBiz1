package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

sealed class DocumentStatus {
    object Archived : DocumentStatus()
    data class Exported(val exportPath: String) : DocumentStatus()
}

@Entity(
    tableName = "generated_documents",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["invoiceId"],
            childColumns = ["relatedInvoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GeneratedDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val relatedInvoiceId: Long,
    val fileName: String,
    val absolutePath: String,
    val fileType: String, // "Invoice" or "Quote"
    val createdAt: Long = System.currentTimeMillis(),
    val status: DocumentStatus = DocumentStatus.Archived
)
