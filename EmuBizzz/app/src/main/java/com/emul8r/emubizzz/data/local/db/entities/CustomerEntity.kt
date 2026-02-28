package com.emul8r.emubizzz.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val businessName: String?,
    val businessNumber: String?, // ABN/Tax ID
    val email: String,
    val phone: String,
    val address: String
)
