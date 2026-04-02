package com.emul8r.bizap.data.pdf

import android.content.Context
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.pdf.InvoiceThemeRenderer
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Invoice Theme Manager & Factory
 *
 * Responsible for:
 * - Selecting the appropriate theme (Canvas or HTML-PDF) based on settings
 * - Creating and managing theme instances
 * - Providing a single entry point for theme-based PDF generation
 *
 * **Architecture:**
 * - Uses InvoiceTheme enum to specify theme preference
 * - Instantiates the correct InvoiceThemeRenderer based on selection
 * - Ensures backward compatibility with Canvas theme by default
 */
@Singleton
class InvoiceThemeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val canvasTheme: CanvasInvoiceTheme,
    private val htmlTheme: HtmlPdfInvoiceTheme
) {

    /**
     * Get the appropriate theme renderer based on theme preference.
     *
     * **Selection Logic:**
     * - If settings.theme == HTML_PDF → return HtmlPdfInvoiceTheme
     * - Otherwise → return CanvasInvoiceTheme (default/fallback)
     *
     * @param theme The theme enum to render as
     * @return The appropriate InvoiceThemeRenderer instance
     */
    fun getThemeRenderer(theme: InvoiceTheme): InvoiceThemeRenderer {
        return when (theme) {
            InvoiceTheme.HTML_PDF -> {
                Timber.d("Using HTML-to-PDF theme for invoice generation")
                htmlTheme
            }
            InvoiceTheme.CANVAS -> {
                Timber.d("Using Canvas theme for invoice generation")
                canvasTheme
            }
        }
    }

    /**
     * Get theme renderer from InvoiceSettings.
     *
     * Convenience method that extracts theme from settings and selects renderer.
     *
     * @param settings Invoice settings containing theme preference
     * @return The appropriate InvoiceThemeRenderer instance
     */
    fun getThemeRenderer(settings: InvoiceSettings): InvoiceThemeRenderer {
        return getThemeRenderer(settings.selectedTheme)
    }

    /**
     * Get all available themes.
     *
     * Useful for UI dropdowns and theme selection interfaces.
     *
     * @return List of all available InvoiceTheme values
     */
    fun getAvailableThemes(): List<InvoiceTheme> {
        return InvoiceTheme.values().toList()
    }

    /**
     * Get theme information for display.
     *
     * @param theme The theme to get info for
     * @return Pair of (theme name, theme description)
     */
    fun getThemeInfo(theme: InvoiceTheme): Pair<String, String> {
        val renderer = getThemeRenderer(theme)
        return Pair(renderer.getThemeName(), renderer.getThemeDescription())
    }

    companion object {
        private const val TAG = "InvoiceThemeManager"
    }
}


