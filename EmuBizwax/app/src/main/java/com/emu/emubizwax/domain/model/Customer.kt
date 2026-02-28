package com.emu.emubizwax.domain.model

data class Customer(
    val id: Long = 0,
    val name: String,
    val businessName: String?,
    val businessNumber: String?,
    val address: String,
    val phone: String,
    val email: String
)
