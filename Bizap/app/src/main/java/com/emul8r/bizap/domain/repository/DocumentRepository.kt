package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.DocumentStatus
import com.emul8r.bizap.domain.model.GeneratedDocument
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getAllDocuments(): Flow<List<GeneratedDocument>>
    fun getDocumentsByInvoiceId(invoiceId: Long): Flow<List<GeneratedDocument>>
    suspend fun getDocumentByInvoiceAndType(invoiceId: Long, fileType: String): GeneratedDocument?
    suspend fun insertDocument(document: GeneratedDocument): Result<Unit>
    suspend fun deleteDocument(documentId: Long): Result<Unit>
    suspend fun updateDocumentStatus(id: Long, status: DocumentStatus): Result<Unit>
}
