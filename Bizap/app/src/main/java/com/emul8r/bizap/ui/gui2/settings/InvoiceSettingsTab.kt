package com.emul8r.bizap.ui.gui2.settings

/**
 * Navigation tabs for Invoice Customization Settings Screen
 * Phase 3.5: Tab-based navigation for organized settings management
 */
enum class InvoiceSettingsTab(
    val displayName: String,
    val description: String? = null
) {
    OVERVIEW(
        displayName = "Overview",
        description = "Invoice numbering and quick presets"
    ),
    ADVANCED(
        displayName = "Advanced",
        description = "Fine-tune spacing, typography, and effects"
    ),
    PRESETS(
        displayName = "Templates",
        description = "Professional preset templates"
    ),
    QUALITY(
        displayName = "Quality",
        description = "PDF quality metrics and warnings"
    );

    companion object {
        val all: List<InvoiceSettingsTab> = values().toList()
    }
}
