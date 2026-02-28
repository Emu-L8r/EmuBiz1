package com.example.databaser.data

import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun getAllNotesStream(): Flow<List<Note>>
    fun getNoteStream(id: Int): Flow<Note?>
    suspend fun insertNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun updateNote(note: Note)
    fun getNotesCount(): Flow<Int>
    fun getNotesForCustomer(customerId: Int): Flow<List<Note>>
    fun getNotesForInvoice(invoiceId: Int): Flow<List<Note>>
}

class OfflineNotesRepository(private val noteDao: NoteDao) : NotesRepository {
    override fun getAllNotesStream(): Flow<List<Note>> = noteDao.getAllNotes()
    override fun getNoteStream(id: Int): Flow<Note?> = noteDao.getNoteById(id)
    override suspend fun insertNote(note: Note) {
        noteDao.insert(note)
    }
    override suspend fun deleteNote(note: Note) = noteDao.delete(note)
    override suspend fun updateNote(note: Note) = noteDao.update(note)
    override fun getNotesCount(): Flow<Int> = noteDao.getNotesCount()
    override fun getNotesForCustomer(customerId: Int): Flow<List<Note>> = noteDao.getNotesForCustomer(customerId)
    override fun getNotesForInvoice(invoiceId: Int): Flow<List<Note>> = noteDao.getNotesForInvoice(invoiceId)
}