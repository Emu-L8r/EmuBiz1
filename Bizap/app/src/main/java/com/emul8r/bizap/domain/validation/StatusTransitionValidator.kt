package com.emul8r.bizap.domain.validation

import com.emul8r.bizap.domain.error.BizapException
import com.emul8r.bizap.domain.model.InvoiceStatus

/**
 * Validates that an invoice status transition follows defined business rules.
 *
 * Valid transitions:
 *   DRAFT         → SENT, OVERDUE
 *   SENT          → PAID, PARTIALLY_PAID, OVERDUE
 *   PARTIALLY_PAID→ PAID, OVERDUE
 *   OVERDUE       → PAID, PARTIALLY_PAID
 *   PAID          → (terminal – no outgoing transitions)
 *
 * Attempting any other transition throws [BizapException.BusinessLogicError].
 */
object StatusTransitionValidator {

    private val VALID_TRANSITIONS: Map<InvoiceStatus, Set<InvoiceStatus>> = mapOf(
        InvoiceStatus.DRAFT to setOf(InvoiceStatus.SENT, InvoiceStatus.OVERDUE),
        InvoiceStatus.SENT to setOf(
            InvoiceStatus.PAID,
            InvoiceStatus.PARTIALLY_PAID,
            InvoiceStatus.OVERDUE
        ),
        InvoiceStatus.PARTIALLY_PAID to setOf(InvoiceStatus.PAID, InvoiceStatus.OVERDUE),
        InvoiceStatus.OVERDUE to setOf(InvoiceStatus.PAID, InvoiceStatus.PARTIALLY_PAID),
        InvoiceStatus.PAID to emptySet()
    )

    /**
     * Validates that transitioning from [currentStatus] to [newStatus] is permitted.
     *
     * @param invoiceId  ID of the invoice being updated (used in error messages).
     * @param currentStatus  The current status of the invoice.
     * @param newStatus  The requested new status.
     * @throws BizapException.BusinessLogicError if the transition is not allowed.
     */
    fun validate(invoiceId: Long, currentStatus: InvoiceStatus, newStatus: InvoiceStatus) {
        val allowed = VALID_TRANSITIONS[currentStatus] ?: emptySet()
        if (newStatus !in allowed) {
            throw BizapException.BusinessLogicError(
                rule = "Invoice status transition must follow defined business rules",
                action = "Change invoice $invoiceId from $currentStatus to $newStatus",
                reason = "Invalid status transition: $currentStatus → $newStatus"
            )
        }
    }

    /**
     * Returns the set of statuses that [currentStatus] may legally transition to.
     */
    fun allowedTransitions(currentStatus: InvoiceStatus): Set<InvoiceStatus> =
        VALID_TRANSITIONS[currentStatus] ?: emptySet()
}
