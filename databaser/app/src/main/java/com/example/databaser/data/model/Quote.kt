package com.example.databaser.data.model

data class Quote(
    val id: Long = 0,
    val customerId: Long,
    val date: Long,
    val header: String = "",
    val subHeader: String = "",
    val footer: String = "",
    val status: Status = Status.DRAFT
)
