package com.example.databaser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.databaser.MyApplication
import com.example.databaser.data.Note
import com.example.databaser.data.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(private val notesRepository: NotesRepository) : ViewModel() {

    val allNotes: StateFlow<List<Note>> = notesRepository.getAllNotesStream().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    fun getNotesCount(): Flow<Int> = notesRepository.getNotesCount()

    fun getNotesForCustomer(customerId: Int): Flow<List<Note>> = notesRepository.getNotesForCustomer(customerId)

    fun getNotesForInvoice(invoiceId: Int): Flow<List<Note>> = notesRepository.getNotesForInvoice(invoiceId)

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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MyApplication)
                val notesRepository = application.container.notesRepository
                NoteViewModel(notesRepository = notesRepository)
            }
        }
    }
}
