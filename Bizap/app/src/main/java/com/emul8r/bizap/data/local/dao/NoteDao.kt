package com.emul8r.bizap.data.local.dao

import androidx.room.*
import com.emul8r.bizap.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE id = :id AND isActive = 1")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE businessProfileId = :businessId AND isActive = 1 ORDER BY updatedAt DESC")
    fun observeAllNotes(businessId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE businessProfileId = :businessId AND isCurrent = 1 AND isActive = 1 ORDER BY updatedAt DESC")
    fun observeCurrentNotes(businessId: Long): Flow<List<NoteEntity>>

    @Query("SELECT COUNT(*) FROM notes WHERE businessProfileId = :businessId AND isCurrent = 1 AND isActive = 1")
    fun observeCurrentNotesCount(businessId: Long): Flow<Int>

    @Query("SELECT * FROM notes WHERE customerId = :customerId AND isActive = 1 ORDER BY updatedAt DESC")
    fun observeCustomerNotes(customerId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE invoiceId = :invoiceId AND isActive = 1 ORDER BY updatedAt DESC")
    fun observeInvoiceNotes(invoiceId: Long): Flow<List<NoteEntity>>
}

