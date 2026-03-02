package com.emul8r.bizap.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.data.service.InvoiceCsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface DataUiEvent {
    data class ShowSnackbar(val message: String) : DataUiEvent
    data class ExportCsv(val content: String, val fileName: String) : DataUiEvent
}

@HiltViewModel
class DataSettingsViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val profileRepository: BusinessProfileRepository
) : ViewModel() {

    private val _isExporting = MutableStateFlow(false)
    val isExporting = _isExporting.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DataUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun exportInvoicesToCsv() {
        viewModelScope.launch {
            try {
                _isExporting.value = true
                val invoices = invoiceRepository.getAllInvoicesWithItems().first()
                
                if (invoices.isEmpty()) {
                    _uiEvent.emit(DataUiEvent.ShowSnackbar("No invoices found to export"))
                    return@launch
                }

                val csvContent = InvoiceCsvExporter.generateCsv(invoices)
                val fileName = "invoices_export_${System.currentTimeMillis()}.csv"
                
                _uiEvent.emit(DataUiEvent.ExportCsv(csvContent, fileName))
            } catch (e: Exception) {
                Timber.e(e, "Failed to export invoices")
                _uiEvent.emit(DataUiEvent.ShowSnackbar("Export failed: ${e.message}"))
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun onExportSuccess() {
        viewModelScope.launch {
            _uiEvent.emit(DataUiEvent.ShowSnackbar("Exported successfully"))
        }
    }

    fun onExportError(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(DataUiEvent.ShowSnackbar("Error: $message"))
        }
    }

    fun onFactoryResetClick() {
        viewModelScope.launch {
            _uiEvent.emit(DataUiEvent.ShowSnackbar("Factory Reset is coming soon!"))
        }
    }
}
