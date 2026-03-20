package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.DocumentDao
import com.emul8r.bizap.data.local.entities.DocumentStatus
import com.emul8r.bizap.data.local.entities.GeneratedDocumentEntity
import com.emul8r.bizap.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val dao: DocumentDao
) : DocumentRepository {
    override fun getAllDocuments(): Flow<List<GeneratedDocumentEntity>> = dao.getAllDocuments()
    override fun getDocumentsByInvoiceId(invoiceId: Long): Flow<List<GeneratedDocumentEntity>> = dao.getDocumentsByInvoiceId(invoiceId)
    override suspend fun getDocumentByInvoiceAndType(invoiceId: Long, fileType: String): GeneratedDocumentEntity? = 
        dao.getDocumentByInvoiceAndType(invoiceId, fileType)
    override suspend fun insertDocument(document: GeneratedDocumentEntity): Result<Unit> = runCatching {
        dao.insertDocument(document)
    }
    override suspend fun deleteDocument(documentId: Long): Result<Unit> = runCatching {
        dao.deleteDocument(documentId)
    }
    override suspend fun updateDocumentStatus(id: Long, status: DocumentStatus): Result<Unit> = runCatching {
        dao.updateDocumentStatus(id, status)
    }
}
