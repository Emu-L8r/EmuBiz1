package com.emul8r.bizap.domain.model

/**
 * Represents an operation that is pending sync to the remote backend.
 * Operations are queued when the device is offline and processed when
 * connectivity is restored.
 */
data class PendingOperation(
    val id: Long = 0,
    val operationType: OperationType,
    val entityType: String,
    val entityId: Long,
    val payload: String,                         // JSON-serialized operation data
    val createdAt: Long = System.currentTimeMillis(),
    val attemptCount: Int = 0,
    val lastAttemptAt: Long? = null,
    val status: PendingOperationStatus = PendingOperationStatus.PENDING,
    val errorMessage: String? = null
)

enum class OperationType {
    CREATE,
    UPDATE,
    DELETE
}

enum class PendingOperationStatus {
    PENDING,
    IN_PROGRESS,
    FAILED,
    COMPLETED
}
