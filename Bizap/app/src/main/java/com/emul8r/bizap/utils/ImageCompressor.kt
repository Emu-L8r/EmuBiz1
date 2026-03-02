package com.emul8r.bizap.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream
import timber.log.Timber

/**
 * Utility object for image compression and Base64 encoding.
 * Optimizes images for storage in DataStore and display in UI/PDFs.
 */
object ImageCompressor {

    /**
     * Converts a URI to a compressed Base64 string.
     *
     * @param context Android context for content resolver
     * @param uri The image URI (from camera or gallery)
     * @param maxWidthPx Maximum width in pixels (default: 400px for ~200dp on xhdpi)
     * @param quality JPEG compression quality (default: 85%)
     * @return Base64-encoded compressed image, or null if compression fails
     */
    fun uriToBase64(
        context: Context,
        uri: Uri,
        maxWidthPx: Int = 400,
        quality: Int = 85
    ): String? {
        return try {
            // Open input stream from URI
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

            if (inputStream == null) {
                Timber.e("Failed to open input stream for URI: $uri")
                return null
            }

            // Decode bitmap with inJustDecodeBounds first to get dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculate inSampleSize for downsampling
            val originalWidth = options.outWidth
            val originalHeight = options.outHeight
            val sampleSize = calculateInSampleSize(originalWidth, originalHeight, maxWidthPx)

            // Decode actual bitmap with downsampling
            val inputStream2 = context.contentResolver.openInputStream(uri)
            val sampledOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            var bitmap = BitmapFactory.decodeStream(inputStream2, null, sampledOptions)
            inputStream2?.close()

            if (bitmap == null) {
                Timber.e("Failed to decode bitmap from URI: $uri")
                return null
            }

            // Scale down further if still too large
            if (bitmap.width > maxWidthPx) {
                val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
                val targetHeight = (maxWidthPx * aspectRatio).toInt()
                bitmap = Bitmap.createScaledBitmap(bitmap, maxWidthPx, targetHeight, true)
            }

            // Compress to JPEG and convert to Base64
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val byteArray = outputStream.toByteArray()
            bitmap.recycle()

            val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            // Log size for debugging
            val sizeKB = byteArray.size / 1024
            Timber.d("Compressed image to $sizeKB KB (Base64 length: ${base64.length})")

            base64
        } catch (e: Exception) {
            Timber.e(e, "Error compressing image")
            null
        }
    }

    /**
     * Decodes Base64 string back to Bitmap for display.
     *
     * @param base64 The Base64-encoded image string
     * @return Decoded Bitmap, or null if decoding fails
     */
    fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Timber.e(e, "Error decoding Base64 to bitmap")
            null
        }
    }

    /**
     * Calculates appropriate inSampleSize for bitmap downsampling.
     */
    private fun calculateInSampleSize(width: Int, height: Int, maxWidth: Int): Int {
        var sampleSize = 1
        while (width / sampleSize > maxWidth || height / sampleSize > maxWidth) {
            sampleSize *= 2
        }
        return sampleSize
    }
}




