package com.emul8r.bizap.data.mapper

import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.local.entities.LineItemEntity
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.LineItem

fun Invoice.toEntity(): InvoiceEntity {
    return InvoiceEntity(
        id = this.id,
        businessProfileId = this.businessProfileId,
        customerId = this.customerId,
        customerName = this.customerName,
        customerAddress = this.customerAddress,
        customerEmail = this.customerEmail,
        date = this.date,
        totalAmount = this.totalAmount,      // Already in cents
        isQuote = this.isQuote,
        status = this.status.name,
        header = this.header,
        subheader = this.subheader,
        notes = this.notes,
        footer = this.footer,
        photoUris = this.photoUris.joinToString(","),
        pdfUri = this.pdfUri,
        dueDate = this.dueDate,
        taxRate = this.taxRate,
        taxAmount = this.taxAmount,        // Already in cents
        companyLogoPath = this.companyLogoPath,
        updatedAt = this.updatedAt,
        amountPaid = this.amountPaid,      // Already in cents
        parentInvoiceId = this.parentInvoiceId,
        version = this.version,
        invoiceYear = this.invoiceYear,
        invoiceSequence = this.invoiceSequence
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
        date = this.invoice.date,
        totalAmount = this.invoice.totalAmount,        // Already in cents
        items = this.items.map { it.toDomain() },
        isQuote = this.invoice.isQuote,
        status = com.emul8r.bizap.domain.model.InvoiceStatus.valueOf(this.invoice.status),
        header = this.invoice.header,
        subheader = this.invoice.subheader,
        notes = this.invoice.notes,
        footer = this.invoice.footer,
        photoUris = this.invoice.photoUris?.split(",") ?: emptyList(),
        pdfUri = this.invoice.pdfUri,
        dueDate = this.invoice.dueDate,
        taxRate = this.invoice.taxRate,
        taxAmount = this.invoice.taxAmount,            // Already in cents
        companyLogoPath = this.invoice.companyLogoPath,
        updatedAt = this.invoice.updatedAt,
        amountPaid = this.invoice.amountPaid,          // Already in cents
        parentInvoiceId = this.invoice.parentInvoiceId,
        version = this.invoice.version,
        invoiceYear = this.invoice.invoiceYear,
        invoiceSequence = this.invoice.invoiceSequence
    )
}

fun LineItem.toEntity(invoiceId: Long): LineItemEntity {
    return LineItemEntity(
        id = this.id,
        invoiceId = invoiceId,
        description = this.description,
        quantity = this.quantity,
        unitPrice = this.unitPrice          // Already in cents
    )
}

fun LineItemEntity.toDomain(): LineItem {
    return LineItem(
        id = this.id,
        description = this.description,
        quantity = this.quantity,
        unitPrice = this.unitPrice          // Already in cents
    )
}

