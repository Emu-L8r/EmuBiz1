package com.example.databaser.data.model

data class Customer(
    val id: Long = 0,
    val name: String,
    val companyName: String?,
    val abn_acn: String?,
    val address: String,
    val phone: String,
    val email: String
)
