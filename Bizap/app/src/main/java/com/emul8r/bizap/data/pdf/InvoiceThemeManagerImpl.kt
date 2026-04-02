package com.emul8r.bizap.data.pdf

import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.pdf.InvoiceThemeRenderer
import com.emul8r.bizap.domain.pdf.InvoiceThemeManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of InvoiceThemeManager.
 *
 * Factory pattern for managing and selecting invoice theme renderers.
 * Handles theme selection logic and provides list of available themes.
 */
@Singleton
class InvoiceThemeManagerImpl @Inject constructor(
    private val canvasTheme: CanvasInvoiceTheme,
    private val htmlPdfTheme: HtmlPdfInvoiceTheme
) : InvoiceThemeManager {

    /**
     * Get the appropriate theme renderer for the given theme type.
     *
     * @param theme The theme type to retrieve
     * @return The corresponding theme renderer implementation
     */
    override fun getTheme(theme: InvoiceTheme): InvoiceThemeRenderer {
        return when (theme) {
            InvoiceTheme.CANVAS -> canvasTheme
            InvoiceTheme.HTML_PDF -> htmlPdfTheme
        }
    }

    /**
     * List all available themes.
     *
     * @return List of available invoice themes
     */
    override fun listAvailableThemes(): List<InvoiceTheme> {
        return listOf(
            InvoiceTheme.CANVAS,
            InvoiceTheme.HTML_PDF
        )
    }
}

