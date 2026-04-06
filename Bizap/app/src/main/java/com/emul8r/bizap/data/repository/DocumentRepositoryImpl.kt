package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.DocumentDao
import com.emul8r.bizap.data.local.entities.DocumentStatus as EntityDocumentStatus
import com.emul8r.bizap.data.local.entities.GeneratedDocumentEntity
import com.emul8r.bizap.domain.model.DocumentStatus
import com.emul8r.bizap.domain.model.GeneratedDocument
import com.emul8r.bizap.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val dao: DocumentDao
) : DocumentRepository {

    private fun GeneratedDocumentEntity.toDomain(): GeneratedDocument = GeneratedDocument(
        id = id,
        relatedInvoiceId = relatedInvoiceId,
        fileName = fileName,
        absolutePath = absolutePath,
        fileType = fileType,
        createdAt = createdAt,
        status = DocumentStatus.valueOf(status.name),
        statusUpdatedAt = statusUpdatedAt
    )

    private fun GeneratedDocument.toEntity(): GeneratedDocumentEntity = GeneratedDocumentEntity(
        id = id,
        relatedInvoiceId = relatedInvoiceId,
        fileName = fileName,
        absolutePath = absolutePath,
        fileType = fileType,
        createdAt = createdAt,
        status = EntityDocumentStatus.valueOf(status.name),
        statusUpdatedAt = statusUpdatedAt
    )

    override fun getAllDocuments(): Flow<List<GeneratedDocument>> =
        dao.getAllDocuments().map { list -> list.map { it.toDomain() } }

    override fun getDocumentsByInvoiceId(invoiceId: Long): Flow<List<GeneratedDocument>> =
        dao.getDocumentsByInvoiceId(invoiceId).map { list -> list.map { it.toDomain() } }

    override suspend fun getDocumentByInvoiceAndType(invoiceId: Long, fileType: String): GeneratedDocument? =
        dao.getDocumentByInvoiceAndType(invoiceId, fileType)?.toDomain()

    override suspend fun insertDocument(document: GeneratedDocument): Result<Unit> = runCatching {
        dao.insertDocument(document.toEntity())
    }

    override suspend fun deleteDocument(documentId: Long): Result<Unit> = runCatching {
        dao.deleteDocument(documentId)
    }

    override suspend fun updateDocumentStatus(id: Long, status: DocumentStatus): Result<Unit> = runCatching {
        dao.updateDocumentStatus(id, EntityDocumentStatus.valueOf(status.name))
    }
}
