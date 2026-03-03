package com.emul8r.bizap.domain.model

data class Document(
    val id: Long = 0,
    val relatedInvoiceId: Long,
    val fileName: String,
    val absolutePath: String,
    val fileType: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: DocumentStatus = DocumentStatus.ARCHIVED
)

enum class DocumentStatus {
    DRAFT,
    ARCHIVED,
    SENT,
    PAID
}
