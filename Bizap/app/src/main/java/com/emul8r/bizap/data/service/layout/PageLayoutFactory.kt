package com.emul8r.bizap.data.service.layout

import com.emul8r.bizap.domain.model.ClassicPageLayout
import com.emul8r.bizap.domain.model.InvoiceColorScheme
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.ModernPageLayout
import com.emul8r.bizap.domain.model.PageLayout
import com.emul8r.bizap.domain.model.PageLayoutProvider
import timber.log.Timber

/**
 * Factory for creating page layout providers based on PageLayout enum.
 *
 * Routes invoice rendering to the correct layout implementation.
 * Each layout defines how content is organized on the page.
 */
object PageLayoutFactory {
    fun createLayout(layout: PageLayout): PageLayoutProvider {
        return when (layout) {
            PageLayout.CLASSIC -> ClassicPageLayout()
            PageLayout.MODERN -> ModernPageLayout()
        }
    }

    fun getLayoutName(layout: PageLayout): String = when (layout) {
        PageLayout.CLASSIC -> "CLASSIC"
        PageLayout.MODERN -> "MODERN"
    }
}

/**
 * Page Layout Manager
 *
 * Coordinates between layout selection and HTML generation.
 * Handles:
 * - Layout selection based on user settings
 * - Color scheme extraction from InvoiceSettings
 * - HTML generation with the appropriate layout
 */
class PageLayoutManager {
    fun generateInvoiceHtmlWithLayout(
        snapshot: InvoiceSnapshot,
        layout: PageLayout,
        colorScheme: InvoiceColorScheme,
        isQuote: Boolean = false
    ): String {
        Timber.d("📐 Generating invoice with layout: ${PageLayoutFactory.getLayoutName(layout)}")

        val layoutProvider = PageLayoutFactory.createLayout(layout)
        return layoutProvider.buildInvoiceHtml(snapshot, isQuote, colorScheme)
    }

    /**
     * Extract color scheme from InvoiceSettings for use in layouts.
     */
    fun extractColorScheme(settings: com.emul8r.bizap.domain.model.InvoiceSettings): InvoiceColorScheme {
        return InvoiceColorScheme(
            primaryColor = settings.primaryColor,
            accentColor = settings.accentColor,
            lightBackground = settings.secondaryColor,
            textDark = "#333333",  // Default dark text
            textLight = "#666666",  // Default light text
            borderColor = "#e0e0e0" // Default border
        )
    }
}


