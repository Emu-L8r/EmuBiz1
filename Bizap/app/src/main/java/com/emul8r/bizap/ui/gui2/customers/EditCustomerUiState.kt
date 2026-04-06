package com.emul8r.bizap.ui.gui2.customers

/**
 * UI state for edit customer screen.
 */
data class EditCustomerUiState(
    val customerId: Long = 0L,
    val name: String = "",
    val businessName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val notes: String = "",
    val isSubmitting: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val generalError: String? = null,
    val customerUpdated: Boolean = false
)

