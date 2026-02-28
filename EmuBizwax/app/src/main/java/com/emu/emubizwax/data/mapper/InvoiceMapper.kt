package com.emu.emubizwax.data.mapper

import com.emu.emubizwax.data.local.entities.InvoiceEntity
import com.emu.emubizwax.data.local.entities.InvoiceWithLineItems
import com.emu.emubizwax.domain.model.Invoice
import com.emu.emubizwax.domain.model.InvoiceStatus
import com.emu.emubizwax.domain.model.InvoiceWithItems
import javax.inject.Inject

class InvoiceMapper @Inject constructor(
    private val lineItemMapper: LineItemMapper
) {

    fun toDomain(entity: InvoiceEntity): Invoice {
        return Invoice(
            id = entity.id,
            customerId = entity.customerId,
            date = entity.date,
            status = InvoiceStatus.valueOf(entity.status),
            isQuote = entity.isQuote,
            taxRate = entity.taxRate,
            photoUris = entity.photoUris
        )
    }

    fun toEntity(domain: Invoice): InvoiceEntity {
        return InvoiceEntity(
            id = domain.id,
            customerId = domain.customerId,
            date = domain.date,
            status = domain.status.name,
            isQuote = domain.isQuote,
            taxRate = domain.taxRate,
            photoUris = domain.photoUris
        )
    }

    fun toDomain(entity: InvoiceWithLineItems): InvoiceWithItems {
        return InvoiceWithItems(
            invoice = toDomain(entity.invoice),
            items = entity.lineItems.map { lineItemMapper.toDomain(it) }
        )
    }
}
