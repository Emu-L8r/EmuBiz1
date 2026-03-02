package com.emul8r.bizap.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.Note
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val profileRepository: BusinessProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotesUiState>(NotesUiState.Loading)
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private var businessId: Long = 0

    init {
        viewModelScope.launch {
            businessId = profileRepository.getActiveBusinessId()
            noteRepository.getNotes(businessId)
                .map { NotesUiState.Success(it) }
                .onEach { _uiState.value = it }
                .catch { _uiState.value = NotesUiState.Error(it.message ?: "Unknown error") }
                .collect()
        }
    }

    fun saveNote(title: String, content: String, id: Long = 0, isPinned: Boolean = false) {
        viewModelScope.launch {
            val note = Note(
                id = id,
                businessProfileId = businessId,
                title = title,
                content = content,
                isPinned = isPinned,
                updatedAt = System.currentTimeMillis()
            )
            noteRepository.saveNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            noteRepository.saveNote(note.copy(isPinned = !note.isPinned))
        }
    }
}

sealed interface NotesUiState {
    object Loading : NotesUiState
    data class Success(val notes: List<Note>) : NotesUiState
    data class Error(val message: String) : NotesUiState
}
