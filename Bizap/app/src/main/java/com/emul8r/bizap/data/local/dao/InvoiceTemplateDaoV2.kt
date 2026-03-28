package com.emul8r.bizap.data.local.dao

import androidx.room.*
import com.emul8r.bizap.data.local.entities.InvoiceTemplateEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for invoice template operations.
 *
 * Handles:
 * - Creating and saving templates
 * - Listing templates for a business
 * - Updating templates
 * - Deleting templates
 */
@Dao
interface InvoiceTemplateDaoV2 {

    /**
     * Insert a new invoice template.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: InvoiceTemplateEntity): Long

    /**
     * Update an existing template.
     */
    @Update
    suspend fun updateTemplate(template: InvoiceTemplateEntity)

    /**
     * Get template by ID.
     */
    @Query("SELECT * FROM invoice_templates WHERE id = :id AND isActive = 1")
    suspend fun getTemplate(id: Long): InvoiceTemplateEntity?

    /**
     * Get all templates for a business.
     */
    @Query("""
        SELECT * FROM invoice_templates 
        WHERE businessProfileId = :businessId AND isActive = 1 
        ORDER BY updatedAt DESC
    """)
    suspend fun getTemplatesByBusiness(businessId: Long): List<InvoiceTemplateEntity>

    /**
     * Observe templates for a business.
     */
    @Query("""
        SELECT * FROM invoice_templates 
        WHERE businessProfileId = :businessId AND isActive = 1 
        ORDER BY updatedAt DESC
    """)
    fun observeTemplatesByBusiness(businessId: Long): Flow<List<InvoiceTemplateEntity>>

    /**
     * Soft delete a template.
     */
    @Query("UPDATE invoice_templates SET isActive = 0, updatedAt = :now WHERE id = :id")
    suspend fun deleteTemplate(id: Long, now: Long = System.currentTimeMillis())

    /**
     * Get recurring templates.
     */
    @Query("""
        SELECT * FROM invoice_templates 
        WHERE businessProfileId = :businessId AND isRecurring = 1 AND isActive = 1 
        ORDER BY updatedAt DESC
    """)
    suspend fun getRecurringTemplates(businessId: Long): List<InvoiceTemplateEntity>
}

