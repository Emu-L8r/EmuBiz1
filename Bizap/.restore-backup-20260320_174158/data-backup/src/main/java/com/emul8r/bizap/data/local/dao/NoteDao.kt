package com.emul8r.bizap.data.local.dao

import androidx.room.*
import com.emul8r.bizap.domain.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE id = :id AND isActive = 1")
    suspend fun getNoteById(id: Long): Note?

    @Query("SELECT * FROM notes WHERE businessProfileId = :businessId AND isActive = 1 ORDER BY updatedAt DESC")
    fun observeAllNotes(businessId: Long): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE businessProfileId = :businessId AND isCurrent = 1 AND isActive = 1 ORDER BY updatedAt DESC")
    fun observeCurrentNotes(businessId: Long): Flow<List<Note>>

    @Query("SELECT COUNT(*) FROM notes WHERE businessProfileId = :businessId AND isCurrent = 1 AND isActive = 1")
    fun observeCurrentNotesCount(businessId: Long): Flow<Int>

    @Query("SELECT * FROM notes WHERE customerId = :customerId AND isActive = 1 ORDER BY updatedAt DESC")
    fun observeCustomerNotes(customerId: Long): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE invoiceId = :invoiceId AND isActive = 1 ORDER BY updatedAt DESC")
    fun observeInvoiceNotes(invoiceId: Long): Flow<List<Note>>
}

