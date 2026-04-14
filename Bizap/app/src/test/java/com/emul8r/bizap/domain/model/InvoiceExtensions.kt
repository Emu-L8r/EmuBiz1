package com.emul8r.bizap.domain.model

/**
 * Test extension properties for Invoice calculations
 *
 * These are utility properties used in tests to calculate derived values
 * from Invoice attributes.
 */

/**
 * Calculate the balance remaining on an invoice
 * Balance = Total Amount - Amount Paid
 */
val Invoice.balanceRemaining: Double
    get() = (totalAmount - amountPaid) / 100.0  // Convert from cents to dollars

/**
 * Check if an invoice is fully paid
 * Fully paid = Amount Paid >= Total Amount
 */
val Invoice.isFullyPaid: Boolean
    get() = amountPaid >= totalAmount




