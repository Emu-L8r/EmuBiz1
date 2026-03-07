package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity backing the offline operation queue.
 * Each row represents a single create / update / delete operation that
 * has not yet been synced to the remote backend.
 */
@Entity(
    tableName = "pending_operations",
    indices = [
        Index(name = "idx_pending_status", value = ["status"]),
        Index(name = "idx_pending_entity", value = ["entityType", "entityId"]),
        Index(name = "idx_pending_created", value = ["createdAt"])
    ]
)
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val operationType: String,           // CREATE | UPDATE | DELETE
    val entityType: String,              // e.g., "INVOICE", "CUSTOMER"
    val entityId: Long,
    val payload: String,                 // JSON-serialized operation data
    val createdAt: Long = System.currentTimeMillis(),
    val attemptCount: Int = 0,
    val lastAttemptAt: Long? = null,
    val status: String = "PENDING",      // PENDING | IN_PROGRESS | FAILED | COMPLETED
    val errorMessage: String? = null
)
