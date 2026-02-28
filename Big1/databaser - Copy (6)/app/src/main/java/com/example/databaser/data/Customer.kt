package com.example.databaser.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "customers", indices = [Index(value = ["name"])])
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val address: String,
    val contactNumber: String,
    val abn: String?,
    val acn: String?,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)
