package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.NoteDao
import com.emul8r.bizap.data.local.entities.NoteEntity
import com.emul8r.bizap.domain.model.Note
import com.emul8r.bizap.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getNotes(businessId: Long): Flow<List<Note>> {
        return noteDao.getNotesByBusinessId(businessId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getNoteById(id: Long): Note? = withContext(Dispatchers.IO) {
        noteDao.getNoteById(id)?.toDomain()
    }

    override fun getNoteCount(businessId: Long): Flow<Int> {
        return noteDao.getNoteCountByBusinessId(businessId)
            .flowOn(Dispatchers.IO)
    }

    override suspend fun saveNote(note: Note): Long = withContext(Dispatchers.IO) {
        noteDao.insertNote(note.toEntity())
    }

    override suspend fun deleteNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(note.toEntity())
    }

    private fun NoteEntity.toDomain() = Note(
        id = id,
        businessProfileId = businessProfileId,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isPinned = isPinned
    )

    private fun Note.toEntity() = NoteEntity(
        id = id,
        businessProfileId = businessProfileId,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isPinned = isPinned
    )
}
