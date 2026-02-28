package com.emu.emubizwax.ui.invoices

sealed interface PdfGenerationUiState {
    object Idle : PdfGenerationUiState
    object Loading : PdfGenerationUiState
    data class Error(val message: String) : PdfGenerationUiState
}
