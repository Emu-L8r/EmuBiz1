package com.emul8r.bizap.data.service.pdf_services

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.PageLayout
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 3D: Layout Selector
 *
 * Implements smart layout selection with auto-upgrade logic.
 * Ensures invoices don't overflow by automatically selecting
 * appropriate layout based on item count and user preference.
 *
 * Features:
 * - User preference-based selection
 * - Auto-upgrade to multi-page when needed
 * - Performance-aware selection
 * - Detailed logging for debugging
 */
@Singleton
class LayoutSelector @Inject constructor() {

    /**
     * Select the optimal layout for an invoice.
     *
     * Selection priority:
     * 1. If user explicitly selected ADVANCED_PAGINATED, use it
     * 2. If item count exceeds layout capacity and auto-upgrade enabled, upgrade to ADVANCED_PAGINATED
     * 3. Use user's preferred layout
     *
     * @param invoice The invoice to select layout for
     * @param userPreference User's preferred page layout
     * @param autoUpgradeEnabled Whether to auto-upgrade if items exceed capacity
     * @return The selected PageLayout
     */
    fun selectLayout(
        invoice: Invoice,
        userPreference: PageLayout = PageLayout.MODERN,
        autoUpgradeEnabled: Boolean = true
    ): PageLayout {
        return try {
            val itemCount = invoice.items?.size ?: 0
            val maxForUser = userPreference.maxItemsPerPage

            Timber.d(
                "Layout selection: invoice has $itemCount items, " +
                "user preference=${userPreference.displayName} (max $maxForUser/page)"
            )

            // Rule 1: User explicitly chose ADVANCED_PAGINATED
            if (userPreference == PageLayout.ADVANCED_PAGINATED) {
                Timber.d("✅ Using user-selected layout: ADVANCED_PAGINATED")
                return userPreference
            }

            // Rule 2: Auto-upgrade if item count exceeds capacity
            if (autoUpgradeEnabled && itemCount > maxForUser) {
                Timber.w(
                    "⚠️ Invoice has $itemCount items, exceeds $maxForUser for ${userPreference.displayName}. " +
                    "Auto-upgrading to ADVANCED_PAGINATED"
                )
                return PageLayout.ADVANCED_PAGINATED
            }

            // Rule 3: Use user's preference
            Timber.d("✅ Using user preference: ${userPreference.displayName}")
            return userPreference
        } catch (e: Exception) {
            Timber.e(e, "Error selecting layout, defaulting to MODERN")
            PageLayout.MODERN
        }
    }

    /**
     * Determine if an invoice requires multi-page layout.
     *
     * @param invoice The invoice to check
     * @param maxItemsPerPage Maximum items that fit on a single page
     * @return True if invoice needs multiple pages
     */
    fun needsMultiPage(
        invoice: Invoice,
        maxItemsPerPage: Int = PageLayout.MODERN.maxItemsPerPage
    ): Boolean {
        val itemCount = invoice.items?.size ?: 0
        return itemCount > maxItemsPerPage
    }

    /**
     * Get the recommended layout based only on item count.
     * Ignores user preference and always returns the layout that best fits the data.
     *
     * @param itemCount Number of invoice items
     * @return The recommended PageLayout
     */
    fun getRecommendedLayoutForItemCount(itemCount: Int): PageLayout {
        return when {
            itemCount <= 5 -> {
                Timber.d("Item count $itemCount → Recommended: COMPACT")
                PageLayout.COMPACT
            }
            itemCount <= 10 -> {
                Timber.d("Item count $itemCount → Recommended: FOCUSED")
                PageLayout.FOCUSED
            }
            itemCount <= 15 -> {
                Timber.d("Item count $itemCount → Recommended: MODERN")
                PageLayout.MODERN
            }
            itemCount <= 20 -> {
                Timber.d("Item count $itemCount → Recommended: CLASSIC")
                PageLayout.CLASSIC
            }
            else -> {
                Timber.d("Item count $itemCount → Recommended: ADVANCED_PAGINATED")
                PageLayout.ADVANCED_PAGINATED
            }
        }
    }

    /**
     * Calculate how many pages an invoice will need with a given layout.
     *
     * @param itemCount Number of items
     * @param itemsPerPage Items per page for the layout
     * @return Number of pages needed
     */
    fun calculatePageCount(itemCount: Int, itemsPerPage: Int): Int {
        val pages = (itemCount + itemsPerPage - 1) / itemsPerPage // Ceiling division
        Timber.d("$itemCount items at $itemsPerPage/page = $pages pages")
        return pages
    }
}

/**
 * Extension function to get layout recommendations
 */
fun Invoice.recommendedLayout(): PageLayout {
    val itemCount = this.items?.size ?: 0
    return when {
        itemCount <= 5 -> PageLayout.COMPACT
        itemCount <= 10 -> PageLayout.FOCUSED
        itemCount <= 15 -> PageLayout.MODERN
        itemCount <= 20 -> PageLayout.CLASSIC
        else -> PageLayout.ADVANCED_PAGINATED
    }
}

