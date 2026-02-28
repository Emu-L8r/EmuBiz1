package com.example.databaser.data

import com.example.databaser.ui.PdfFile
import kotlinx.coroutines.flow.Flow

interface MySavesRepository {
    fun getSavedPdfFilesStream(): Flow<List<PdfFile>>
}
