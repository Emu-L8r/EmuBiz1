package com.example.databaser.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.databaser.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Query("SELECT * FROM notes WHERE customerId = :customerId")
    fun getNotesForCustomer(customerId: Long): Flow<List<Note>>

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun delete(noteId: Long)
}
