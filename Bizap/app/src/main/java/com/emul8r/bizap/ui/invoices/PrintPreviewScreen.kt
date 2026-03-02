package com.emul8r.bizap.ui.invoices

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.emul8r.bizap.ui.invoices.components.InvoiceActionHub

@Composable
fun PrintPreviewScreen(viewModel: InvoicePdfViewModel = hiltViewModel()) {
    var showSheet by remember { mutableStateOf(false) }
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSheet = true },
                icon = { Icon(Icons.Default.IosShare, null) },
                text = { Text("Export Document") }
            )
        }
    ) { padding ->
        // PDF Render logic here...

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

