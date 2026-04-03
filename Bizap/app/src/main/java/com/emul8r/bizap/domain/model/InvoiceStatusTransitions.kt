package com.emul8r.bizap.domain.model

/**
 * Invoice status transition rules - defines which statuses can transition to which.
 * Prevents invalid business state transitions (e.g., PAID → DRAFT is not allowed).
 *
 * Usage:
 * ```kotlin
 * val isValid = InvoiceStatusTransitions.isValidTransition(from = PAID, to = OVERDUE)
 * val validOptions = InvoiceStatusTransitions.allowedTransitionsFrom(DRAFT)
 * ```
 */
object InvoiceStatusTransitions {

    /**
     * Defines which status transitions are allowed.
     * Returns true if transitioning from [from] status to [to] status is valid.
     */
    fun isValidTransition(from: String, to: String): Boolean {
        return when (from) {
            InvoiceStatusConstants.DRAFT -> {
                // Draft can go to: Sent, Paid, Overdue, or stay Draft
                to in setOf(
                    InvoiceStatusConstants.SENT,
                    InvoiceStatusConstants.PAID,
                    InvoiceStatusConstants.OVERDUE,
                    InvoiceStatusConstants.DRAFT
                )
            }
            InvoiceStatusConstants.SENT -> {
                // Sent can go to: Paid, Overdue, or back to Draft
                to in setOf(
                    InvoiceStatusConstants.PAID,
                    InvoiceStatusConstants.OVERDUE,
                    InvoiceStatusConstants.DRAFT,
                    InvoiceStatusConstants.SENT
                )
            }
            InvoiceStatusConstants.PAID -> {
                // Paid can only go to: Overdue (can't undo paid status)
                to in setOf(
                    InvoiceStatusConstants.OVERDUE,
                    InvoiceStatusConstants.PAID
                )
            }
            InvoiceStatusConstants.OVERDUE -> {
                // Overdue can go to: Paid (when payment received) or stay Overdue
                to in setOf(
                    InvoiceStatusConstants.PAID,
                    InvoiceStatusConstants.OVERDUE
                )
            }
            InvoiceStatusConstants.PARTIALLY_PAID -> {
                // Partially Paid can go to: Paid, Overdue, or back to Draft
                to in setOf(
                    InvoiceStatusConstants.PAID,
                    InvoiceStatusConstants.OVERDUE,
                    InvoiceStatusConstants.DRAFT,
                    InvoiceStatusConstants.PARTIALLY_PAID
                )
            }
            else -> {
                // Unknown status - no transitions allowed
                to == from
            }
        }
    }

    /**
     * Returns set of statuses that can be transitioned TO from the given [status].
     * Useful for populating UI dropdowns with valid options.
     */
    fun allowedTransitionsFrom(status: String): Set<String> {
        return when (status) {
            InvoiceStatusConstants.DRAFT -> setOf(
                InvoiceStatusConstants.SENT,
                InvoiceStatusConstants.PAID,
                InvoiceStatusConstants.OVERDUE
            )
            InvoiceStatusConstants.SENT -> setOf(
                InvoiceStatusConstants.PAID,
                InvoiceStatusConstants.OVERDUE,
                InvoiceStatusConstants.DRAFT
            )
            InvoiceStatusConstants.PAID -> setOf(
                InvoiceStatusConstants.OVERDUE
            )
            InvoiceStatusConstants.OVERDUE -> setOf(
                InvoiceStatusConstants.PAID
            )
            InvoiceStatusConstants.PARTIALLY_PAID -> setOf(
                InvoiceStatusConstants.PAID,
                InvoiceStatusConstants.OVERDUE,
                InvoiceStatusConstants.DRAFT
            )
            else -> emptySet()
        }
    }

    /**
     * Gets a human-readable description of what transitions are allowed.
     */
    fun getTransitionDescription(from: String): String = when (from) {
        InvoiceStatusConstants.DRAFT -> "Can be sent, marked as paid, or marked overdue"
        InvoiceStatusConstants.SENT -> "Can be paid, marked overdue, or reverted to draft"
        InvoiceStatusConstants.PAID -> "Can be marked as overdue (payment status cannot be reverted)"
        InvoiceStatusConstants.OVERDUE -> "Can be marked as paid if payment received"
        InvoiceStatusConstants.PARTIALLY_PAID -> "Can be fully paid, marked overdue, or reverted to draft"
        else -> "Unknown status"
    }
}

