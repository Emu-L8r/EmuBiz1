package com.emu.emubizwax.ui.invoices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.emu.emubizwax.ui.invoices.components.CustomerSelector
import com.emu.emubizwax.ui.invoices.components.InvoiceSummaryBar
import com.emu.emubizwax.ui.invoices.components.LineItemRow
import com.emu.emubizwax.ui.invoices.components.PhotoAttachmentSection

@Composable
fun CreateInvoiceScreen(
    viewModel: InvoiceFormViewModel = hiltViewModel(),
    onSaveComplete: () -> Unit
) {
    val state by viewModel.formState.collectAsState()

    Scaffold(
        topBar = { 
            TopAppBar(title = { Text(if (state.isQuote) "New Quote" else "New Invoice") }) 
        },
        bottomBar = {
            InvoiceSummaryBar(state, onSave = {
                viewModel.saveInvoice()
                onSaveComplete()
            })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CustomerSelector(viewModel = viewModel)
            }

            item { 
                Text("Items", style = MaterialTheme.typography.titleMedium) 
            }

            items(state.items, key = { it.id }) { item ->
                LineItemRow(
                    item = item,
                    onUpdate = { updated -> viewModel.updateItem(item.id, updated) },
                    onRemove = { viewModel.removeItem(item.id) }
                )
            }

            item {
                TextButton(onClick = { viewModel.addItem() }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add Line Item")
                }
            }

            item {
                PhotoAttachmentSection(
                    selectedUris = state.photoUris,
                    onPhotosSelected = viewModel::onPhotosSelected,
                    onRemovePhoto = viewModel::onRemovePhoto
                )
            }
        }
    }
}
