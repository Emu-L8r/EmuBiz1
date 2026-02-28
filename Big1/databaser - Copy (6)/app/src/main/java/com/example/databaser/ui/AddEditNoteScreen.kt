package com.example.databaser.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.databaser.R
import com.example.databaser.data.Note
import com.example.databaser.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    noteId: Int?,
    customerId: Int?,
    invoiceId: Int?,
    onNoteSaved: () -> Unit,
    noteViewModel: NoteViewModel = viewModel(factory = NoteViewModel.Factory)
) {
    val note by if (noteId != null) {
        noteViewModel.getNote(noteId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    if (noteId != null && note != null) {
        title = note!!.title
        content = note!!.content
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (noteId == null) stringResource(R.string.add_note) else stringResource(R.string.edit_note)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.title)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(stringResource(R.string.content)) },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val newNote = Note(
                    id = noteId ?: 0,
                    title = title,
                    content = content,
                    createdAt = System.currentTimeMillis(),
                    customerId = customerId,
                    invoiceId = invoiceId
                )
                if (noteId == null) {
                    noteViewModel.addNote(newNote)
                } else {
                    noteViewModel.updateNote(newNote)
                }
                onNoteSaved()
            }) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
