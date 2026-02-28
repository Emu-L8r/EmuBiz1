package com.example.databaser.data.model

data class Note(
    val id: Long = 0,
    val customerId: Long,
    val text: String
)
