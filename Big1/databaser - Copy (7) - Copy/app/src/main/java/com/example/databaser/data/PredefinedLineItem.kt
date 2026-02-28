package com.example.databaser.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "predefined_line_items", indices = [Index(value = ["job"])])
data class PredefinedLineItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val job: String,
    val details: String,
    val unitPrice: Double
)
