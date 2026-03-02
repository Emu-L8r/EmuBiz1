package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.InvoiceTemplateDao
import com.emul8r.bizap.data.local.dao.InvoiceCustomFieldDao
import com.emul8r.bizap.data.local.entities.InvoiceTemplate
import com.emul8r.bizap.data.local.entities.InvoiceCustomField
import javax.inject.Inject

/**
 * Repository for Invoice Template operations.
 */
class InvoiceTemplateRepository @Inject constructor(
    private val templateDao: InvoiceTemplateDao,
    private val fieldDao: InvoiceCustomFieldDao
) {

    companion object {
        private const val TAG = "InvoiceTemplateRepository"
    }

    suspend fun getAllTemplates(businessProfileId: Long): Result<List<InvoiceTemplate>> {
        return try {
            val templates = templateDao.getActiveTemplatesByBusiness(businessProfileId)
            Result.success(templates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTemplate(templateId: String): Result<InvoiceTemplate?> {
        return try {
            val template = templateDao.getTemplate(templateId)
            Result.success(template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTemplateWithFields(templateId: String): Result<Pair<InvoiceTemplate?, List<InvoiceCustomField>>> {
        return try {
            val template = templateDao.getTemplate(templateId)
            val fields = if (template != null) fieldDao.getFieldsByTemplate(templateId) else emptyList()
            Result.success(Pair(template, fields))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTemplate(template: InvoiceTemplate): Result<String> {
        return try {
            // Business logic: check limits if necessary
            val count = templateDao.getActiveTemplateCount(template.businessProfileId)
            if (count >= 10) { // Arbitrary limit for testing compatibility
                return Result.failure(IllegalStateException("Maximum template limit reached"))
            }
            templateDao.insertTemplate(template)
            Result.success(template.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTemplate(template: InvoiceTemplate): Result<Unit> {
        return try {
            templateDao.updateTemplate(template)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTemplate(templateId: String): Result<Unit> {
        return try {
            templateDao.softDeleteTemplate(templateId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setAsDefault(templateId: String, businessProfileId: Long): Result<Unit> {
        return try {
            val template = templateDao.getTemplate(templateId)
            if (template == null) {
                return Result.failure(Exception("Template not found"))
            }
            if (template.businessProfileId != businessProfileId) {
                return Result.failure(IllegalArgumentException("Template does not belong to this business"))
            }
            templateDao.clearDefaults(businessProfileId)
            templateDao.updateTemplate(template.copy(isDefault = true))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDefaultTemplate(businessProfileId: Long): Result<InvoiceTemplate?> {
        return try {
            Result.success(templateDao.getDefaultTemplate(businessProfileId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCustomField(field: InvoiceCustomField): Result<String> {
        return try {
            val count = fieldDao.getFieldCount(field.templateId)
            if (count >= 50) {
                return Result.failure(IllegalStateException("Maximum field limit reached"))
            }
            fieldDao.insertField(field)
            Result.success(field.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCustomFields(templateId: String): Result<List<InvoiceCustomField>> {
        return try {
            Result.success(fieldDao.getFieldsByTemplate(templateId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCustomField(field: InvoiceCustomField): Result<Unit> {
        return try {
            fieldDao.updateField(field)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCustomField(fieldId: String): Result<Unit> {
        return try {
            fieldDao.softDeleteField(fieldId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
