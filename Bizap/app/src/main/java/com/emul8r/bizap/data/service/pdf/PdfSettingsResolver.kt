package com.emul8r.bizap.data.service.pdf

import com.emul8r.bizap.domain.model.*
import com.emul8r.bizap.data.repository.InvoiceSettingsRepository
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.data.service.pdf_layouts.AdvancedPageLayout
import com.emul8r.bizap.data.service.pdf_layouts.ClassicLayout
import com.emul8r.bizap.data.service.pdf_layouts.ModernLayout
import com.emul8r.bizap.data.service.pdf_layouts.PageLayout as PageLayoutImpl
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves multi-level PDF settings into a single coherent configuration.
 *
 * **Settings Hierarchy (Priority Order):**
 * 1. User Defaults (InvoiceSettings) - Highest priority
 * 2. Business Overrides (BusinessProfile) - Medium priority
 * 3. Defaults (Template defaults) - Lowest priority
 *
 * **Field Resolution Matrix:**
 * ┌────────────────────────┬─────────────┬──────────────────┬──────────────┐
 * │ Field                  │ User Level  │ Business Level   │ Auto-Compute │
 * ├────────────────────────┼─────────────┼──────────────────┼──────────────┤
 * │ ColorScheme            │ ✅ YES      │ ❌ NO            │ ❌ NO        │
 * │ SpacingProfile         │ ✅ YES      │ ❌ NO            │ ❌ NO        │
 * │ Logo                   │ ❌ NO       │ ✅ YES (primary) │ ❌ NO        │
 * │ PaymentTerms           │ ✅ YES      │ ✅ YES (override)│ ❌ NO        │
 * │ PageLayout             │ ✅ YES      │ ❌ NO            │ ✅ YES       │
 * │ TotalBoxStyle          │ ✅ YES      │ ❌ NO            │ ❌ NO        │
 * │ PrimaryColor           │ ✅ YES      │ ❌ NO            │ ❌ NO        │
 * │ BankDetails            │ ❌ NO       │ ✅ YES (primary) │ ❌ NO        │
 * │ NeedsMultiPage         │ ❌ NO       │ ❌ NO            │ ✅ YES       │
 * └────────────────────────┴─────────────┴──────────────────┴──────────────┘
 */
data class ResolvedPdfSettings(
    // ─────────────────────────────────────────────────────────────────────
    // USER LEVEL (InvoiceSettings)
    // ─────────────────────────────────────────────────────────────────────
    val colorScheme: ColorScheme,
    val spacingProfile: SpacingProfile,
    val totalBoxStyle: TotalBoxStyle,
    val pageLayout: PageLayout,
    val visualAccents: VisualAccents,
    val enableGradientHeader: Boolean,
    val enableAlternatingRowColors: Boolean,
    val enableDividers: Boolean,
    val highlightTotals: Boolean,
    val primaryColor: String,
    val accentColor: String,
    val invoiceNumberPrefix: String,
    val paymentTermsDays: Int,
    val defaultPaymentNotes: String,
    val footerMessage: String,
    val taxRate: Double,
    val taxName: String,

    // ─────────────────────────────────────────────────────────────────────
    // BUSINESS LEVEL (BusinessProfile)
    // ─────────────────────────────────────────────────────────────────────
    val businessName: String,
    val businessEmail: String,
    val businessPhone: String,
    val businessAddress: String,
    val businessAbn: String,
    val businessWebsite: String,
    val businessLogo: String?,  // Base64 or URI
    val bankName: String?,
    val bankAccountName: String?,
    val bankBsb: String?,
    val bankAccountNumber: String?,

    // ─────────────────────────────────────────────────────────────────────
    // AUTO-COMPUTED (Based on Invoice Data)
    // ─────────────────────────────────────────────────────────────────────
    val selectedLayout: PageLayoutImpl,  // May be auto-upgraded from user preference
    val needsMultiPage: Boolean,
    val autoUpgradeReason: String?  // Why was layout auto-upgraded? (for logging)
) {
    /**
     * Get effective color for use in CSS generation.
     * Prefers user override, falls back to scheme default.
     */
    fun getEffectivePrimaryColor(): String = primaryColor.takeIf { it != "SCHEME_DEFAULT" }
        ?: colorScheme.primaryHex

    fun getEffectiveAccentColor(): String = accentColor.takeIf { it != "SCHEME_DEFAULT" }
        ?: colorScheme.accentHex
}

/**
 * Resolves and validates multi-level PDF settings.
 *
 * Handles:
 * - Priority ordering (user > business > defaults)
 * - Conflict detection and logging
 * - Auto-layout selection (multi-page detection)
 * - Graceful fallbacks for missing data
 * - Settings validation
 */
@Singleton
class PdfSettingsResolver @Inject constructor(
    private val invoiceSettingsRepository: InvoiceSettingsRepository,
    private val businessProfileRepository: BusinessProfileRepository,
    private val businessProfileDao: com.emul8r.bizap.data.local.BusinessProfileDao
) {
    companion object {
        private const val TAG = "PdfSettingsResolver"
    }

    /**
     * Resolve all settings into a single coherent configuration.
     *
     * @param userId Current user ID
     * @param businessId Active business ID
     * @param invoiceSnapshot Invoice data (for auto-layout detection)
     *
     * @return ResolvedPdfSettings with all fields populated, never null
     */
    suspend fun resolve(
        userId: String,
        businessId: Long,
        invoiceSnapshot: InvoiceSnapshot
    ): ResolvedPdfSettings {
        Timber.tag(TAG).d("Resolving PDF settings for user=$userId, business=$businessId, invoice=${invoiceSnapshot.invoiceId}")

        return try {
            // Load data from all levels
            val userSettings = invoiceSettingsRepository.getSettings(userId)
                ?: InvoiceSettings.default(userId).also {
                    Timber.tag(TAG).w("No user settings found, using defaults")
                }

            val businessProfileEntity = businessProfileDao.getProfileById(businessId)
            val businessProfile = businessProfileEntity?.let { entity ->
                BusinessProfile(
                    id = entity.id,
                    businessName = entity.businessName,
                    abn = entity.abn,
                    email = entity.email,
                    phone = entity.phone,
                    address = entity.address,
                    website = entity.website,
                    bsbNumber = entity.bsbNumber,
                    accountNumber = entity.accountNumber,
                    accountName = entity.accountName,
                    bankName = entity.bankName,
                    logoBase64 = entity.logoBase64,
                    signatureUri = entity.signatureUri,
                    isTaxRegistered = entity.isTaxRegistered,
                    defaultTaxRate = entity.defaultTaxRate
                )
            }

            // Resolve each field according to priority matrix
            val resolvedSettings = ResolvedPdfSettings(
                // USER LEVEL
                colorScheme = userSettings.selectedColorScheme,
                spacingProfile = userSettings.selectedSpacingProfile,
                totalBoxStyle = userSettings.totalBoxStyle,
                pageLayout = userSettings.selectedPageLayout,
                visualAccents = userSettings.getVisualAccents(),
                enableGradientHeader = userSettings.enableGradientHeader,
                enableAlternatingRowColors = userSettings.enableAlternatingRowColors,
                enableDividers = userSettings.enableDividers,
                highlightTotals = userSettings.highlightTotals,
                primaryColor = userSettings.primaryColor,
                accentColor = userSettings.accentColor,
                invoiceNumberPrefix = userSettings.invoiceNumberPrefix,
                paymentTermsDays = userSettings.paymentTermsDays,
                defaultPaymentNotes = userSettings.defaultPaymentNotes,
                footerMessage = userSettings.footerMessage,
                taxRate = userSettings.taxRate,
                taxName = userSettings.taxName,

                // BUSINESS LEVEL
                businessName = businessProfile?.businessName ?: "Business Name",
                businessEmail = businessProfile?.email ?: "",
                businessPhone = businessProfile?.phone ?: "",
                businessAddress = businessProfile?.address ?: "",
                businessAbn = businessProfile?.abn ?: "",
                businessWebsite = businessProfile?.website ?: "",
                businessLogo = businessProfile?.logoBase64,
                bankName = businessProfile?.bankName,
                bankAccountName = businessProfile?.accountName,
                bankBsb = businessProfile?.bsbNumber,
                bankAccountNumber = businessProfile?.accountNumber,

                // AUTO-COMPUTED (with logging)
                selectedLayout = selectLayout(invoiceSnapshot, userSettings),
                needsMultiPage = invoiceSnapshot.items.size > 12,
                autoUpgradeReason = getAutoUpgradeReason(invoiceSnapshot, userSettings)
            )

            Timber.tag(TAG).d(
                "✅ Settings resolved: layout=${resolvedSettings.pageLayout.displayName}, " +
                "colors=${resolvedSettings.colorScheme.displayName}, " +
                "spacing=${resolvedSettings.spacingProfile.displayName}, " +
                "multiPage=${resolvedSettings.needsMultiPage}"
            )

            resolvedSettings
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "❌ Error resolving PDF settings, falling back to defaults")
            createDefaultSettings(userId, businessId, invoiceSnapshot)
        }
    }

    /**
     * Select appropriate layout based on invoice and user preferences.
     *
     * **Rules:**
     * 1. If item count exceeds layout capacity, auto-upgrade to ADVANCED_PAGINATED
     * 2. Otherwise, use user's selected layout
     * 3. Fall back to MODERN if user selection is invalid
     */
    private fun selectLayout(
        invoiceSnapshot: InvoiceSnapshot,
        userSettings: InvoiceSettings
    ): PageLayoutImpl {
        val itemCount = invoiceSnapshot.items.size
        val userPreference = userSettings.selectedPageLayout
        val maxItemsForLayout = getMaxItemsForLayout(userPreference)

        // Rule 1: User explicitly selected ADVANCED_PAGINATED
        if (userPreference == PageLayout.ADVANCED_PAGINATED) {
            Timber.tag(TAG).d("User explicitly selected ADVANCED_PAGINATED layout")
            return AdvancedPageLayout()
        }

        // Rule 2: Auto-upgrade if item count exceeds threshold
        if (itemCount > maxItemsForLayout) {
            Timber.tag(TAG).w(
                "⚠️ AUTO-UPGRADE: Invoice has $itemCount items, exceeds $maxItemsForLayout " +
                "for ${userPreference.displayName}. Activating ADVANCED_PAGINATED"
            )
            return AdvancedPageLayout()
        }

        // Rule 3: Use user's preference
        val layout = when (userPreference) {
            PageLayout.CLASSIC -> ClassicLayout()
            PageLayout.MODERN -> ModernLayout()
            PageLayout.SPACIOUS -> ModernLayout()  // TODO: Implement SpacedLayout
            PageLayout.COMPACT -> ClassicLayout()  // TODO: Implement CompactLayout
            PageLayout.SIDEBAR -> ModernLayout()   // TODO: Implement SidebarLayout
            PageLayout.CARDS -> ModernLayout()     // TODO: Implement CardsLayout
            PageLayout.MINIMAL_TABLES -> ModernLayout()  // TODO: Implement MinimalLayout
            PageLayout.FOCUSED -> ClassicLayout()  // TODO: Implement FocusedLayout
            PageLayout.ADVANCED_PAGINATED -> AdvancedPageLayout()
        }

        Timber.tag(TAG).d("Using user preference: ${userPreference.displayName}")
        return layout
    }

    /**
     * Get maximum items per page for layout before auto-upgrade triggers.
     */
    private fun getMaxItemsForLayout(layout: PageLayout): Int = when (layout) {
        PageLayout.CLASSIC -> 20
        PageLayout.MODERN -> 15
        PageLayout.COMPACT -> 25
        PageLayout.SPACIOUS -> 12
        PageLayout.SIDEBAR -> 18
        PageLayout.CARDS -> 8
        PageLayout.MINIMAL_TABLES -> 20
        PageLayout.FOCUSED -> 10
        PageLayout.ADVANCED_PAGINATED -> Int.MAX_VALUE
    }

    /**
     * Get human-readable reason for layout auto-upgrade.
     */
    private fun getAutoUpgradeReason(
        invoiceSnapshot: InvoiceSnapshot,
        userSettings: InvoiceSettings
    ): String? {
        val itemCount = invoiceSnapshot.items.size
        val userPreference = userSettings.selectedPageLayout
        val maxItems = getMaxItemsForLayout(userPreference)

        return if (itemCount > maxItems && userPreference != PageLayout.ADVANCED_PAGINATED) {
            "Invoice has $itemCount items (max $maxItems for ${userPreference.displayName})"
        } else {
            null
        }
    }

    /**
     * Create fully default settings for error recovery.
     */
    private fun createDefaultSettings(
        userId: String,
        businessId: Long,
        invoiceSnapshot: InvoiceSnapshot
    ): ResolvedPdfSettings {
        val defaults = InvoiceSettings.default(userId)
        return ResolvedPdfSettings(
            colorScheme = ColorScheme.PROFESSIONAL,
            spacingProfile = SpacingProfile.NORMAL,
            totalBoxStyle = TotalBoxStyle.SUBTLE_BACKGROUND,
            pageLayout = PageLayout.CLASSIC,
            visualAccents = VisualAccents.default(),
            enableGradientHeader = true,
            enableAlternatingRowColors = true,
            enableDividers = true,
            highlightTotals = true,
            primaryColor = "#6B4C9A",
            accentColor = "#FF9F43",
            invoiceNumberPrefix = "INV-",
            paymentTermsDays = 30,
            defaultPaymentNotes = "",
            footerMessage = "Thank you for your business",
            taxRate = 0.10,
            taxName = "GST",
            businessName = "Business Name",
            businessEmail = "",
            businessPhone = "",
            businessAddress = "",
            businessAbn = "",
            businessWebsite = "",
            businessLogo = null,
            bankName = null,
            bankAccountName = null,
            bankBsb = null,
            bankAccountNumber = null,
            selectedLayout = ClassicLayout(),
            needsMultiPage = invoiceSnapshot.items.size > 12,
            autoUpgradeReason = null
        )
    }
}


