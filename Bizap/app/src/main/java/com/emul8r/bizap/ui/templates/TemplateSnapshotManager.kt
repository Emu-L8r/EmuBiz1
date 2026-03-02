package com.emul8r.bizap.ui.templates

import com.emul8r.bizap.data.local.entities.InvoiceTemplate
import com.emul8r.bizap.data.local.entities.InvoiceCustomField
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.time.LocalDateTime

/**
 * Manages serialization and deserialization of template snapshots.
 */
class TemplateSnapshotManager {

    companion object {
        private const val TAG = "TemplateSnapshotManager"
        private val json = Json { prettyPrint = false }
    }

    fun createSnapshot(template: InvoiceTemplate): String {
        return try {
            val snapshot = TemplateSnapshot(
                id = template.id,
                name = template.name,
                designType = template.designType,
                primaryColor = template.primaryColor,
                secondaryColor = template.secondaryColor,
                fontFamily = template.fontFamily,
                companyName = template.companyName,
                companyAddress = template.companyAddress,
                companyPhone = template.companyPhone,
                companyEmail = template.companyEmail,
                taxId = template.taxId,
                bankDetails = template.bankDetails,
                hideLineItems = template.hideLineItems,
                hidePaymentTerms = template.hidePaymentTerms,
                logoFileName = template.logoFileName
            )
            val jsonString = json.encodeToString(TemplateSnapshot.serializer(), snapshot)
            Timber.d("✅ Snapshot created: ${template.name}")
            jsonString
        } catch (e: Exception) {
            Timber.e(e, "❌ Error creating snapshot")
            "{}"
        }
    }

    fun restoreSnapshot(jsonSnapshot: String?): TemplateSnapshot? {
        if (jsonSnapshot.isNullOrBlank()) return null
        return try {
            val snapshot = json.decodeFromString(TemplateSnapshot.serializer(), jsonSnapshot)
            Timber.d("✅ Snapshot restored: ${snapshot.name}")
            snapshot
        } catch (e: Exception) {
            Timber.e(e, "❌ Error restoring snapshot")
            null
        }
    }

    fun createCustomFieldValuesMap(values: Map<String, String>): String {
        return try {
            val map = CustomFieldValuesMap(values)
            val jsonString = json.encodeToString(CustomFieldValuesMap.serializer(), map)
            Timber.d("✅ Custom field values map created: ${values.size} fields")
            jsonString
        } catch (e: Exception) {
            Timber.e(e, "❌ Error creating custom field values map")
            "{}"
        }
    }

    fun restoreCustomFieldValues(jsonMap: String?): Map<String, String> {
        if (jsonMap.isNullOrBlank()) return emptyMap()
        return try {
            val map = json.decodeFromString(CustomFieldValuesMap.serializer(), jsonMap)
            Timber.d("✅ Custom field values restored: ${map.values.size} fields")
            map.values
        } catch (e: Exception) {
            Timber.e(e, "❌ Error restoring custom field values")
            emptyMap()
        }
    }

    fun isValidSnapshot(jsonSnapshot: String?): Boolean {
        return !jsonSnapshot.isNullOrBlank() && restoreSnapshot(jsonSnapshot) != null
    }
}

@Serializable
data class TemplateSnapshot(
    val id: String,
    val name: String,
    val designType: String,
    val primaryColor: String,
    val secondaryColor: String,
    val fontFamily: String,
    val companyName: String,
    val companyAddress: String,
    val companyPhone: String,
    val companyEmail: String,
    val taxId: String?,
    val bankDetails: String?,
    val hideLineItems: Boolean,
    val hidePaymentTerms: Boolean,
    val logoFileName: String?
)

@Serializable
data class CustomFieldValuesMap(
    val values: Map<String, String> = emptyMap()
)

