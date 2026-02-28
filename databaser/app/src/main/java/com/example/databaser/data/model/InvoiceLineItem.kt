package com.example.databaser.data.model

data class InvoiceLineItem(
    val id: Long = 0,
    val invoiceId: Long,
    val description: String,
    val quantity: Int,
    val price: Double
)
