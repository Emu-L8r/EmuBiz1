package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.data.local.entities.DocumentStatus
import com.emul8r.bizap.data.local.entities.GeneratedDocumentEntity
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getAllDocuments(): Flow<List<GeneratedDocumentEntity>>
    fun getDocumentsByInvoiceId(invoiceId: Long): Flow<List<GeneratedDocumentEntity>>
    suspend fun getDocumentByInvoiceAndType(invoiceId: Long, fileType: String): GeneratedDocumentEntity?
    suspend fun insertDocument(document: GeneratedDocumentEntity)
    suspend fun deleteDocument(documentId: Long)
    suspend fun updateDocumentStatus(id: Long, status: DocumentStatus)
}

