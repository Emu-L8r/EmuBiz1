package com.emul8r.bizap.data.sync

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit contract for background data synchronization.
 */
interface SyncService {
    
    @POST("sync/operations")
    suspend fun syncPendingOperations(@Body payload: List<String>): SyncResponse
}

data class SyncResponse(
    val success: Boolean,
    val syncedIds: List<String>,
    val serverTimestamp: Long
)
