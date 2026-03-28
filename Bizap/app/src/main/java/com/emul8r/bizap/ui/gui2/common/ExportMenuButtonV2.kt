package com.emul8r.bizap.ui.gui2.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Export menu button for analytics screens.
 *
 * Provides options to export data as PDF or CSV with loading state.
 *
 * @param onExportPdf Callback when PDF export is selected
 * @param onExportCsv Callback when CSV export is selected
 * @param isExporting Whether export is currently in progress
 * @param modifier Optional modifier
 */
@Composable
fun ExportMenuButtonV2(
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    isExporting: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            enabled = !isExporting
        ) {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = "Export",
                tint = if (isExporting) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Export as PDF") },
                onClick = {
                    onExportPdf()
                    expanded = false
                },
                leadingIcon = {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                }
            )

            DropdownMenuItem(
                text = { Text("Export as CSV") },
                onClick = {
                    onExportCsv()
                    expanded = false
                },
                leadingIcon = {
                    Icon(Icons.Default.Share, contentDescription = null)
                }
            )
        }
    }
}


