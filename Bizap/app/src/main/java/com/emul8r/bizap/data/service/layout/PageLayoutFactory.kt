package com.emul8r.bizap.data.service.layout

import com.emul8r.bizap.domain.model.ClassicPageLayout
import com.emul8r.bizap.domain.model.CompactPageLayout
import com.emul8r.bizap.domain.model.InvoiceColorScheme
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.ModernPageLayout
import com.emul8r.bizap.domain.model.SpaciousPageLayout
import com.emul8r.bizap.domain.model.PageLayout
import com.emul8r.bizap.domain.model.PageLayoutProvider
import timber.log.Timber

/**
 * Factory for creating page layout providers based on PageLayout enum.
 *
 * Routes invoice rendering to the correct layout implementation.
 * Each layout defines how content is organized on the page.
 *
 * Supported layouts (4 full implementations):
 * - CLASSIC: Traditional layout with full spacing
 * - MODERN: Compact grid-based layout
 * - SPACIOUS: Premium spacious layout with generous spacing
 * - COMPACT: Minimal spacing, maximum content density
 *
 * Stub layouts (currently use ClassicPageLayout as fallback):
 * - SIDEBAR: Reserved for future sidebar layout implementation
 * - CARDS: Reserved for future card-grid layout implementation
 * - MINIMAL_TABLES: Reserved for future table-only layout
 * - FOCUSED: Reserved for future focus mode (single-item view)
 * - ADVANCED_PAGINATED: Used with AdvancedPageLayout in PDF generation
 *
 * NOTE: Users can select any layout, but stubs will display as CLASSIC until fully implemented.
 * To implement a stub: create the PageLayoutProvider class and add to createLayout() below.
 */
object PageLayoutFactory {
    fun createLayout(layout: PageLayout): PageLayoutProvider {
        return when (layout) {
            PageLayout.CLASSIC -> ClassicPageLayout()
            PageLayout.MODERN -> ModernPageLayout()
            PageLayout.SPACIOUS -> SpaciousPageLayout()
            PageLayout.COMPACT -> CompactPageLayout()
            // ⚠️ STUB LAYOUTS (Phase 4.0 implementation targets)
            // These layouts currently fallback to ClassicPageLayout.
            // When implementing, create the corresponding PageLayoutProvider class
            // and replace the fallback below with the new implementation.
            PageLayout.SIDEBAR -> ClassicPageLayout()  // TODO: Implement SidebarPageLayout()
            PageLayout.CARDS -> ClassicPageLayout()    // TODO: Implement CardsPageLayout()
            PageLayout.MINIMAL_TABLES -> ClassicPageLayout()  // TODO: Implement MinimalTablesPageLayout()
            PageLayout.FOCUSED -> ClassicPageLayout()  // TODO: Implement FocusedPageLayout()
            PageLayout.ADVANCED_PAGINATED -> ClassicPageLayout()  // Uses AdvancedPageLayout in PDF generation
        }
    }

    fun getLayoutName(layout: PageLayout): String = when (layout) {
        PageLayout.CLASSIC -> "CLASSIC"
        PageLayout.MODERN -> "MODERN"
        PageLayout.SPACIOUS -> "SPACIOUS"
        PageLayout.COMPACT -> "COMPACT"
        PageLayout.SIDEBAR -> "SIDEBAR"
        PageLayout.CARDS -> "CARDS"
        PageLayout.MINIMAL_TABLES -> "MINIMAL_TABLES"
        PageLayout.FOCUSED -> "FOCUSED"
        PageLayout.ADVANCED_PAGINATED -> "ADVANCED_PAGINATED"
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
     * Maps the selectedColorScheme enum to the corresponding hex colors.
     * This ensures color scheme changes in the UI are reflected in preview and PDF output.
     */
    fun extractColorScheme(settings: com.emul8r.bizap.domain.model.InvoiceSettings): InvoiceColorScheme {
        // Map ColorScheme enum to hex pair (primary + accent)
        val (primaryHex, accentHex) = when (settings.selectedColorScheme) {
            com.emul8r.bizap.domain.model.ColorScheme.PROFESSIONAL -> "#003366" to "#FFC107"
            com.emul8r.bizap.domain.model.ColorScheme.VIBRANT      -> "#6B4C9A" to "#FF9F43"
            com.emul8r.bizap.domain.model.ColorScheme.MINIMAL      -> "#1a1a1a" to "#666666"
            com.emul8r.bizap.domain.model.ColorScheme.WARM         -> "#D97706" to "#78350F"
            com.emul8r.bizap.domain.model.ColorScheme.TECH         -> "#0F172A" to "#06B6D4"
            com.emul8r.bizap.domain.model.ColorScheme.NATURE       -> "#15803D" to "#92400E"
        }

        return InvoiceColorScheme(
            primaryColor = primaryHex,
            accentColor = accentHex,
            lightBackground = settings.secondaryColor,
            textDark = "#333333",  // Default dark text
            textLight = "#666666",  // Default light text
            borderColor = "#e0e0e0" // Default border
        )
    }
}


