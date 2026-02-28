package com.emul8r.bizap.ui.invoices

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInvoiceScreen(
    onInvoiceUpdated: () -> Unit,
    viewModel: EditInvoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempImageUri?.let { viewModel.addPhoto(it) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.addPhoto(it) }
    }

    var showAddPhotoDialog by remember { mutableStateOf(false) }

    if (showAddPhotoDialog) {
        AddPhotoDialog(
            onDismiss = { showAddPhotoDialog = false },
            onTakePhoto = {
                showAddPhotoDialog = false
                val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                tempImageUri = FileProvider.getUriForFile(context, "com.emul8r.bizap.fileprovider", file)
                cameraLauncher.launch(tempImageUri)
            },
            onChooseFromGallery = {
                showAddPhotoDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onInvoiceUpdated()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text(if (uiState.isQuote) "Edit Quote" else "Edit Invoice") }) },
        bottomBar = {
            InvoiceBottomSummary(
                total = uiState.items.sumOf { it.quantity * it.unitPrice },
                isSaving = uiState.isSaving,
                onSave = { viewModel.updateInvoice() }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Treat as Quote")
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = uiState.isQuote,
                        onCheckedChange = { viewModel.onIsQuoteChange(it) }
                    )
                }
            }
            item { CustomerDropdown(uiState.selectedCustomer, uiState.customers, onSelect = {}) }
            item { OutlinedTextField(uiState.header, viewModel::onHeaderChange, label = { Text("Header") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(uiState.subheader, viewModel::onSubheaderChange, label = { Text("Subheader") }, modifier = Modifier.fillMaxWidth()) }

            items(uiState.items, key = { it.id ?: it.hashCode().toLong() }) { item ->
                LineItemEditor(
                    description = item.description,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    onUpdate = { desc, qty, price -> viewModel.updateLineItem(item.id, desc, qty, price) },
                    onRemove = { viewModel.removeLineItem(item.id) }
                )
            }

            item {
                TextButton(onClick = viewModel::addLineItem) {
                    Icon(Icons.Default.Add, contentDescription = "Add Line Item")
                    Text("Add Line Item")
                }
            }

            item { OutlinedTextField(uiState.notes, viewModel::onNotesChange, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(uiState.footer, viewModel::onFooterChange, label = { Text("Footer") }, modifier = Modifier.fillMaxWidth()) }

            item {
                Text("Photos", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.photoUris) { uri ->
                        Image(rememberAsyncImagePainter(uri), null, modifier = Modifier.size(100.dp), contentScale = ContentScale.Crop)
                    }
                }
                Button(onClick = { showAddPhotoDialog = true }) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo")
                    Spacer(Modifier.width(8.dp))
                    Text("Add Photo")
                }
            }
        }
    }
}
