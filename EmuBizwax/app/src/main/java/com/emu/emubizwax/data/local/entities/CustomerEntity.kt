package com.emu.emubizwax.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val businessName: String?,
    val businessNumber: String?, // ABN/Tax ID
    val address: String,
    val phone: String,
    val email: String
)
