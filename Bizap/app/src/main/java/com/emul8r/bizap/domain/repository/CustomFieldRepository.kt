package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.CustomField

/**
 * Domain-level contract for Custom Field data operations on Invoice Templates.
 *
 * Constraints:
 * - Max [MAX_FIELDS_PER_TEMPLATE] active fields per template
 */
interface CustomFieldRepository {

    /**
     * Saves a new custom field for a given template.
     *
     * @return [Result.success] with the new field ID on success, or [Result.failure] if the
     * maximum field limit is reached or the database operation fails.
     */
    suspend fun saveCustomField(field: CustomField): Result<String>

    /**
     * Updates an existing custom field.
     *
     * @return [Result.success] on success, or [Result.failure] if the field does not exist or
     * the database operation fails.
     */
    suspend fun updateCustomField(field: CustomField): Result<Unit>

    /**
     * Soft-deletes a custom field by its ID.
     *
     * @return [Result.success] on success, or [Result.failure] if the database operation fails.
     */
    suspend fun deleteCustomField(fieldId: String): Result<Unit>

    /**
     * Retrieves all active custom fields for the given template, ordered by display order.
     *
     * @return [Result.success] with the list of active fields, or [Result.failure] if the
     * database operation fails.
     */
    suspend fun getCustomFields(templateId: String): Result<List<CustomField>>

    companion object {
        const val MAX_FIELDS_PER_TEMPLATE = 50
    }
}
