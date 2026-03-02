package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index(name = "idx_customers_business", value = ["businessProfileId"]),
        Index(name = "idx_customers_email", value = ["email"])
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessProfileId: Long = 1, // Default to first business
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

