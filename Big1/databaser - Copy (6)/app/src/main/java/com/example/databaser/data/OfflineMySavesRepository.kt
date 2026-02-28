package com.example.databaser.data

import android.content.Context
import com.example.databaser.ui.PdfFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class OfflineMySavesRepository : MySavesRepository {
    override fun getSavedPdfFilesStream(context: Context): Flow<List<PdfFile>> = flow {
        val downloadsDir = context.getExternalFilesDir(null)?.parentFile?.parentFile?.parentFile?.parentFile?.resolve("Download")
        val files = downloadsDir?.listFiles { file -> file.name.endsWith(".pdf") }?.map { PdfFile(it.name, it.lastModified(), it.absolutePath) }?.toList() ?: emptyList()
        emit(files)
    }
}
