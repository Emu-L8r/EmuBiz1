package com.emul8r.bizap.data.service

import android.graphics.Color
import android.graphics.Typeface
import com.emul8r.bizap.ui.templates.TemplateSnapshot
import timber.log.Timber

/**
 * Extracts and applies styling from template snapshot to PDF
 */
class PdfStyler {

    companion object {
        private const val TAG = "PdfStyler"
    }

    /**
     * Extract colors from template snapshot
     */
    fun extractColors(snapshot: TemplateSnapshot?): PdfColors {
        if (snapshot == null) {
            // Return default colors
            return PdfColors(
                primary = Color.parseColor("#6750A4"),
                secondary = Color.parseColor("#FAFAFA"),
                text = Color.BLACK,
                textLight = Color.DKGRAY
            )
        }

        return try {
            PdfColors(
                primary = Color.parseColor(snapshot.primaryColor),
                secondary = Color.parseColor(snapshot.secondaryColor),
                text = Color.BLACK,
                textLight = Color.DKGRAY
            )
        } catch (e: Exception) {
            Timber.e(e, "Error parsing colors from snapshot")
            // Return defaults on error
            PdfColors(
                primary = Color.parseColor("#6750A4"),
                secondary = Color.parseColor("#FAFAFA"),
                text = Color.BLACK,
                textLight = Color.DKGRAY
            )
        }
    }

    /**
     * Get typeface based on font family
     */
    fun getTypeface(fontFamily: String?, context: android.content.Context, isBold: Boolean): Typeface {
        return try {
            val fontPath = when (fontFamily) {
                "SERIF" -> "fonts/Serif.ttf"
                "SANS_SERIF", null -> "fonts/Roboto-Regular.ttf"
                else -> "fonts/Roboto-Regular.ttf"
            }

            val boldPath = when (fontFamily) {
                "SERIF" -> "fonts/Serif-Bold.ttf"
                "SANS_SERIF", null -> "fonts/Roboto-Bold.ttf"
                else -> "fonts/Roboto-Bold.ttf"
            }

            val assetPath = if (isBold) boldPath else fontPath
            Typeface.createFromAsset(context.assets, assetPath)
        } catch (e: Exception) {
            Timber.w(e, "Could not load font: $fontFamily, using default")
            if (isBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
    }

    /**
     * Check if line items should be hidden
     */
    fun shouldHideLineItems(snapshot: TemplateSnapshot?): Boolean {
        return snapshot?.hideLineItems ?: false
    }

    /**
     * Check if payment terms should be hidden
     */
    fun shouldHidePaymentTerms(snapshot: TemplateSnapshot?): Boolean {
        return snapshot?.hidePaymentTerms ?: false
    }

    /**
     * Get company info from snapshot
     */
    fun getCompanyInfo(snapshot: TemplateSnapshot?): CompanyInfo {
        if (snapshot == null) {
            return CompanyInfo(
                name = "",
                address = "",
                phone = "",
                email = "",
                taxId = null,
                bankDetails = null
            )
        }

        return CompanyInfo(
            name = snapshot.companyName,
            address = snapshot.companyAddress,
            phone = snapshot.companyPhone,
            email = snapshot.companyEmail,
            taxId = snapshot.taxId,
            bankDetails = snapshot.bankDetails
        )
    }

    /**
     * Get logo filename from snapshot
     */
    fun getLogoFileName(snapshot: TemplateSnapshot?): String? {
        return snapshot?.logoFileName
    }
}

/**
 * PDF color scheme
 */
data class PdfColors(
    val primary: Int,
    val secondary: Int,
    val text: Int,
    val textLight: Int
)

/**
 * Company information from template
 */
data class CompanyInfo(
    val name: String,
    val address: String,
    val phone: String,
    val email: String,
    val taxId: String?,
    val bankDetails: String?
)

