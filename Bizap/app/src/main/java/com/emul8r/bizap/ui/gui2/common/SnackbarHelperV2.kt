package com.emul8r.bizap.ui.gui2.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Snackbar helper function for showing success/error messages in analytics screens.
 *
 * Usage:
 * ```
 * val snackbarHostState = remember { SnackbarHostState() }
 *
 * LaunchedEffect(exportMessage) {
 *     if (exportMessage.isNotEmpty()) {
 *         showSnackbar(snackbarHostState, exportMessage, isSuccess = true)
 *     }
 * }
 * ```
 */
suspend fun showSnackbar(
    hostState: SnackbarHostState,
    message: String,
    isSuccess: Boolean = false,
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    hostState.showSnackbar(
        message = message,
        duration = duration
    )
}

/**
 * Export Success Snackbar - Shows when export completes successfully.
 */
@Composable
fun ExportSuccessSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Success")
            Text(message, modifier = Modifier.weight(1f))
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    }
}

/**
 * Export Error Snackbar - Shows when export fails.
 */
@Composable
fun ExportErrorSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
            Text(message, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.error)
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}





