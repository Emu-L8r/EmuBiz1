@file:OptIn(ExperimentalMaterial3Api::class)

package com.emul8r.bizap.ui.settings.backup

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.ui.settings.backup.BackupRestoreUiState.*
import timber.log.Timber
import java.io.File
import java.util.Locale

@Composable
fun BackupRestoreScreen(
    viewModel: BackupRestoreViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var selectedRestoreFile by remember { mutableStateOf<File?>(null) }

    // Reset features state
    var showResetDialog by remember { mutableStateOf(false) }
    var resetType by remember { mutableStateOf<ResetType?>(null) }
    var deleteConfirmationText by remember { mutableStateOf("") }
    var deleteConfirmationError by remember { mutableStateOf<String?>(null) }
    val DELETE_CONFIRMATION_WORD = "delete" // Word to type for confirmation

    // File picker for backup directory
    val backupDirectoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            Timber.d("Backup directory selected: $uri")
            viewModel.createBackup(uri.toString())
        }
    }

    // File picker for restore file
    val restoreFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "restore_temp.db")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                selectedRestoreFile = file
                showRestoreConfirmDialog = true
                Timber.d("Restore file selected: ${file.absolutePath}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load restore file")
            }
        }
    }

    // Listen for events
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is BackupRestoreEvent.ShowSnackbar -> {
                    Timber.d("Snackbar: ${event.message}")
                }
                is BackupRestoreEvent.RestartApp -> {
                    Timber.d("Restarting app after restore")
                    val packageManager = context.packageManager
                    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                    if (intent != null) {
                        context.startActivity(intent)
                        Runtime.getRuntime().exit(0)
                    }
                }
            }
        }
    }

    // Restore confirmation dialog
    if (showRestoreConfirmDialog && selectedRestoreFile != null) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = { Text("Confirm Restore") },
            text = {
                Text(
                    "This will replace ALL current data with the backup. This action cannot be undone. Are you sure?",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog = false
                        viewModel.restoreFromBackup(selectedRestoreFile!!)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Confirmation Dialog with Password
    if (showResetDialog && resetType != null) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("⚠️ Confirm Reset") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        when (resetType) {
                            ResetType.ALL_DATA -> "This will permanently delete ALL data including customers, invoices, and all settings. This action CANNOT be undone!"
                            ResetType.CUSTOMER_DATA -> "This will permanently delete ALL customer records. Associated invoices will be orphaned. This action CANNOT be undone!"
                            ResetType.INVOICE_DATA -> "This will permanently delete ALL invoices and payments. This action CANNOT be undone!"
                            null -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )

                    Text(
                        "Type 'delete' to confirm this action:",
                        style = MaterialTheme.typography.labelMedium
                    )

                    OutlinedTextField(
                        value = deleteConfirmationText,
                        onValueChange = { deleteConfirmationText = it; deleteConfirmationError = null },
                        label = { Text("Type 'delete'") },
                        placeholder = { Text("Type the word: delete") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = deleteConfirmationError != null,
                        supportingText = {
                            if (deleteConfirmationError != null) {
                                Text(
                                    deleteConfirmationError!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deleteConfirmationText.lowercase() == DELETE_CONFIRMATION_WORD) {
                            Timber.w("User confirmed reset by typing DELETE: ${resetType?.name}")
                            // Execute the actual reset based on type
                            when (resetType) {
                                ResetType.ALL_DATA -> {
                                    Timber.w("🚨 Executing: Reset All Data")
                                    viewModel.resetAllData()
                                }
                                ResetType.CUSTOMER_DATA -> {
                                    Timber.w("🚨 Executing: Reset Customer Data")
                                    viewModel.resetCustomerData()
                                }
                                ResetType.INVOICE_DATA -> {
                                    Timber.w("🚨 Executing: Reset Invoice Data")
                                    viewModel.resetInvoiceData()
                                }
                                null -> {}
                            }
                            showResetDialog = false
                            deleteConfirmationText = ""
                            deleteConfirmationError = null
                        } else {
                            deleteConfirmationError = "Please type 'delete' exactly to confirm"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = deleteConfirmationText.isNotEmpty()
                ) {
                    Text("Yes, Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    deleteConfirmationText = ""
                    deleteConfirmationError = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {}  // MainActivity provides the header
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Information card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Database Backup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Create encrypted backups of your entire database. Backups are stored locally on your device or cloud storage.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Backup status section
                when (uiState) {
                    is BackupInProgress -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    "Creating backup...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    is BackupSuccess -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "✓ Backup Created",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "File: ${(uiState as BackupSuccess).backupFile.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Size: ${formatBytes((uiState as BackupSuccess).sizeBytes)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Date: ${(uiState as BackupSuccess).dateTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    is BackupError -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "✗ Backup Failed",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    (uiState as BackupError).message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    else -> {}
                }

                // Create Backup button
                Button(
                    onClick = { backupDirectoryPicker.launch(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = uiState != BackupInProgress && uiState != RestoreInProgress
                ) {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = "Create Backup",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Backup")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Restore section header
                Text(
                    "Restore from Backup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Restore status section
                when (uiState) {
                    is RestoreInProgress -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    "Restoring from backup...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    is RestoreSuccess -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "✓ Restore Complete",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "App will restart to apply changes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    is RestoreError -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "✗ Restore Failed",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    (uiState as RestoreError).message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    else -> {}
                }

                // Restore from Backup button
                Button(
                    onClick = { restoreFilePicker.launch(arrayOf("*/*")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = uiState != BackupInProgress && uiState != RestoreInProgress
                ) {
                    Icon(
                        Icons.Default.CloudDownload,
                        contentDescription = "Restore from Backup",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore from Backup")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Reset Data Section
                Text(
                    "Danger Zone - Reset Data",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Reset All Data Button
                OutlinedButton(
                    onClick = {
                        resetType = ResetType.ALL_DATA
                        showResetDialog = true
                        deleteConfirmationText = ""
                        deleteConfirmationError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Reset All Data",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset All Data")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Reset Customer Data Button
                OutlinedButton(
                    onClick = {
                        resetType = ResetType.CUSTOMER_DATA
                        showResetDialog = true
                        deleteConfirmationText = ""
                        deleteConfirmationError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Reset Customer Data",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset All Customer Data")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Reset Invoice Data Button
                OutlinedButton(
                    onClick = {
                        resetType = ResetType.INVOICE_DATA
                        showResetDialog = true
                        deleteConfirmationText = ""
                        deleteConfirmationError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Reset Invoice Data",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset All Invoice Data")
                }
            }
        }
    }
}

/**
 * Formats bytes into human-readable size string.
 */
private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> String.format(Locale.getDefault(), "%.2f MB", bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> String.format(Locale.getDefault(), "%.2f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}

/**
 * Enum for reset data options
 */
enum class ResetType {
    ALL_DATA,           // Reset everything including settings
    CUSTOMER_DATA,      // Reset only customer records
    INVOICE_DATA        // Reset only invoices and payments
}

