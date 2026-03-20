package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    suspend fun createNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(noteId: Long)
    fun getAllNotes(businessId: Long): Flow<List<Note>>
    fun getCurrentNotes(businessId: Long): Flow<List<Note>>
    fun getCurrentNotesCount(businessId: Long): Flow<Int>
    fun getCustomerNotes(customerId: Long): Flow<List<Note>>
    fun getInvoiceNotes(invoiceId: Long): Flow<List<Note>>
}

