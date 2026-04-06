package com.emul8r.bizap.data.service.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import timber.log.Timber

/**
 * Renders QR codes on PDFs for payment references.
 *
 * Uses the ZXing library to generate QR codes from payment data.
 * QR codes are placed in the "Payment Details" section for easy scanning.
 */
class PdfQrCodeRenderer(
    private val canvas: Canvas,
    private val pageWidth: Float = 595f
) {

    companion object {
        private const val TAG = "PdfQrCodeRenderer"
        private const val QR_CODE_SIZE = 80  // pixels
        private const val QR_X = 450f        // Position: top-right of payment section
        private const val QR_Y = 200f
    }

    /**
     * Draws a QR code for the given payment reference.
     *
     * @param paymentReference Text to encode in QR code (e.g., "INV-2026-000123")
     * @return True if QR code was successfully drawn; false if encoding failed
     */
    fun drawPaymentQrCode(paymentReference: String): Boolean {
        if (paymentReference.isBlank()) {
            Timber.w("$TAG: Empty payment reference, skipping QR code")
            return false
        }

        return try {
            val bitmap = generateQrBitmap(paymentReference) ?: return false
            canvas.drawBitmap(bitmap, QR_X, QR_Y, null)
            Timber.d("$TAG: QR code rendered for: $paymentReference")
            true
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Error generating QR code")
            false
        }
    }

    /**
     * Draws a QR code linking to a payment portal.
     *
     * @param paymentUrl URL to encode (e.g., "https://pay.example.com/invoice/123")
     * @return True if QR code was successfully drawn; false if encoding failed
     */
    fun drawPaymentUrl(paymentUrl: String): Boolean {
        if (paymentUrl.isBlank() || !paymentUrl.startsWith("http")) {
            Timber.w("$TAG: Invalid payment URL, skipping QR code")
            return false
        }

        return try {
            val bitmap = generateQrBitmap(paymentUrl) ?: return false
            canvas.drawBitmap(bitmap, QR_X, QR_Y, null)
            Timber.d("$TAG: Payment URL QR code rendered for: $paymentUrl")
            true
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Error generating payment URL QR code")
            false
        }
    }

    /**
     * Generates a [Bitmap] containing a QR code for [content].
     *
     * @return Bitmap or null if encoding fails
     */
    private fun generateQrBitmap(content: String): Bitmap? {
        return try {
            val hints = mapOf(EncodeHintType.MARGIN to 1)
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints)

            val bitmap = Bitmap.createBitmap(QR_CODE_SIZE, QR_CODE_SIZE, Bitmap.Config.ARGB_8888)
            for (x in 0 until QR_CODE_SIZE) {
                for (y in 0 until QR_CODE_SIZE) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Failed to generate QR bitmap for content: $content")
            null
        }
    }
}

