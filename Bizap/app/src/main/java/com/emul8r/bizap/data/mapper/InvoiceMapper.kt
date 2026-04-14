package com.emul8r.bizap.data.mapper

import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.local.entities.LineItemEntity
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceItem
import com.emul8r.bizap.domain.model.InvoiceStatus
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val isoFormatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())

// Convert epoch millis to ISO-8601 string
private fun Long.toIsoString(): String {
    return if (this == 0L) "" else Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(isoFormatter)
}

// Convert ISO-8601 string to epoch millis
private fun String?.toEpochMillis(): Long {
    return try {
        if (this.isNullOrBlank()) 0L
        else Instant.parse(this).toEpochMilli()
    } catch (e: Exception) {
        Timber.e(e, "Failed to parse date string: $this")
        0L
    }
}

fun Invoice.toEntity(): InvoiceEntity {
    return InvoiceEntity(
        id = this.id,
        businessProfileId = this.businessProfileId,
        customerId = this.customerId,
        customerName = this.customerName,
        customerAddress = this.customerAddress,
        customerEmail = this.customerEmail,
        date = this.dateCreated.toEpochMillis(),
        totalAmount = this.totalAmount,
        isQuote = this.isQuote,
        status = this.status.name,
        header = this.header,
        subheader = this.subheader,
        notes = this.notes,
        footer = this.footer,
        photoUris = this.photoUris.joinToString(","),
        pdfUri = this.pdfUri,
        dueDate = this.dueDate.toEpochMillis(),
        taxRate = this.taxRate,
        taxAmount = this.taxAmount,
        companyLogoPath = this.companyLogoPath,
        updatedAt = this.updatedAt,
        amountPaid = this.amountPaid,
        parentInvoiceId = this.parentInvoiceId,
        version = this.version,
        dailySequence = this.dailySequence,
        invoiceYear = this.invoiceYear,
        invoiceSequence = this.invoiceSequence,
        currencyCode = this.currency,
        invoiceNumber = this.invoiceNumber,
        dailyCounter = this.dailyCounter,
        displayName = this.displayName,
        discountAmount = this.discountAmount,
        createdAt = this.createdAt,
        isActive = this.isActive
    )
}

fun InvoiceWithItems.toDomain(): Invoice {
    return Invoice(
        id = this.invoice.id,
        businessProfileId = this.invoice.businessProfileId,
        customerId = this.invoice.customerId,
        customerName = this.invoice.customerName,
        customerAddress = this.invoice.customerAddress,
        customerEmail = this.invoice.customerEmail,
        dateCreated = this.invoice.date.toIsoString(),
        dueDate = this.invoice.dueDate.toIsoString(),
        totalAmount = this.invoice.totalAmount,
        items = this.items.map { it.toDomain() },
        isQuote = this.invoice.isQuote,
        status = try {
            InvoiceStatus.valueOf(this.invoice.status)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Invalid invoice status: ${this.invoice.status}, defaulting to DRAFT")
            InvoiceStatus.DRAFT
        },
        header = this.invoice.header,
        subheader = this.invoice.subheader,
        notes = this.invoice.notes,
        footer = this.invoice.footer,
        photoUris = this.invoice.photoUris?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
        pdfUri = this.invoice.pdfUri,
        taxRate = this.invoice.taxRate,
        taxAmount = this.invoice.taxAmount,
        companyLogoPath = this.invoice.companyLogoPath,
        updatedAt = this.invoice.updatedAt,
        amountPaid = this.invoice.amountPaid,
        parentInvoiceId = this.invoice.parentInvoiceId,
        version = this.invoice.version,
        dailySequence = this.invoice.dailySequence,
        invoiceYear = this.invoice.invoiceYear,
        invoiceSequence = this.invoice.invoiceSequence,
        currency = this.invoice.currencyCode,
        dailyCounter = this.invoice.dailyCounter,
        displayName = this.invoice.displayName,
        discountAmount = this.invoice.discountAmount,
        invoiceNumber = this.invoice.invoiceNumber,
        datePaid = this.invoice.updatedAt.toIsoString(),
        createdAt = this.invoice.createdAt,
        isActive = this.invoice.isActive,
        discount = 0.0 // Calculated from discountAmount in business logic
    )
}

fun InvoiceItem.toEntity(invoiceId: Long): LineItemEntity {
    return LineItemEntity(
        id = this.id,
        invoiceId = invoiceId,
        description = this.description,
        quantity = this.quantity,
        unitPrice = this.unitPrice
    )
}

fun LineItemEntity.toDomain(): InvoiceItem {
    return InvoiceItem(
        id = this.id,
        description = this.description,
        quantity = this.quantity,
        unitPrice = this.unitPrice
    )
}
