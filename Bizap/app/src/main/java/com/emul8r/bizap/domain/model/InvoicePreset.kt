package com.emul8r.bizap.domain.model

/**
 * Pre-built invoice customization templates for quick setup
 * Phase 1: One-Click Presets (WIN #4)
 *
 * Allows users to start with a professional template instead of configuring
 * all 28 settings manually. Presets can be customized further after selection.
 */
sealed class InvoicePreset(
    val id: String,
    val name: String,
    val description: String
) {
    abstract fun applyTo(settings: InvoiceSettings): InvoiceSettings

    /**
     * Professional preset: Classic formal appearance
     * Best for: Corporate, law firms, consulting
     */
    object Professional : InvoicePreset(
        "professional",
        "Professional",
        "Classic formal look with blue/gray tones"
    ) {
        override fun applyTo(settings: InvoiceSettings): InvoiceSettings {
            return settings.copy(
                selectedColorScheme = ColorScheme.PROFESSIONAL,
                selectedSpacingProfile = SpacingProfile.NORMAL,
                selectedPageLayout = PageLayout.MODERN,
                totalBoxStyle = TotalBoxStyle.SUBTLE_BACKGROUND,
                enableDividers = true,
                enableAlternatingRowColors = true,
                primaryColor = "#1F4788",
                accentColor = "#5B8AC5"
            )
        }
    }

    /**
     * Modern preset: Contemporary design with vibrant colors
     * Best for: Tech startups, creative agencies, modern businesses
     */
    object Modern : InvoicePreset(
        "modern",
        "Modern",
        "Contemporary design with vibrant colors"
    ) {
        override fun applyTo(settings: InvoiceSettings): InvoiceSettings {
            return settings.copy(
                selectedColorScheme = ColorScheme.VIBRANT,
                selectedSpacingProfile = SpacingProfile.GENEROUS,
                selectedPageLayout = PageLayout.CARDS,
                totalBoxStyle = TotalBoxStyle.PROMINENT_BORDER,
                enableDividers = true,
                enableAlternatingRowColors = true,
                primaryColor = "#FF6B35",
                accentColor = "#004E89"
            )
        }
    }

    /**
     * Minimal preset: Clean and simple design
     * Best for: Freelancers, minimalists, focus-on-content businesses
     */
    object Minimal : InvoicePreset(
        "minimal",
        "Minimal",
        "Clean and simple design"
    ) {
        override fun applyTo(settings: InvoiceSettings): InvoiceSettings {
            return settings.copy(
                selectedColorScheme = ColorScheme.PROFESSIONAL,
                selectedSpacingProfile = SpacingProfile.TIGHT,
                selectedPageLayout = PageLayout.MINIMAL_TABLES,
                totalBoxStyle = TotalBoxStyle.SUBTLE_BACKGROUND,
                enableDividers = false,
                enableAlternatingRowColors = false,
                primaryColor = "#000000",
                accentColor = "#666666"
            )
        }
    }

    /**
     * Creative preset: Bold and colorful design
     * Best for: Design studios, entertainment, creative professionals
     */
    object Creative : InvoicePreset(
        "creative",
        "Creative",
        "Bold and colorful design"
    ) {
        override fun applyTo(settings: InvoiceSettings): InvoiceSettings {
            return settings.copy(
                selectedColorScheme = ColorScheme.TECH,
                selectedSpacingProfile = SpacingProfile.PREMIUM,
                selectedPageLayout = PageLayout.CARDS,
                totalBoxStyle = TotalBoxStyle.PROMINENT_BORDER,
                enableDividers = true,
                enableAlternatingRowColors = true,
                primaryColor = "#FF1654",
                accentColor = "#3A86FF"
            )
        }
    }

    companion object {
        /**
         * Get all available presets
         */
        fun all(): List<InvoicePreset> = listOf(
            Professional,
            Modern,
            Minimal,
            Creative
        )

        /**
         * Get preset by ID
         */
        fun byId(id: String): InvoicePreset? {
            return all().find { it.id == id }
        }

        /**
         * Get default preset (Professional)
         */
        fun default(): InvoicePreset = Professional
    }
}
