package com.emul8r.bizap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.emul8r.bizap.data.local.entities.InvoiceTemplate

@Dao
interface InvoiceTemplateDao {
    @Insert
    suspend fun insertTemplate(template: InvoiceTemplate)

    @Update
    suspend fun updateTemplate(template: InvoiceTemplate)

    @Query("UPDATE invoiceTemplates SET isActive = 0 WHERE id = :templateId")
    suspend fun softDeleteTemplate(templateId: String)

    @Query("SELECT * FROM invoiceTemplates WHERE businessProfileId = :businessProfileId AND isActive = 1 ORDER BY isDefault DESC, createdAt DESC")
    suspend fun getActiveTemplatesByBusiness(businessProfileId: Long): List<InvoiceTemplate>

    @Query("SELECT * FROM invoiceTemplates WHERE id = :templateId AND isActive = 1")
    suspend fun getTemplate(templateId: String): InvoiceTemplate?

    @Query("SELECT * FROM invoiceTemplates WHERE businessProfileId = :businessProfileId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultTemplate(businessProfileId: Long): InvoiceTemplate?

    @Query("UPDATE invoiceTemplates SET isDefault = 0 WHERE businessProfileId = :businessProfileId")
    suspend fun clearDefaults(businessProfileId: Long)

    @Query("SELECT COUNT(*) FROM invoiceTemplates WHERE businessProfileId = :businessProfileId AND isActive = 1")
    suspend fun getActiveTemplateCount(businessProfileId: Long): Int

    @Query("SELECT * FROM invoiceTemplates WHERE businessProfileId = :businessProfileId AND isActive = 1")
    suspend fun getTemplatesByBusiness(businessProfileId: Long): List<InvoiceTemplate>
}


