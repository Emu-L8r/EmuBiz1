package com.example.databaser.data

import android.content.Context
import com.example.databaser.ui.PdfFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

class OfflineMySavesRepository @Inject constructor(private val context: Context) : MySavesRepository {
    override fun getSavedPdfFilesStream(): Flow<List<PdfFile>> = flow {
        val downloadsDir = context.getExternalFilesDir(null)?.parentFile?.parentFile?.parentFile?.parentFile?.resolve("Download")
        val files = downloadsDir?.listFiles { file -> file.name.endsWith(".pdf") }?.map { PdfFile(it.name, it.lastModified(), it.absolutePath) }?.toList() ?: emptyList()
        emit(files)
    }.flowOn(Dispatchers.IO)
}
