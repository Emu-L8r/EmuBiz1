package com.example.databaser.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.databaser.R
import com.example.databaser.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    onAddNoteClick: () -> Unit,
    onNoteClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    navController: NavHostController,
    noteViewModel: NoteViewModel = hiltViewModel()
) {
    val notes by noteViewModel.allNotes.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(R.string.notes),
                canNavigateBack = true,
                onNavigateUp = onBackClick,
                navController = navController
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNoteClick) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_note))
            }
        }
    ) { padding ->
        if (notes == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                items(notes!!, key = { it.id }) { note ->
                    NoteListItem(note = note, onClick = { onNoteClick(note.id) })
                }
            }
        }
    }
}
