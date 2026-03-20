package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.NoteDao
import com.emul8r.bizap.data.local.entity.toDomain
import com.emul8r.bizap.data.local.entity.toEntity
import com.emul8r.bizap.domain.model.Note
import com.emul8r.bizap.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override suspend fun createNote(note: Note): Long {
        return noteDao.insertNote(note.toEntity())
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(note.toEntity())
    }

    override suspend fun deleteNote(noteId: Long) {
        // Soft delete - mark as inactive instead of true deletion
        val noteEntity = noteDao.getNoteById(noteId)
        noteEntity?.let {
            noteDao.updateNote(it.copy(isActive = false, updatedAt = System.currentTimeMillis()))
        }
    }

    override fun getAllNotes(businessId: Long): Flow<List<Note>> {
        return noteDao.observeAllNotes(businessId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCurrentNotes(businessId: Long): Flow<List<Note>> {
        return noteDao.observeCurrentNotes(businessId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCurrentNotesCount(businessId: Long): Flow<Int> {
        return noteDao.observeCurrentNotesCount(businessId)
    }

    override fun getCustomerNotes(customerId: Long): Flow<List<Note>> {
        return noteDao.observeCustomerNotes(customerId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getInvoiceNotes(invoiceId: Long): Flow<List<Note>> {
        return noteDao.observeInvoiceNotes(invoiceId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}

