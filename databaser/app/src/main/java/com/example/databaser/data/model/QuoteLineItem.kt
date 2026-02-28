package com.example.databaser.data.model

data class QuoteLineItem(
    val id: Long = 0,
    val quoteId: Long,
    val description: String,
    val quantity: Int,
    val price: Double
)
