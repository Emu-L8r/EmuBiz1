package com.emul8r.bizap.ui.invoices.common

import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.ui.invoices.LineItemForm

/**
 * Unified state for the create invoice form.
 * This state is shared between GUI1 and GUI2 for feature parity.
 *
 * Standardized naming:
 * - Uses "header" and "subheader" (not "title" and "subtitle")
 * - Consistent field names across both interfaces
 */
data class CreateInvoiceFormState(
    // Customer Selection
    val selectedCustomer: Customer? = null,
    val customers: List<Customer> = emptyList(),

    // Invoice metadata - Standardized naming
    val header: String = "",                    // Standard: "Header" instead of "Title"
    val subheader: String = "",                 // Standard: "Subheader" instead of "Subtitle"
    val notes: String = "",
    val footer: String = "",

    // Line items
    val items: List<LineItemForm> = emptyList(),

    // Currency & Customization
    val selectedCurrencyCode: String = "AUD",
    val companyName: String = "",
    val templateType: String = "modern",

    // Photos
    val photoUris: List<String> = emptyList(),

    // Tax Settings
    val taxRate: Double = 0.1,                  // 10% default

    // UI State
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val isLoading: Boolean = false,

    // Validation
    val isValid: Boolean = false
)



