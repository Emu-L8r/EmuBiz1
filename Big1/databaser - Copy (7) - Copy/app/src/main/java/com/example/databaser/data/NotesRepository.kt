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
    fun getNotesForQuote(quoteId: Int): Flow<List<Note>>
}
