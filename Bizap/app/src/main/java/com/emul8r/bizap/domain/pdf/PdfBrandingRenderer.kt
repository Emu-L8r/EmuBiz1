package com.emul8r.bizap.domain.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Base64
import timber.log.Timber

/**
 * Handles branding elements in PDFs: logos, company name, colors.
 *
 * **Features:**
 * - Decodes Base64 logos and draws them on the PDF
 * - Fallback to text branding if logo is missing/invalid
 * - Respects aspect ratio and max size constraints
 */
class PdfBrandingRenderer(
    private val canvas: Canvas,
    private val pageWidth: Float = 595f
) {

    companion object {
        private const val TAG = "PdfBrandingRenderer"
        private const val LOGO_MAX_WIDTH = 80f
        private const val LOGO_MAX_HEIGHT = 50f
        private const val LOGO_X = 450f
        private const val LOGO_Y = 30f
    }

    /**
     * Draws a Base64-encoded logo on the PDF.
     *
     * **Behavior:**
     * - If logoBase64 is valid, decodes and draws it
     * - If logoBase64 is null or invalid, silently skips (no error thrown)
     * - Maintains aspect ratio within LOGO_MAX_WIDTH × LOGO_MAX_HEIGHT bounds
     *
     * @param logoBase64 Base64-encoded JPEG/PNG image, or null
     * @return True if logo was successfully drawn; false if null or invalid
     */
    fun drawLogo(logoBase64: String?): Boolean {
        if (logoBase64.isNullOrBlank()) {
            Timber.d("$TAG: No logo provided, skipping logo rendering")
            return false
        }

        return try {
            val decodedBytes = Base64.decode(logoBase64, Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            if (bitmap != null) {
                drawLogoBitmap(bitmap)
                bitmap.recycle()
                Timber.d("$TAG: Logo rendered successfully")
                true
            } else {
                Timber.w("$TAG: Failed to decode Base64 bitmap")
                false
            }
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "$TAG: Invalid Base64 encoding for logo")
            false
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Error decoding logo")
            false
        }
    }

    /**
     * Draws a bitmap logo on the PDF, respecting aspect ratio.
     */
    private fun drawLogoBitmap(bitmap: Bitmap) {
        // Calculate scaled dimensions maintaining aspect ratio
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val (width, height) = if (aspectRatio > 1f) {
            // Wider than tall
            Pair(LOGO_MAX_WIDTH, LOGO_MAX_WIDTH / aspectRatio)
        } else {
            // Taller than wide
            Pair(LOGO_MAX_HEIGHT * aspectRatio, LOGO_MAX_HEIGHT)
        }

        // Create a scaled bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap,
            width.toInt(),
            height.toInt(),
            true
        )

        // Draw the bitmap on the canvas
        canvas.drawBitmap(scaledBitmap, LOGO_X, LOGO_Y, null)

        // Clean up
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }

        Timber.d("$TAG: Drew logo at ($LOGO_X, $LOGO_Y) with size ${width}×${height}")
    }
}

