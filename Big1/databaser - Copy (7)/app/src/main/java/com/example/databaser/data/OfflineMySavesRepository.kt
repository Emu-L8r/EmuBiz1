package com.example.databaser.data

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.databaser.ui.PdfFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class OfflineMySavesRepository : MySavesRepository {
    override fun getSavedPdfFilesStream(context: Context): Flow<List<PdfFile>> = flow {
        val list = withContext(Dispatchers.IO) {
            val result = mutableListOf<PdfFile>()
            try {
                val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Files.getContentUri("external")
                }

                val projection = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DATE_MODIFIED,
                    MediaStore.Files.FileColumns.MIME_TYPE
                )

                val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
                val selectionArgs = arrayOf("application/pdf")

                context.contentResolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol)
                        val name = cursor.getString(nameCol) ?: ""
                        val date = cursor.getLong(dateCol)
                        val contentUri = Uri.withAppendedPath(collection, id.toString())
                        result.add(PdfFile(name, date, contentUri.toString()))
                    }
                }
            } catch (t: Throwable) {
                // Fallback to previous direct file scanning (best-effort)
                val downloadsDir = context.getExternalFilesDir(null)?.parentFile?.parentFile?.parentFile?.parentFile?.resolve("Download")
                val files = downloadsDir?.listFiles { file -> file.name.endsWith(".pdf", ignoreCase = true) }?.map { PdfFile(it.name, it.lastModified(), it.absolutePath) }?.toList() ?: emptyList()
                return@withContext files
            }
            result
        }
        emit(list)
    }
}
