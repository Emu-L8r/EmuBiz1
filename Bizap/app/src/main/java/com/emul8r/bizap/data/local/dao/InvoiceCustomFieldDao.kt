package com.emul8r.bizap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.emul8r.bizap.data.local.entities.InvoiceCustomField

@Dao
interface InvoiceCustomFieldDao {
    @Insert
    suspend fun insertField(field: InvoiceCustomField)

    @Update
    suspend fun updateField(field: InvoiceCustomField)

    @Query("UPDATE invoiceCustomFields SET isActive = 0 WHERE id = :fieldId")
    suspend fun softDeleteField(fieldId: String)

    @Query("SELECT * FROM invoiceCustomFields WHERE templateId = :templateId AND isActive = 1 ORDER BY displayOrder ASC")
    suspend fun getFieldsByTemplate(templateId: String): List<InvoiceCustomField>

    @Query("DELETE FROM invoiceCustomFields WHERE templateId = :templateId")
    suspend fun deleteFieldsByTemplate(templateId: String)

    @Query("SELECT COUNT(*) FROM invoiceCustomFields WHERE templateId = :templateId AND isActive = 1")
    suspend fun getFieldCount(templateId: String): Int

    @Query("SELECT * FROM invoiceCustomFields WHERE templateId = :templateId ORDER BY displayOrder ASC")
    suspend fun getAllFieldsByTemplate(templateId: String): List<InvoiceCustomField>
}

