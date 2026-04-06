package com.emul8r.bizap.domain.model

enum class DocumentStatus {
    DRAFT,
    ARCHIVED,
    SENT,
    PAID
}

data class GeneratedDocument(
    val id: Long = 0,
    val relatedInvoiceId: Long,
    val fileName: String,
    val absolutePath: String,
    val fileType: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: DocumentStatus = DocumentStatus.ARCHIVED,
    val statusUpdatedAt: Long = System.currentTimeMillis()
)
