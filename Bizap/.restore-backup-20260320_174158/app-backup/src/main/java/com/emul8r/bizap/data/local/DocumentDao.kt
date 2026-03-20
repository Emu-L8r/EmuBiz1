package com.emul8r.bizap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.emul8r.bizap.data.local.entities.DocumentStatus
import com.emul8r.bizap.data.local.entities.GeneratedDocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Now correctly replaces metadata for regenerated docs
    suspend fun insertDocument(document: GeneratedDocumentEntity)

    @Query("SELECT * FROM generated_documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<GeneratedDocumentEntity>>

    @Query("SELECT * FROM generated_documents WHERE relatedInvoiceId = :invoiceId")
    fun getDocumentsByInvoiceId(invoiceId: Long): Flow<List<GeneratedDocumentEntity>>

    @Query("SELECT * FROM generated_documents WHERE relatedInvoiceId = :invoiceId AND fileType = :fileType LIMIT 1")
    suspend fun getDocumentByInvoiceAndType(invoiceId: Long, fileType: String): GeneratedDocumentEntity?

    @Query("DELETE FROM generated_documents WHERE id = :documentId")
    suspend fun deleteDocument(documentId: Long)

    @Query("UPDATE generated_documents SET status = :status WHERE id = :id")
    suspend fun updateDocumentStatus(id: Long, status: DocumentStatus)
}
