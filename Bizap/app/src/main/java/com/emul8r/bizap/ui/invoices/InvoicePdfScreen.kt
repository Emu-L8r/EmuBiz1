package com.emul8r.bizap.ui.invoices

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import com.emul8r.bizap.ui.invoices.components.InvoiceActionHub

@Composable
fun InvoicePdfScreen(
    invoiceId: Long,
    isQuote: Boolean,
    viewModel: InvoicePdfViewModel = hiltViewModel()
) {
    var showSheet by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(invoiceId) {
        viewModel.preparePreview(invoiceId, isQuote)
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSheet = true },
                icon = { Icon(Icons.Default.IosShare, contentDescription = "Export") },
                text = { Text("Export Document") }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is PdfPreviewUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PdfPreviewUiState.Ready -> {
                    Image(
                        bitmap = state.previewBitmap.asImageBitmap(),
                        contentDescription = "Invoice Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                is PdfPreviewUiState.Error -> {
                    Text(text = state.message, modifier = Modifier.align(Alignment.Center))
                }
            }
        }

        if (showSheet) {
            InvoiceActionHub(
                onShare = { viewModel.shareInternalFile() },
                onSaveToDownloads = { viewModel.exportToPublicDownloads() },
                onPrint = { viewModel.launchSystemPrint() },
                onDismiss = { showSheet = false }
            )
        }
    }
}
