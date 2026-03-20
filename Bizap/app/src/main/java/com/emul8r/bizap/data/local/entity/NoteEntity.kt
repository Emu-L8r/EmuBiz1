package com.emul8r.bizap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.emul8r.bizap.domain.model.Note

/**
 * Room entity for persisting notes in the database.
 * Maps to/from the domain [Note] model.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessProfileId: Long,
    val customerId: Long? = null,
    val invoiceId: Long? = null,
    val title: String,
    val content: String,
    val isCurrent: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

/**
 * Extension function to convert domain model to entity for persistence.
 */
fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    businessProfileId = businessProfileId,
    customerId = customerId,
    invoiceId = invoiceId,
    title = title,
    content = content,
    isCurrent = isCurrent,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isActive = isActive
)

/**
 * Extension function to convert entity to domain model.
 */
fun NoteEntity.toDomain(): Note = Note(
    id = id,
    businessProfileId = businessProfileId,
    customerId = customerId,
    invoiceId = invoiceId,
    title = title,
    content = content,
    isCurrent = isCurrent,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isActive = isActive
)
