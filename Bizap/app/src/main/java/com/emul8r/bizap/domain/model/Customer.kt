package com.emul8r.bizap.domain.model

data class Customer(
    val id: Long = 0,
    val name: String,
    val businessName: String? = null,
    val businessNumber: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
