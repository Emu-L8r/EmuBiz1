package com.example.databaser.data

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

object Converters {
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    @TypeConverter
    @JvmStatic
    fun fromString(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            json.decodeFromString(ListSerializer(String.serializer()), value)
        } catch (t: Throwable) {
            // Fallback to older comma-based format
            return value.split(",").map { it.trim() }
        }
    }

    @TypeConverter
    @JvmStatic
    fun fromList(list: List<String>?): String {
        if (list == null || list.isEmpty()) return "[]"
        return json.encodeToString(ListSerializer(String.serializer()), list)
    }
}
