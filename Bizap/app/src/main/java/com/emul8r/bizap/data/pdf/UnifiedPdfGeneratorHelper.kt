package com.emul8r.bizap.data.pdf

import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.InvoiceTheme
import timber.log.Timber
import java.io.File

/**
 * Unified PDF Generator Helper
 *
 * This helper encapsulates the logic for creating unified PdfRenderModel objects
 * that work with both Modern (GUI2) and Classic (GUI1) interfaces.
 *
 * Both interfaces use this helper to build PDF render models before passing
 * them to the actual PDF generation service.
 *
 * Usage:
 * ```kotlin
 * val helper = UnifiedPdfGeneratorHelper(context, appColorScheme)
 * val renderModel = helper.buildRenderModel(
 *     invoiceData = snapshot,
 *     isQuote = false,
 *     header = HeaderSection("Bob's Hardware"),
 *     subheader = SubheaderSection.from("Store #123", "Department: Tools", "Location: Downtown")
 * )
 * val (invoicePdf, quotePdf) = pdfService.generateDualPdf(
 *     snapshot = renderModel.invoiceData,
 *     header = renderModel.header,
 *     subheader = renderModel.subheader,
 *     colorScheme = renderModel.colors
 * )
 * ```
 */
class UnifiedPdfGeneratorHelper(
    private val context: android.content.Context,
    private val appColorScheme: androidx.compose.material3.ColorScheme? = null
) {
    companion object {
        private const val TAG = "UnifiedPdfGeneratorHelper"
    }

    private val themeAdapter = PdfThemeAdapter(context, appColorScheme)

    /**
     * Build a complete, unified PDF render model
     *
     * This model can be used to generate both Invoice and Quote PDFs
     */
    fun buildRenderModel(
        invoiceData: InvoiceSnapshot,
        isQuote: Boolean = false,
        header: HeaderSection? = null,
        subheader: SubheaderSection = SubheaderSection(),
        customColors: PdfColorScheme? = null,
        layoutMode: LayoutMode = LayoutMode.MODERN
    ): PdfRenderModel {
        return PdfRenderModel(
            documentType = if (isQuote) DocumentType.QUOTE else DocumentType.INVOICE,
            documentTitle = if (isQuote) "QUOTE" else "INVOICE",
            header = header,
            subheader = subheader,
            invoiceData = invoiceData,
            pdfTheme = themeAdapter.adaptPdfThemeConfig(),
            colors = customColors ?: themeAdapter.adaptColors(),
            typography = themeAdapter.adaptTypography(),
            spacing = themeAdapter.adaptSpacing(),
            layoutMode = layoutMode
        ).also {
            Timber.d(TAG, """
                ✅ Unified render model built:
                   Document: ${it.documentTitle}
                   Header: ${it.header?.text ?: "None"}
                   Subheader lines: ${it.subheader.lines.size}
                   Layout: ${it.layoutMode}
                   Colors: App theme synced
            """.trimIndent())
        }
    }

    /**
     * Build model for Modern (GUI2) interface
     */
    fun buildModernInterfaceModel(
        invoiceData: InvoiceSnapshot,
        userHeader: String? = null,
        userSubheaderLines: List<String> = emptyList()
    ): PdfRenderModel {
        return buildRenderModel(
            invoiceData = invoiceData,
            isQuote = false,
            header = if (userHeader != null) HeaderSection(userHeader) else null,
            subheader = SubheaderSection(userSubheaderLines),
            layoutMode = LayoutMode.MODERN
        )
    }

    /**
     * Build model for Classic (GUI1) interface
     *
     * Classic interface uses COMPACT layout for tighter spacing
     */
    fun buildClassicInterfaceModel(
        invoiceData: InvoiceSnapshot,
        userHeader: String? = null,
        userSubheaderLines: List<String> = emptyList()
    ): PdfRenderModel {
        return buildRenderModel(
            invoiceData = invoiceData,
            isQuote = false,
            header = if (userHeader != null) HeaderSection(userHeader) else null,
            subheader = SubheaderSection(userSubheaderLines),
            layoutMode = LayoutMode.COMPACT  // Compact for classic interface
        )
    }

    /**
     * Validate render model before PDF generation
     */
    fun validateRenderModel(model: PdfRenderModel): Boolean {
        return try {
            // Validate colors
            if (!themeAdapter.validateTheme(model.colors, model.spacing)) {
                Timber.w(TAG, "⚠️ Theme validation failed")
                return false
            }

            // Validate layout manager
            val layoutManager = PdfLayoutManager(model.spacing)
            val positions = layoutManager.calculateSectionPositions(
                hasHeader = model.header != null,
                subheaderLineCount = model.subheader.lines.size
            )

            if (!positions.isSafe()) {
                Timber.w(TAG, "⚠️ Layout will not fit safely on page")
                return false
            }

            Timber.d(TAG, "✅ Render model validated successfully")
            true
        } catch (e: Exception) {
            Timber.e(e, TAG, "Error validating render model")
            false
        }
    }

    /**
     * Get layout manager for this model
     */
    fun getLayoutManager(model: PdfRenderModel): PdfLayoutManager {
        return PdfLayoutManager(model.spacing)
    }

    /**
     * Get theme adapter (for accessing typography, colors, etc.)
     */
    fun getThemeAdapter(): PdfThemeAdapter {
        return themeAdapter
    }
}

/**
 * Extension function for easy model building
 */
fun InvoiceSnapshot.toPdfRenderModel(
    context: android.content.Context,
    isQuote: Boolean = false,
    header: HeaderSection? = null,
    subheader: SubheaderSection = SubheaderSection(),
    layoutMode: LayoutMode = LayoutMode.MODERN
): PdfRenderModel {
    val helper = UnifiedPdfGeneratorHelper(context)
    return helper.buildRenderModel(
        invoiceData = this,
        isQuote = isQuote,
        header = header,
        subheader = subheader,
        layoutMode = layoutMode
    )
}

