package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prefilled_items")
data class PrefilledItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val unitPrice: Double
)
