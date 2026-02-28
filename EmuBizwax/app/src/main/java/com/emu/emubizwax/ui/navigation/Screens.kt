package com.emu.emubizwax.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object Dashboard

@Serializable
object Customers

@Serializable
object Invoices

@Serializable
data class CustomerDetails(val customerId: Long)

@Serializable
data class InvoiceDetails(val invoiceId: Long)

@Serializable
object AddCustomer

@Serializable
data class EditCustomer(val customerId: Long)

@Serializable
object AddInvoice

@Serializable
data class EditInvoice(val invoiceId: Long)
