package com.example.databaser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.databaser.data.Note
import com.example.databaser.data.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(private val notesRepository: NotesRepository) : ViewModel() {

    val allNotes: StateFlow<List<Note>> = notesRepository.getAllNotesStream().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    fun getNotesCount(): Flow<Int> = notesRepository.getNotesCount()

    fun getNotesForCustomer(customerId: Int): Flow<List<Note>> = notesRepository.getNotesForCustomer(customerId)

    fun getNotesForInvoice(invoiceId: Int): Flow<List<Note>> = notesRepository.getNotesForInvoice(invoiceId)
    
    fun getNotesForQuote(quoteId: Int): Flow<List<Note>> = notesRepository.getNotesForQuote(quoteId)

    fun getNote(noteId: Int): Flow<Note?> = notesRepository.getNoteStream(noteId)

    fun addNote(note: Note) {
        viewModelScope.launch {
            notesRepository.insertNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            notesRepository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            notesRepository.deleteNote(note)
        }
    }
}
