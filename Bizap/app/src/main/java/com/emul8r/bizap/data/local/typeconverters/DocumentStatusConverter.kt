package com.emul8r.bizap.data.local.typeconverters

import androidx.room.TypeConverter
import com.emul8r.bizap.data.local.entities.DocumentStatus

class DocumentStatusConverter {
    @TypeConverter
    fun fromDocumentStatus(status: DocumentStatus): String {
        return status.name
    }

    @TypeConverter
    fun toDocumentStatus(value: String): DocumentStatus {
        return try {
            DocumentStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            // Default to ARCHIVED for legacy or invalid values
            DocumentStatus.ARCHIVED
        }
    }
}

