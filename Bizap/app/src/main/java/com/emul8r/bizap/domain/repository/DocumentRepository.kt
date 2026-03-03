package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.Document
import com.emul8r.bizap.domain.model.DocumentStatus
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getAllDocuments(): Flow<List<Document>>
    fun getDocumentsByInvoiceId(invoiceId: Long): Flow<List<Document>>
    suspend fun getDocumentByInvoiceAndType(invoiceId: Long, fileType: String): Document?
    suspend fun insertDocument(document: Document)
    suspend fun deleteDocument(documentId: Long)
    suspend fun updateDocumentStatus(id: Long, status: DocumentStatus)
}
