package com.emul8r.bizap.data.local.typeconverters

import androidx.room.TypeConverter
import com.emul8r.bizap.data.local.entities.DocumentStatus

class DocumentStatusConverter {
    @TypeConverter
    fun fromDocumentStatus(status: DocumentStatus): String {
        return when (status) {
            is DocumentStatus.Archived -> "ARCHIVED"
            is DocumentStatus.Exported -> "EXPORTED;${status.exportPath}"
        }
    }

    @TypeConverter
    fun toDocumentStatus(status: String): DocumentStatus {
        return if (status.startsWith("EXPORTED;")) {
            DocumentStatus.Exported(status.removePrefix("EXPORTED;"))
        } else {
            DocumentStatus.Archived
        }
    }
}
