package com.emul8r.bizap.ui.templates

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import android.util.Log

/**
 * Handles logo upload, compression, and caching
 */
class LogoUploadHandler(private val context: Context) {

    companion object {
        private const val TAG = "LogoUploadHandler"
        private const val MAX_FILE_SIZE = 2 * 1024 * 1024 // 2MB
        private const val MAX_WIDTH = 1080
        private const val MAX_HEIGHT = 720
        private const val COMPRESSION_QUALITY = 85
        private const val LOGOS_DIR = "logos"
    }

    /**
     * Process and save logo from URI
     * Returns filename if successful, null if error
     */
    suspend fun uploadLogo(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Validate file size
            val fileSize = getFileSizeFromUri(uri)
            if (fileSize > MAX_FILE_SIZE) {
                Log.e(TAG, "File too large: $fileSize bytes")
                return@withContext Result.failure(
                    IllegalArgumentException("File size exceeds 2MB limit")
                )
            }

            // Read bitmap from URI
            val bitmap = BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(uri)
            ) ?: return@withContext Result.failure(
                IllegalArgumentException("Could not read image file")
            )

            // Compress bitmap if needed
            val compressed = compressBitmap(bitmap)

            // Generate filename
            val filename = "${UUID.randomUUID()}.png"

            // Save to cache directory
            val logoFile = File(getLogosDir(), filename)
            FileOutputStream(logoFile).use { output ->
                compressed.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, output)
                output.flush()
            }

            Log.d(TAG, "✅ Logo saved: $filename (${logoFile.length()} bytes)")
            Result.success(filename)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error uploading logo", e)
            Result.failure(e)
        }
    }

    /**
     * Get logo file from cache
     */
    fun getLogoFile(filename: String): File {
        return File(getLogosDir(), filename)
    }

    /**
     * Delete logo from cache
     */
    fun deleteLogo(filename: String): Boolean {
        return try {
            val file = File(getLogosDir(), filename)
            file.delete().also {
                if (it) Log.d(TAG, "✅ Logo deleted: $filename")
                else Log.w(TAG, "⚠️ Could not delete logo: $filename")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting logo", e)
            false
        }
    }

    /**
     * Compress bitmap to max dimensions
     */
    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Check if resizing is needed
        if (width <= MAX_WIDTH && height <= MAX_HEIGHT) {
            return bitmap
        }

        // Calculate scaling
        val scale = minOf(
            MAX_WIDTH.toFloat() / width,
            MAX_HEIGHT.toFloat() / height
        )

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        Log.d(TAG, "Resizing bitmap: $width x $height → $newWidth x $newHeight")
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Get file size from URI
     */
    private fun getFileSizeFromUri(uri: Uri): Long {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(
                    android.provider.OpenableColumns.SIZE
                )
                cursor.moveToFirst()
                cursor.getLong(sizeIndex)
            } ?: 0L
        } catch (e: Exception) {
            Log.w(TAG, "Could not get file size from URI", e)
            0L
        }
    }

    /**
     * Get logos directory in cache
     */
    private fun getLogosDir(): File {
        return File(context.cacheDir, LOGOS_DIR).apply {
            if (!exists()) {
                mkdir()
            }
        }
    }

    /**
     * Clear all logos from cache
     */
    fun clearAllLogos(): Boolean {
        return try {
            getLogosDir().deleteRecursively().also {
                if (it) Log.d(TAG, "✅ All logos cleared")
                else Log.w(TAG, "⚠️ Could not clear logos directory")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error clearing logos", e)
            false
        }
    }

    /**
     * Get total cache size for logos
     */
    fun getTotalLogosCacheSize(): Long {
        return try {
            getLogosDir().walkTopDown()
                .filter { it.isFile }
                .sumOf { it.length() }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating cache size", e)
            0L
        }
    }
}

