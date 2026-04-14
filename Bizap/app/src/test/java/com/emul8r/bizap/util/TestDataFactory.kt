package com.emul8r.bizap.util

import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.InvoiceItem
import java.time.Instant

/**
 * Factory for creating test data objects in unit tests.
 */
object TestDataFactory {

    fun createTestBusinessProfile(
        id: Long = 1,
        name: String = "Emu Consulting Pty Ltd",
        abn: String = "12 345 678 901"
    ): BusinessProfile {
        return BusinessProfile(
            id = id,
            businessName = name,
            abn = abn,
            email = "contact@emuconsulting.com.au",
            phone = "(02) 8999 1234",
            address = "Level 10, 123 Business Avenue, Sydney NSW 2000"
        )
    }

    fun createTestCustomer(
        id: Long = 1,
        name: String = "UNREALCUSTOMER1"
    ): Customer {
        return Customer(
            id = id,
            name = name,
            email = "test@unrealcustomer1.com.au",
            phone = "(02) 9999 1111",
            address = "123 Test Street, Sydney NSW 2000"
        )
    }

    fun createTestInvoice(
        id: Long = 1,
        businessProfileId: Long = 1,
        customerId: Long = 1,
        currencyCode: String = "AUD",
        total: Long = 100000L,  // $1000 in cents
        isQuote: Boolean = false,
        status: InvoiceStatus = InvoiceStatus.DRAFT
    ): Invoice {
        val now = Instant.now().toString()
        val dueDateStr = Instant.now().plusSeconds(30L * 24 * 60 * 60).toString()
        return Invoice(
            id = id,
            businessProfileId = businessProfileId,
            customerId = customerId,
            customerName = "UNREALCUSTOMER1",
            dateCreated = now,
            dueDate = dueDateStr,
            totalAmount = total,
            currency = currencyCode,
            status = status,
            items = emptyList(),
            isQuote = isQuote
        )
    }
}



