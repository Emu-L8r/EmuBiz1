package com.emul8r.bizap.ui.gui2.customers

import com.emul8r.bizap.domain.model.Customer

/**
 * UI state for the create customer screen.
 *
 * Centralizes all form state (name, email, phone, etc.) so it survives
 * recompositions and screen restarts. This prevents ghost data bugs.
 */
data class CreateCustomerUiState(
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
    val customerCreated: Boolean = false
)

