package com.emul8r.bizap.data.local.offline

/**
 * Represents the current state of the offline queue for UI observation.
 */
data class QueueState(
    val totalPending: Int = 0,
    val failedCount: Int = 0,
    val lastSyncTime: Long? = null,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null
) {
    val hasFailedOperations: Boolean
        get() = failedCount > 0
    
    val needsSync: Boolean
        get() = totalPending > 0 && !isSyncing
    
    val isHealthy: Boolean
        get() = !hasFailedOperations && !isSyncing
}
