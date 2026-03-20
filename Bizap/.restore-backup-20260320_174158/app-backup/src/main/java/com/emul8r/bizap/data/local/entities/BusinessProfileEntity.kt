package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_profiles")
data class BusinessProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessName: String,
    val abn: String,
    val email: String,
    val phone: String,
    val address: String,
    val website: String,
    val bsbNumber: String? = null,
    val accountNumber: String? = null,
    val accountName: String? = null,
    val bankName: String? = null,
    val logoBase64: String? = null,
    val signatureUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    // Tax Registration (for VAT/GST/Sales Tax)
    val isTaxRegistered: Boolean = false,        // Default: business is NOT tax registered
    val defaultTaxRate: Float = 0.10f            // Default: 10% tax rate if registered
)
