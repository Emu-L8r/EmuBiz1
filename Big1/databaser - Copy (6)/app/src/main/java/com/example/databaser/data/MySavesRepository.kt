package com.example.databaser.data

import android.content.Context
import com.example.databaser.ui.PdfFile
import kotlinx.coroutines.flow.Flow

interface MySavesRepository {
    fun getSavedPdfFilesStream(context: Context): Flow<List<PdfFile>>
}
