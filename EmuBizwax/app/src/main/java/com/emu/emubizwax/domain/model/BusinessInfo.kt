package com.emu.emubizwax.domain.model

data class BusinessInfo(
    val businessName: String = "Your Company",
    val ownerName: String = "Your Name",
    val address: String = "123 Main St, Anytown, USA",
    val phone: String = "(555) 123-4567",
    val email: String = "contact@yourcompany.com",
    val businessNumber: String = "ABN: 12 345 678 910"
)
