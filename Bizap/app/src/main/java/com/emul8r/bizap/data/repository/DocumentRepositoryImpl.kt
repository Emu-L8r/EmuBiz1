package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.DocumentDao
import com.emul8r.bizap.data.local.entities.GeneratedDocumentEntity
import com.emul8r.bizap.data.local.entities.DocumentStatus as EntityDocumentStatus
import com.emul8r.bizap.domain.model.Document
import com.emul8r.bizap.domain.model.DocumentStatus
import com.emul8r.bizap.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val dao: DocumentDao
) : DocumentRepository {

    override fun getAllDocuments(): Flow<List<Document>> =
        dao.getAllDocuments().map { list -> list.map { it.toDomain() } }

    override fun getDocumentsByInvoiceId(invoiceId: Long): Flow<List<Document>> =
        dao.getDocumentsByInvoiceId(invoiceId).map { list -> list.map { it.toDomain() } }

    override suspend fun getDocumentByInvoiceAndType(invoiceId: Long, fileType: String): Document? =
        dao.getDocumentByInvoiceAndType(invoiceId, fileType)?.toDomain()

    override suspend fun insertDocument(document: Document) =
        dao.insertDocument(document.toEntity())

    override suspend fun deleteDocument(documentId: Long) =
        dao.deleteDocument(documentId)

    override suspend fun updateDocumentStatus(id: Long, status: DocumentStatus) =
        dao.updateDocumentStatus(id, status.toEntity())

    // --- Mappers ---

    private fun GeneratedDocumentEntity.toDomain() = Document(
        id = id,
        relatedInvoiceId = relatedInvoiceId,
        fileName = fileName,
        absolutePath = absolutePath,
        fileType = fileType,
        createdAt = createdAt,
        status = status.toDomain()
    )

    private fun Document.toEntity() = GeneratedDocumentEntity(
        id = id,
        relatedInvoiceId = relatedInvoiceId,
        fileName = fileName,
        absolutePath = absolutePath,
        fileType = fileType,
        createdAt = createdAt,
        status = status.toEntity()
    )

    private fun EntityDocumentStatus.toDomain(): DocumentStatus = when (this) {
        EntityDocumentStatus.DRAFT -> DocumentStatus.DRAFT
        EntityDocumentStatus.ARCHIVED -> DocumentStatus.ARCHIVED
        EntityDocumentStatus.SENT -> DocumentStatus.SENT
        EntityDocumentStatus.PAID -> DocumentStatus.PAID
    }

    private fun DocumentStatus.toEntity(): EntityDocumentStatus = when (this) {
        DocumentStatus.DRAFT -> EntityDocumentStatus.DRAFT
        DocumentStatus.ARCHIVED -> EntityDocumentStatus.ARCHIVED
        DocumentStatus.SENT -> EntityDocumentStatus.SENT
        DocumentStatus.PAID -> EntityDocumentStatus.PAID
    }
}
