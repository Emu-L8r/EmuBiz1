package com.emul8r.bizap.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Entity representing an operation performed while offline that needs to be synchronized.
 * Part of Phase 2: Offline-First Reliability.
 *
 * ✅ SCHEMA MATCHED: Updated to exactly match Migration 29->30 including indices and defaults.
 * This version is verified to prevent the Room IllegalStateException.
 */
@Serializable
@Entity(
    tableName = "offline_operations",
    indices = [
        Index(value = ["status"], name = "idx_offline_ops_status"),
        Index(value = ["business_profile_id"], name = "idx_offline_ops_business")
    ]
)
data class OfflineOperation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "operation_type")
    val operationType: String,
    
    @ColumnInfo(name = "entity_id")
    val entityId: Long,
    
    @ColumnInfo(name = "entity_data")
    val entityData: String,
    
    @ColumnInfo(name = "business_profile_id")
    val businessProfileId: Long,
    
    @ColumnInfo(name = "timestamp_ms")
    val timestampMs: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "status", defaultValue = "PENDING")
    val status: String = "PENDING",
    
    @ColumnInfo(name = "retry_count", defaultValue = "0")
    val retryCount: Int = 0,
    
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null
)
