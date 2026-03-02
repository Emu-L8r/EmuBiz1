package com.emul8r.bizap.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.emul8r.bizap.utils.DocumentNamingUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun archiveToInternalStorage(sourceFile: File, invoiceId: Long): File {
        val internalDir = File(context.filesDir, "documents")
        if (!internalDir.exists()) {
            internalDir.mkdirs()
        }
        val newFile = File(internalDir, sourceFile.name)
        sourceFile.copyTo(newFile, true)
        sourceFile.delete()
        return newFile
    }

    /**
     * Copies a file from internal storage to the public "Downloads" directory using MediaStore.
     *
     * @param sourceFile The file in internal storage to copy.
     * @param displayName The desired display name for the file in the Downloads directory.
     * @return The MediaStore Uri of the newly created file, or null if the operation failed.
     */
    fun saveToDownloads(sourceFile: File, displayName: String): String? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/Bizap")
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let { destinationUri ->
            try {
                resolver.openOutputStream(destinationUri)?.use { outputStream ->
                    sourceFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                return destinationUri.toString()
            } catch (e: Exception) {
                // Clean up the created entry if the copy fails
                resolver.delete(destinationUri, null, null)
            }
        }
        return null
    }
}

