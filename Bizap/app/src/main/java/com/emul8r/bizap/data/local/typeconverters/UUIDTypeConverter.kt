package com.emul8r.bizap.data.local.typeconverters

import androidx.room.TypeConverter
import java.util.UUID

/**
 * Room TypeConverter for java.util.UUID.
 * Converts between UUID and String.
 */
class UUIDTypeConverter {
    @TypeConverter
    fun fromUUID(value: UUID?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toUUID(value: String?): UUID? {
        return value?.let { UUID.fromString(it) }
    }
}
