package com.emul8r.bizap.ui.invoices.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceActionHub(
    onShare: () -> Unit,
    onSaveToDownloads: () -> Unit,
    onPrint: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                "Export Options",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 1. Share Action
            ListItem(
                headlineContent = { Text("Share via Email/Apps") },
                supportingContent = { Text("Send PDF to your client now") },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.Send, null) },
                modifier = Modifier.clickable { onShare(); onDismiss() }
            )

            // 2. Public Downloads Action
            ListItem(
                headlineContent = { Text("Save to Downloads") },
                supportingContent = { Text("Copy to phone's public folder") },
                leadingContent = { Icon(Icons.Default.FileDownload, null) },
                modifier = Modifier.clickable { onSaveToDownloads(); onDismiss() }
            )

            // 3. System Print Action
            ListItem(
                headlineContent = { Text("System Print") },
                supportingContent = { Text("Wireless print or Save as PDF") },
                leadingContent = { Icon(Icons.Default.Print, null) },
                modifier = Modifier.clickable { onPrint(); onDismiss() }
            )
        }
    }
}