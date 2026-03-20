package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.InvoiceCustomFieldDao
import com.emul8r.bizap.data.local.entities.InvoiceCustomField
import com.emul8r.bizap.domain.repository.CustomFieldRepository
import com.emul8r.bizap.domain.repository.CustomFieldRepository.Companion.MAX_FIELDS_PER_TEMPLATE
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [CustomFieldRepository] backed by [InvoiceCustomFieldDao].
 */
@Singleton
class CustomFieldRepositoryImpl @Inject constructor(
    private val fieldDao: InvoiceCustomFieldDao
) : CustomFieldRepository {

    override suspend fun saveCustomField(field: InvoiceCustomField): Result<String> = runCatching {
        val count = fieldDao.getFieldCount(field.templateId)
        require(count < MAX_FIELDS_PER_TEMPLATE) {
            "Maximum custom field limit of $MAX_FIELDS_PER_TEMPLATE reached for this template"
        }
        fieldDao.insertField(field)
        Timber.d("✅ Saved custom field '${field.label}' for template ${field.templateId}")
        field.id
    }.also { result ->
        result.onFailure { e ->
            Timber.e(e, "❌ Failed to save custom field for template ${field.templateId}")
        }
    }

    override suspend fun updateCustomField(field: InvoiceCustomField): Result<Unit> = runCatching {
        fieldDao.updateField(field)
        Timber.d("✅ Updated custom field '${field.label}' (${field.id})")
    }.also { result ->
        result.onFailure { e ->
            Timber.e(e, "❌ Failed to update custom field ${field.id}")
        }
    }

    override suspend fun deleteCustomField(fieldId: String): Result<Unit> = runCatching {
        fieldDao.softDeleteField(fieldId)
        Timber.d("✅ Deleted custom field $fieldId")
    }.also { result ->
        result.onFailure { e ->
            Timber.e(e, "❌ Failed to delete custom field $fieldId")
        }
    }

    override suspend fun getCustomFields(templateId: String): Result<List<InvoiceCustomField>> = runCatching {
        val fields = fieldDao.getFieldsByTemplate(templateId)
        Timber.d("✅ Retrieved ${fields.size} custom fields for template $templateId")
        fields
    }.also { result ->
        result.onFailure { e ->
            Timber.e(e, "❌ Failed to get custom fields for template $templateId")
        }
    }
}
