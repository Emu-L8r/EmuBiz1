package com.emul8r.bizap.data.service

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class DocumentExportService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportToPublicDownloads(internalFile: File, fileName: String): Uri? {
        val resolver = context.contentResolver

        // 1. Define metadata for the new public file
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/EmuBiz")
            // IS_PENDING = 1 tells the OS we are still writing (prevents other apps from reading it)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
        }

        // 2. Insert into MediaStore
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let { targetUri ->
            resolver.openOutputStream(targetUri)?.use { outputStream ->
                internalFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream) // Efficient stream copy
                }
            }

            // 3. Signal that we are finished
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                resolver.update(targetUri, contentValues, null, null)
            }
        }
        return uri
    }
}

