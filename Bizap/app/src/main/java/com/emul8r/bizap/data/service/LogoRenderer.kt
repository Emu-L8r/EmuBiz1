package com.emul8r.bizap.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import java.io.File
import timber.log.Timber

/**
 * Renders logo image in PDF invoice header
 */
class LogoRenderer(
    private val context: Context,
    private val canvas: Canvas
) {

    companion object {
        private const val TAG = "LogoRenderer"
        private const val LOGO_SIZE = 100f
        private const val LOGO_MARGIN_TOP = 20f
        private const val LOGO_MARGIN_RIGHT = 40f
        private const val PAGE_WIDTH = 595f
    }

    /**
     * Render logo in PDF header (top-right)
     */
    fun renderLogo(logoFileName: String?): Boolean {
        if (logoFileName.isNullOrBlank()) {
            Timber.d("No logo filename provided")
            return false
        }

        return try {
            val logoFile = File(context.cacheDir, "logos/$logoFileName")

            if (!logoFile.exists()) {
                Timber.w("⚠️ Logo file not found: ${logoFile.absolutePath}")
                return false
            }

            // Load and decode bitmap
            val bitmap = BitmapFactory.decodeFile(logoFile.absolutePath)
            if (bitmap == null) {
                Timber.w("⚠️ Could not decode logo bitmap: $logoFileName")
                return false
            }

            // Calculate position (top-right)
            val logoX = PAGE_WIDTH - LOGO_SIZE - LOGO_MARGIN_RIGHT
            val logoY = LOGO_MARGIN_TOP

            // Draw logo with border
            drawLogoBorder(logoX, logoY)

            // Scale and draw bitmap
            val scaledBitmap = Bitmap.createScaledBitmap(
                bitmap,
                LOGO_SIZE.toInt(),
                LOGO_SIZE.toInt(),
                true
            )
            canvas.drawBitmap(scaledBitmap, logoX, logoY, null)

            Timber.d("✅ Logo rendered successfully: $logoFileName")
            true
        } catch (e: Exception) {
            Timber.e(e, "❌ Error rendering logo: $logoFileName")
            false
        }
    }

    /**
     * Draw border around logo
     */
    private fun drawLogoBorder(x: Float, y: Float) {
        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = android.graphics.Color.LTGRAY
            isAntiAlias = true
        }

        canvas.drawRect(x, y, x + LOGO_SIZE, y + LOGO_SIZE, borderPaint)
    }

    /**
     * Check if logo file exists
     */
    fun logoExists(logoFileName: String?): Boolean {
        if (logoFileName.isNullOrBlank()) return false

        return try {
            val logoFile = File(context.cacheDir, "logos/$logoFileName")
            logoFile.exists() && logoFile.isFile
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get logo dimensions (for layout purposes)
     */
    fun getLogoDimensions(logoFileName: String?): Pair<Int, Int>? {
        if (logoFileName.isNullOrBlank()) return null

        return try {
            val logoFile = File(context.cacheDir, "logos/$logoFileName")
            if (!logoFile.exists()) return null

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(logoFile.absolutePath, options)

            Pair(options.outWidth, options.outHeight)
        } catch (e: Exception) {
            Timber.w(e, "Could not get logo dimensions: $logoFileName")
            null
        }
    }
}

