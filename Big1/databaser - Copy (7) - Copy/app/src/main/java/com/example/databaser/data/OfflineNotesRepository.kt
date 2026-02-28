package com.example.databaser.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OfflineNotesRepository @Inject constructor(private val noteDao: NoteDao) : NotesRepository {
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
    override fun getNotesForQuote(quoteId: Int): Flow<List<Note>> = noteDao.getNotesForQuote(quoteId)
}
