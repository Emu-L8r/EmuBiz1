package com.emul8r.bizap.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.Note
import com.emul8r.bizap.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    // TODO: Get businessId from context or preferences instead of hardcoding
    private val businessId: Long = 1L

    val currentNotesCount: StateFlow<Int> = noteRepository.getCurrentNotesCount(businessId)
        .catch { e ->
            Timber.e(e, "Error observing current notes count")
            emit(0)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val allNotes: StateFlow<NotesUiState> = noteRepository.getAllNotes(businessId)
        .map { notes ->
            if (notes.isEmpty()) NotesUiState.Empty
            else NotesUiState.Success(notes)
        }
        .catch { e ->
            Timber.e(e, "Error observing notes")
            emit(NotesUiState.Error(e.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotesUiState.Loading
        )

    fun createNote(title: String, content: String, customerId: Long? = null, invoiceId: Long? = null) {
        viewModelScope.launch {
            try {
                val note = Note(
                    businessProfileId = businessId,
                    title = title,
                    content = content,
                    customerId = customerId,
                    invoiceId = invoiceId,
                    isCurrent = true
                )
                noteRepository.createNote(note)
                Timber.d("Note created successfully")
            } catch (e: Exception) {
                Timber.e(e, "Error creating note")
            }
        }
    }

    fun updateNote(noteId: Long, title: String, content: String, isCurrent: Boolean) {
        viewModelScope.launch {
            try {
                val note = Note(
                    id = noteId,
                    businessProfileId = businessId,
                    title = title,
                    content = content,
                    isCurrent = isCurrent,
                    updatedAt = System.currentTimeMillis()
                )
                noteRepository.updateNote(note)
                Timber.d("Note updated successfully")
            } catch (e: Exception) {
                Timber.e(e, "Error updating note")
            }
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(noteId)
                Timber.d("Note deleted successfully")
            } catch (e: Exception) {
                Timber.e(e, "Error deleting note")
            }
        }
    }
}

sealed interface NotesUiState {
    object Loading : NotesUiState
    object Empty : NotesUiState
    data class Success(val notes: List<Note>) : NotesUiState
    data class Error(val message: String) : NotesUiState
}


