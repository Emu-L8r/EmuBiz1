package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a pending operation that needs to sync to server
 * Stored locally when offline, synced when online
 */
@Entity(tableName = "pending_operations")
data class PendingOperation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val operationType: String, // CREATE, UPDATE, DELETE
    val entityType: String, // INVOICE, CUSTOMER, LINE_ITEM
    val entityId: Long?, // ID of the entity
    val businessProfileId: Long, // Which business owns this
    val payload: String, // JSON serialized data
    val status: String = "PENDING", // PENDING, SYNCING, SYNCED, FAILED
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastError: String? = null
) {
    fun isPending(): Boolean = status == "PENDING"
    fun isSyncing(): Boolean = status == "SYNCING"
    fun isSynced(): Boolean = status == "SYNCED"
    fun isFailed(): Boolean = status == "FAILED"
    fun canRetry(): Boolean = retryCount < maxRetries
}

