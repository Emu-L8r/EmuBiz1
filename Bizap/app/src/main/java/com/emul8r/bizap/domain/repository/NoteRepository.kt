package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(businessId: Long): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?
    fun getNoteCount(businessId: Long): Flow<Int>
    suspend fun saveNote(note: Note): Long
    suspend fun deleteNote(note: Note)
}
