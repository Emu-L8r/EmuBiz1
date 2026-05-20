package com.emul8r.bizap.domain.model

/**
 * Built-in PDF template presets for one-click configuration.
 *
 * Each preset applies a curated set of settings for a specific use case,
 * allowing users to start with best practices instead of blank slate.
 *
 * Phase 3.5: Preset Templates System
 */
data class PdfPreset(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val category: PresetCategory,
    val apply: (settings: InvoiceSettings) -> InvoiceSettings
) {
    companion object {
        /**
         * Get all built-in presets organized by category.
         */
        fun builtInPresets(): List<PdfPreset> = listOf(
            // ─────────────────────────────────────────────────────────────────
            // PROFESSIONAL CATEGORY
            // ─────────────────────────────────────────────────────────────────
            PdfPreset(
                id = "professional_corporate",
                name = "Professional Corporate",
                description = "Navy & gold corporate invoice for established businesses",
                emoji = "💼",
                category = PresetCategory.PROFESSIONAL,
                apply = { it.copy(
                    selectedPageLayout = PageLayout.CLASSIC,
                    selectedColorScheme = ColorScheme.PROFESSIONAL,
                    selectedSpacingProfile = SpacingProfile.NORMAL,
                    enableGradientHeader = false,
                    enableShadows = true,
                    enableRoundedCorners = false,
                    enableAlternatingRowColors = true,
                    enableDividers = true,
                    highlightTotals = true,
                    totalBoxStyle = TotalBoxStyle.PROMINENT_BORDER,
                    enableBackgroundPattern = false,
                    enableWatermarkText = false,
                    enableStatusBadges = true
                )}
            ),

            PdfPreset(
                id = "professional_formal",
                name = "Professional Formal",
                description = "Elegant formal design with two-column sidebar layout",
                emoji = "🎩",
                category = PresetCategory.PROFESSIONAL,
                apply = { it.copy(
                    selectedPageLayout = PageLayout.SIDEBAR,
                    selectedColorScheme = ColorScheme.PROFESSIONAL,
                    selectedSpacingProfile = SpacingProfile.GENEROUS,
                    enableGradientHeader = false,
                    enableShadows = true,
                    enableRoundedCorners = true,
                    cornerRadiusDp = 12f,
                    enableAlternatingRowColors = false,
                    enableDividers = true,
                    highlightTotals = true,
                    totalBoxStyle = TotalBoxStyle.BOLD_HIGHLIGHT,
                    enableSignatureArea = true,
                    showSignatureField = true,
                    enableStatusBadges = true
                )}
            ),

            // ─────────────────────────────────────────────────────────────────
            // MODERN CATEGORY
            // ─────────────────────────────────────────────────────────────────
            PdfPreset(
                id = "modern_startup",
                name = "Modern Startup",
                description = "Vibrant contemporary design for tech companies & startups",
                emoji = "🚀",
                category = PresetCategory.MODERN,
                apply = { it.copy(
                    selectedPageLayout = PageLayout.MODERN,
                    selectedColorScheme = ColorScheme.VIBRANT,
                    selectedSpacingProfile = SpacingProfile.GENEROUS,
                    enableGradientHeader = true,
                    enableShadows = true,
                    enableRoundedCorners = true,
                    cornerRadiusDp = 8f,
                    enableAlternatingRowColors = true,
                    enableDividers = true,
                    highlightTotals = true,
                    totalBoxStyle = TotalBoxStyle.GRADIENT_BACKGROUND,
                    enableBackgroundPattern = true,
                    backgroundPatternType = BackgroundPattern.DOTS,
                    patternOpacity = 0.05f,
                    enableStatusBadges = true,
                    badgeStyle = BadgeStyle.ROUNDED_FILLED
                )}
            ),

            PdfPreset(
                id = "modern_tech",
                name = "Modern Tech",
                description = "Deep blue & cyan modern design for innovative companies",
                emoji = "💻",
                category = PresetCategory.MODERN,
                apply = { it.copy(
                    selectedPageLayout = PageLayout.MODERN,
                    selectedColorScheme = ColorScheme.TECH,
                    selectedSpacingProfile = SpacingProfile.NORMAL,
                    enableGradientHeader = true,
                    enableShadows = true,
                    enableRoundedCorners = true,
                    cornerRadiusDp = 6f,
                    enableAlternatingRowColors = false,
                    enableDividers = true,
                    highlightTotals = true,
                    totalBoxStyle = TotalBoxStyle.ACCENT_BORDER,
                    enableBackgroundPattern = true,
                    backgroundPatternType = BackgroundPattern.GRID,
                    patternOpacity = 0.03f,
                    enableStatusBadges = true
                )}
            ),

            // ─────────────────────────────────────────────────────────────────
            // MINIMAL CATEGORY
            // ─────────────────────────────────────────────────────────────────
            PdfPreset(
                id = "minimal_clean",
                name = "Minimal Clean",
                description = "Elegant simplicity with minimal visual clutter",
                emoji = "⚪",
                category = PresetCategory.MINIMAL,
                apply = { it.copy(
                    selectedPageLayout = PageLayout.MINIMAL_TABLES,
                    selectedColorScheme = ColorScheme.MINIMAL,
                    selectedSpacingProfile = SpacingProfile.NORMAL,
                    enableGradientHeader = false,
                    enableShadows = false,
                    enableRoundedCorners = false,
                    enableAlternatingRowColors = false,
                    enableDividers = true,
                    dividerStyle = DividerStyle.SOLID,
                    dividerColor = "#CCCCCC",
                    highlightTotals = true,
                    totalBoxStyle = TotalBoxStyle.SUBTLE_BACKGROUND,
                    enableBackgroundPattern = false,
                    enableWatermarkText = false,
                    enableStatusBadges = false,
                    enablePaymentIcons = false
                )}
            ),

            PdfPreset(
                id = "minimal_nature",
                name = "Minimal Nature",
                description = "Green & earth-tone palette for sustainable/eco-focused businesses",
                emoji = "🌿",
                category = PresetCategory.MINIMAL,
                apply = { it.copy(
                    selectedPageLayout = PageLayout.FOCUSED,
                    selectedColorScheme = ColorScheme.NATURE,
                    selectedSpacingProfile = SpacingProfile.GENEROUS,
                    enableGradientHeader = true,
                    enableShadows = false,
                    enableRoundedCorners = true,
                    cornerRadiusDp = 6f,
                    enableAlternatingRowColors = false,
                    enableDividers = true,
                    dividerStyle = DividerStyle.DOTTED,
                    highlightTotals = true,
                    totalBoxStyle = TotalBoxStyle.SUBTLE_BACKGROUND,
                    enableBackgroundPattern = true,
                    backgroundPatternType = BackgroundPattern.WAVES,
                    patternOpacity = 0.04f,
                    enableStatusBadges = true
                )}
            ),

            // ─────────────────────────────────────────────────────────────────
            // PREMIUM CATEGORY
            // ─────────────────────────────────────────────────────────────────
            PdfPreset(
                id = "premium_luxury",
                name = "Premium Luxury",
                description = "High-end invoice with all visual enhancements and branding",
                emoji = "👑",
                category = PresetCategory.PREMIUM,
                apply = { it.copy(
                    selectedPageLayout = PageLayout.SIDEBAR,
                    selectedColorScheme = ColorScheme.PROFESSIONAL,
                    selectedSpacingProfile = SpacingProfile.PREMIUM,
                    enableGradientHeader = true,
                    headerGradientEndColor = "#FFD700",
                    enableShadows = true,
                    shadowIntensity = 0.25f,
                    enableRoundedCorners = true,
                    cornerRadiusDp = 12f,
                    enableAlternatingRowColors = false,
                    enableDividers = true,
                    dividerStyle = DividerStyle.SOLID,
                    highlightTotals = true,
                    totalBoxStyle = TotalBoxStyle.GRADIENT_BACKGROUND,
                    enableBackgroundPattern = true,
                    backgroundPatternType = BackgroundPattern.WAVES,
                    patternOpacity = 0.08f,
                    enableWatermarkText = false,
                    enableLogo = true,
                    enableMotto = true,
                    enableSignatureArea = true,
                    enableStatusBadges = true,
                    badgeStyle = BadgeStyle.BADGE_WITH_ICON,
                    enablePaymentIcons = true,
                    enableQrCode = true
                )}
            ),

            PdfPreset(
                id = "premium_boutique",
                name = "Premium Boutique",
                description = "Artistic design for boutique/creative services with cards layout",
                emoji = "✨",
                category = PresetCategory.PREMIUM,
                apply = { it.copy(
                    selectedPageLayout = PageLayout.CARDS,
                    selectedColorScheme = ColorScheme.VIBRANT,
                    selectedSpacingProfile = SpacingProfile.PREMIUM,
                    enableGradientHeader = true,
                    enableShadows = true,
                    shadowIntensity = 0.2f,
                    enableRoundedCorners = true,
                    cornerRadiusDp = 16f,
                    enableAlternatingRowColors = false,
                    enableDividers = false,
                    highlightTotals = true,
                    totalBoxStyle = TotalBoxStyle.BOLD_HIGHLIGHT,
                    enableBackgroundPattern = true,
                    backgroundPatternType = BackgroundPattern.GRID,
                    patternOpacity = 0.06f,
                    enableWatermarkText = true,
                    watermarkOpacity = 0.08f,
                    enableLogo = true,
                    enableMotto = true,
                    enableSignatureArea = true
                )}
            ),

            // ─────────────────────────────────────────────────────────────────
            // COMPACT CATEGORY
            // ─────────────────────────────────────────────────────────────────
            PdfPreset(
                id = "compact_efficient",
                name = "Compact Efficient",
                description = "Maximum items per page with minimal spacing",
                emoji = "📊",
                category = PresetCategory.COMPACT,
                apply = { it.copy(
                    selectedPageLayout = PageLayout.COMPACT,
                    selectedColorScheme = ColorScheme.MINIMAL,
                    selectedSpacingProfile = SpacingProfile.TIGHT,
                    enableGradientHeader = false,
                    enableShadows = false,
                    enableRoundedCorners = false,
                    enableAlternatingRowColors = true,
                    enableDividers = true,
                    highlightTotals = true,
                    totalBoxStyle = TotalBoxStyle.SUBTLE_BACKGROUND,
                    enableBackgroundPattern = false,
                    enableWatermarkText = false,
                    enableStatusBadges = true,
                    enablePaymentIcons = false
                )}
            ),

            PdfPreset(
                id = "compact_paginated",
                name = "Compact Auto-Paginated",
                description = "Unlimited items with automatic pagination and smart headers",
                emoji = "📄",
                category = PresetCategory.COMPACT,
                apply = { it.copy(
                    selectedPageLayout = PageLayout.ADVANCED_PAGINATED,
                    selectedColorScheme = ColorScheme.PROFESSIONAL,
                    selectedSpacingProfile = SpacingProfile.TIGHT,
                    enableGradientHeader = false,
                    enableShadows = false,
                    enableRoundedCorners = false,
                    enableAlternatingRowColors = true,
                    enableDividers = true,
                    highlightTotals = true,
                    totalBoxStyle = TotalBoxStyle.SUBTLE_BACKGROUND,
                    enableBackgroundPattern = false,
                    enableStatusBadges = true,
                    showPageNumbers = true
                )}
            )
        )

        /**
         * Get preset by ID.
         */
        fun byId(id: String): PdfPreset? {
            return builtInPresets().find { it.id == id }
        }

        /**
         * Get presets by category.
         */
        fun byCategory(category: PresetCategory): List<PdfPreset> {
            return builtInPresets().filter { it.category == category }
        }

        /**
         * Get recommended preset for a specific business type.
         */
        fun recommend(businessType: String): PdfPreset {
            return when (businessType.lowercase()) {
                "startup", "tech", "it" -> byId("modern_startup") ?: builtInPresets().first()
                "agency", "design", "creative" -> byId("premium_boutique") ?: builtInPresets().first()
                "retail", "ecommerce" -> byId("modern_tech") ?: builtInPresets().first()
                "corporate", "enterprise" -> byId("professional_corporate") ?: builtInPresets().first()
                "minimal", "freelance", "consultant" -> byId("minimal_clean") ?: builtInPresets().first()
                else -> builtInPresets().first()
            }
        }
    }
}

/**
 * Preset categories for organization and discovery.
 */
enum class PresetCategory(val displayName: String, val emoji: String) {
    PROFESSIONAL("Professional", "💼"),
    MODERN("Modern", "🎨"),
    MINIMAL("Minimal", "⚪"),
    PREMIUM("Premium", "👑"),
    COMPACT("Compact", "📊")
}

