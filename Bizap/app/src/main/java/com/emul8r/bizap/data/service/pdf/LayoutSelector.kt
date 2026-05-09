package com.emul8r.bizap.data.service.pdf

import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.PageLayout
import com.emul8r.bizap.data.service.pdf_layouts.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Intelligent layout selection service.
 *
 * Handles:
 * - User preference validation
 * - Capacity detection (item count vs. max items per layout)
 * - Auto-upgrade to ADVANCED_PAGINATED when needed
 * - Graceful fallbacks for invalid preferences
 * - Comprehensive logging for debugging
 *
 * **Selection Rules (in priority order):**
 * 1. If user explicitly chose ADVANCED_PAGINATED → use it
 * 2. If item count exceeds layout capacity AND auto-upgrade enabled → ADVANCED_PAGINATED
 * 3. Otherwise → user's selected layout
 * 4. Fallback → MODERN layout
 */
@Singleton
class LayoutSelector @Inject constructor() {

    companion object {
        private const val TAG = "LayoutSelector"
    }

    /**
     * Select appropriate layout for invoice.
     *
     * @param invoiceSnapshot Invoice data (for item count)
     * @param userPreference User's selected layout preference
     * @param autoUpgradeEnabled Allow auto-upgrade to multi-page? (default: true)
     *
     * @return PageLayout implementation (ClassicLayout, ModernLayout, AdvancedPageLayout)
     */
    fun select(
        invoiceSnapshot: InvoiceSnapshot,
        userPreference: PageLayout,
        autoUpgradeEnabled: Boolean = true
    ): com.emul8r.bizap.data.service.pdf_layouts.PageLayout {
        val itemCount = invoiceSnapshot.items.size

        Timber.tag(TAG).d(
            "Selecting layout: items=$itemCount, preference=${userPreference.displayName}, " +
            "autoUpgrade=$autoUpgradeEnabled"
        )

        // Rule 1: User explicitly chose ADVANCED_PAGINATED
        if (userPreference == PageLayout.ADVANCED_PAGINATED) {
            Timber.tag(TAG).d("✅ User explicitly selected ADVANCED_PAGINATED")
            return AdvancedPageLayout()
        }

        // Rule 2: Detect if auto-upgrade is needed
        val maxItemsForPreference = getCapacity(userPreference)
        if (autoUpgradeEnabled && itemCount > maxItemsForPreference) {
            Timber.tag(TAG).w(
                "⚠️ AUTO-UPGRADE TRIGGERED: " +
                "Invoice has $itemCount items, exceeds capacity of $maxItemsForPreference " +
                "for layout ${userPreference.displayName}. " +
                "Automatically switching to ADVANCED_PAGINATED for proper pagination."
            )
            return AdvancedPageLayout()
        }

        // Rule 3: Use user's preference
        Timber.tag(TAG).d("✅ Using user preference: ${userPreference.displayName}")
        return selectByPreference(userPreference)
    }

    /**
     * Select layout by user preference enum.
     * Maps PageLayout enum to actual PageLayout implementation classes.
     */
    private fun selectByPreference(preference: PageLayout): com.emul8r.bizap.data.service.pdf_layouts.PageLayout {
        return when (preference) {
            PageLayout.CLASSIC -> {
                Timber.tag(TAG).d("Instantiating ClassicLayout")
                ClassicLayout()
            }

            PageLayout.MODERN -> {
                Timber.tag(TAG).d("Instantiating ModernLayout")
                ModernLayout()
            }

            // TODO: Implement these in Phase 3B
            PageLayout.SPACIOUS -> {
                Timber.tag(TAG).d("SPACIOUS not yet implemented, falling back to ModernLayout")
                ModernLayout()  // Fallback
            }

            PageLayout.COMPACT -> {
                Timber.tag(TAG).d("COMPACT not yet implemented, falling back to ClassicLayout")
                ClassicLayout()  // Fallback
            }

            PageLayout.SIDEBAR -> {
                Timber.tag(TAG).d("SIDEBAR not yet implemented, falling back to ModernLayout")
                ModernLayout()  // Fallback
            }

            PageLayout.CARDS -> {
                Timber.tag(TAG).d("CARDS not yet implemented, falling back to ModernLayout")
                ModernLayout()  // Fallback
            }

            PageLayout.MINIMAL_TABLES -> {
                Timber.tag(TAG).d("MINIMAL_TABLES not yet implemented, falling back to ModernLayout")
                ModernLayout()  // Fallback
            }

            PageLayout.FOCUSED -> {
                Timber.tag(TAG).d("FOCUSED not yet implemented, falling back to ClassicLayout")
                ClassicLayout()  // Fallback
            }

            PageLayout.ADVANCED_PAGINATED -> {
                Timber.tag(TAG).d("Instantiating AdvancedPageLayout")
                AdvancedPageLayout()
            }
        }
    }

    /**
     * Get maximum item capacity for each layout.
     * These thresholds determine when auto-upgrade to ADVANCED_PAGINATED is triggered.
     *
     * **Rationale:**
     * - Tight layouts (CARDS, SPACIOUS): 8-12 items max before crowding
     * - Balanced layouts (CLASSIC, MODERN, MINIMAL_TABLES): 15-20 items
     * - Compact layouts (COMPACT): 25 items (aggressive packing)
     * - ADVANCED_PAGINATED: Unlimited (auto-paginates)
     */
    fun getCapacity(layout: PageLayout): Int = when (layout) {
        PageLayout.CLASSIC -> 20
        PageLayout.MODERN -> 15
        PageLayout.COMPACT -> 25
        PageLayout.SPACIOUS -> 12
        PageLayout.SIDEBAR -> 18
        PageLayout.CARDS -> 8
        PageLayout.MINIMAL_TABLES -> 20
        PageLayout.FOCUSED -> 10
        PageLayout.ADVANCED_PAGINATED -> Int.MAX_VALUE  // Unlimited
    }

    /**
     * Check if layout needs multi-page for given item count.
     */
    fun needsMultiPage(itemCount: Int, itemsPerPage: Int = 12): Boolean {
        return itemCount > itemsPerPage
    }

    /**
     * Get human-readable description of why a layout was selected.
     */
    fun getSelectionReason(
        invoiceSnapshot: InvoiceSnapshot,
        userPreference: PageLayout,
        selectedLayout: com.emul8r.bizap.data.service.pdf_layouts.PageLayout
    ): String {
        val itemCount = invoiceSnapshot.items.size
        val selectedName = selectedLayout::class.simpleName ?: "Unknown"

        return when {
            userPreference == PageLayout.ADVANCED_PAGINATED ->
                "User explicitly selected multi-page layout"

            itemCount > getCapacity(userPreference) ->
                "Auto-upgraded: ${userPreference.displayName} max is ${getCapacity(userPreference)} " +
                "items, but invoice has $itemCount items"

            else ->
                "Using user preference: ${userPreference.displayName}"
        }
    }
}

