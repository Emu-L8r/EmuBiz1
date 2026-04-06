package com.emul8r.bizap.ui.gui2.settings

/**
 * UI state for invoice customization settings screen.
 */
data class InvoiceCustomizationSettingsUiState(
    val prefix: String = "",
    val startingNumber: String = "",
    val footerText: String = "",
    val includeNotes: Boolean = true,
    val includeTaxId: Boolean = true,
    val showLogo: Boolean = true,
    val showCompanyInfo: Boolean = true,
    val isSubmitting: Boolean = false,
    val prefixError: String? = null,
    val startingNumberError: String? = null,
    val generalError: String? = null,
    val settingsSaved: Boolean = false
)

