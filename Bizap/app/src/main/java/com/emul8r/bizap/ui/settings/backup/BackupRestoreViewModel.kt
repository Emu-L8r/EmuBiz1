package com.emul8r.bizap.ui.settings.backup

import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.backup.DatabaseBackupService
import com.emul8r.bizap.data.backup.DatabaseRestoreService
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.Locale
import javax.inject.Inject

sealed interface BackupRestoreUiState {
    object Idle : BackupRestoreUiState
    object BackupInProgress : BackupRestoreUiState
    data class BackupSuccess(val backupFile: File, val sizeBytes: Long, val dateTime: String) : BackupRestoreUiState
    data class BackupError(val message: String) : BackupRestoreUiState
    object RestoreInProgress : BackupRestoreUiState
    object RestoreSuccess : BackupRestoreUiState
    data class RestoreError(val message: String) : BackupRestoreUiState
}

sealed interface BackupRestoreEvent {
    data class ShowSnackbar(val message: String) : BackupRestoreEvent
    object RestartApp : BackupRestoreEvent
}

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupService: DatabaseBackupService,
    private val restoreService: DatabaseRestoreService,
    private val customerRepository: CustomerRepository,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupRestoreUiState>(BackupRestoreUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<BackupRestoreEvent>()
    val event = _event.asSharedFlow()

    /**
     * Creates a backup of the database to the specified directory.
     *
     * @param backupUri The Uri of the directory to save the backup
     */
    fun createBackup(backupUri: String) {
        viewModelScope.launch {
            _uiState.value = BackupRestoreUiState.BackupInProgress

            try {
                val result = backupService.createBackup(android.net.Uri.parse(backupUri))

                result.onSuccess { backupFile ->
                    val sizeBytes = backupService.getBackupSize(backupFile)
                    val dateTime = backupService.getBackupDateTime(backupFile)

                    _uiState.value = BackupRestoreUiState.BackupSuccess(
                        backupFile = backupFile,
                        sizeBytes = sizeBytes,
                        dateTime = dateTime
                    )

                    _event.emit(
                        BackupRestoreEvent.ShowSnackbar(
                            "Backup created: ${backupFile.name} (${formatBytes(sizeBytes)})"
                        )
                    )

                    Timber.d("Backup successful: ${backupFile.absolutePath}")
                }

                result.onFailure { exception ->
                    val errorMsg = exception.message ?: "Unknown error during backup"
                    _uiState.value = BackupRestoreUiState.BackupError(errorMsg)
                    _event.emit(BackupRestoreEvent.ShowSnackbar("Backup failed: $errorMsg"))
                    Timber.e(exception, "Backup failed")
                }

            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unexpected error"
                _uiState.value = BackupRestoreUiState.BackupError(errorMsg)
                _event.emit(BackupRestoreEvent.ShowSnackbar("Backup error: $errorMsg"))
                Timber.e(e, "Backup error")
            }
        }
    }

    /**
     * Restores the database from a backup file.
     * Requires app restart to be called via the event flow.
     *
     * @param backupFile The backup file to restore from
     */
    fun restoreFromBackup(backupFile: File) {
        viewModelScope.launch {
            _uiState.value = BackupRestoreUiState.RestoreInProgress

            try {
                val result = restoreService.restoreFromBackup(backupFile)

                result.onSuccess {
                    _uiState.value = BackupRestoreUiState.RestoreSuccess
                    _event.emit(BackupRestoreEvent.ShowSnackbar("Backup restored successfully"))
                    _event.emit(BackupRestoreEvent.RestartApp)
                    Timber.d("Restore successful")
                }

                result.onFailure { exception ->
                    val errorMsg = exception.message ?: "Unknown error during restore"
                    _uiState.value = BackupRestoreUiState.RestoreError(errorMsg)
                    _event.emit(BackupRestoreEvent.ShowSnackbar("Restore failed: $errorMsg"))
                    Timber.e(exception, "Restore failed")
                }

            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unexpected error"
                _uiState.value = BackupRestoreUiState.RestoreError(errorMsg)
                _event.emit(BackupRestoreEvent.ShowSnackbar("Restore error: $errorMsg"))
                Timber.e(e, "Restore error")
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
     * Resets the UI state back to idle.
     */
    fun resetState() {
        _uiState.value = BackupRestoreUiState.Idle
    }

    /**
     * Resets ALL data including customers, invoices, and all settings.
     * This is a destructive operation that cannot be undone.
     */
    fun resetAllData() {
        viewModelScope.launch {
            try {
                Timber.w("🚨 CRITICAL: Resetting ALL data — this is irreversible!")
                _uiState.value = BackupRestoreUiState.BackupInProgress

                invoiceRepository.deleteAllInvoices()
                    .onFailure { e -> Timber.e(e, "Failed to delete invoices") }

                customerRepository.deleteAllCustomers()
                    .onFailure { e -> Timber.e(e, "Failed to delete customers") }

                _uiState.value = BackupRestoreUiState.Idle
                _event.emit(BackupRestoreEvent.ShowSnackbar("✅ All data reset successfully"))
                Timber.w("ALL DATA RESET COMPLETED")

            } catch (e: Exception) {
                val errorMsg = e.message ?: "Failed to reset data"
                _uiState.value = BackupRestoreUiState.BackupError(errorMsg)
                _event.emit(BackupRestoreEvent.ShowSnackbar("Reset failed: $errorMsg"))
                Timber.e(e, "Reset all data failed")
            }
        }
    }

    /**
     * Resets ALL customer data.
     * Associated invoices will be orphaned (no customer reference).
     * This is a destructive operation that cannot be undone.
     */
    fun resetCustomerData() {
        viewModelScope.launch {
            try {
                Timber.w("⚠️ Resetting ALL customer data — invoices will be orphaned!")
                _uiState.value = BackupRestoreUiState.BackupInProgress

                customerRepository.deleteAllCustomers()
                    .onSuccess {
                        _uiState.value = BackupRestoreUiState.Idle
                        _event.emit(BackupRestoreEvent.ShowSnackbar("✅ All customer data reset successfully"))
                        Timber.w("CUSTOMER DATA RESET COMPLETED")
                    }
                    .onFailure { e ->
                        val errorMsg = e.message ?: "Failed to reset customer data"
                        _uiState.value = BackupRestoreUiState.BackupError(errorMsg)
                        _event.emit(BackupRestoreEvent.ShowSnackbar("Reset failed: $errorMsg"))
                        Timber.e(e, "Reset customer data failed")
                    }

            } catch (e: Exception) {
                val errorMsg = e.message ?: "Failed to reset customer data"
                _uiState.value = BackupRestoreUiState.BackupError(errorMsg)
                _event.emit(BackupRestoreEvent.ShowSnackbar("Reset failed: $errorMsg"))
                Timber.e(e, "Reset customer data failed")
            }
        }
    }

    /**
     * Resets ALL invoice data including payments.
     * Customer records remain unchanged.
     * This is a destructive operation that cannot be undone.
     */
    fun resetInvoiceData() {
        viewModelScope.launch {
            try {
                Timber.w("⚠️ Resetting ALL invoice data including payments!")
                _uiState.value = BackupRestoreUiState.BackupInProgress

                invoiceRepository.deleteAllInvoices()
                    .onSuccess {
                        _uiState.value = BackupRestoreUiState.Idle
                        _event.emit(BackupRestoreEvent.ShowSnackbar("✅ All invoice data reset successfully"))
                        Timber.w("INVOICE DATA RESET COMPLETED")
                    }
                    .onFailure { e ->
                        val errorMsg = e.message ?: "Failed to reset invoice data"
                        _uiState.value = BackupRestoreUiState.BackupError(errorMsg)
                        _event.emit(BackupRestoreEvent.ShowSnackbar("Reset failed: $errorMsg"))
                        Timber.e(e, "Reset invoice data failed")
                    }

            } catch (e: Exception) {
                val errorMsg = e.message ?: "Failed to reset invoice data"
                _uiState.value = BackupRestoreUiState.BackupError(errorMsg)
                _event.emit(BackupRestoreEvent.ShowSnackbar("Reset failed: $errorMsg"))
                Timber.e(e, "Reset invoice data failed")
            }
        }
    }
}
