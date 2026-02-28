package com.example.databaser.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note): Long

    @Insert
    suspend fun insertAll(notes: List<Note>)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()

    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun getNoteById(noteId: Int): Flow<Note?>

    @Query("SELECT * FROM notes WHERE customerId = :customerId")
    fun getNotesForCustomer(customerId: Int): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE invoiceId = :invoiceId")
    fun getNotesForInvoice(invoiceId: Int): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE quoteId = :quoteId")
    fun getNotesForQuote(quoteId: Int): Flow<List<Note>>

    @Query("SELECT COUNT(*) FROM notes")
    fun getNotesCount(): Flow<Int>

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes")
    suspend fun getAllNotesList(): List<Note>
}
