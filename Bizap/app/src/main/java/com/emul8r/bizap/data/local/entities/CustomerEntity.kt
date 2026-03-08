package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index(name = "idx_customers_business", value = ["businessProfileId"]),
        Index(name = "idx_customers_email", value = ["email"], unique = true),
        Index(name = "idx_customers_business_name", value = ["businessProfileId", "name"]),
        Index(name = "idx_customers_business_active_name", value = ["businessProfileId", "isActive", "name"])
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
    val city: String? = null,
    val postalCode: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
