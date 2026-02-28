package com.emul8r.bizap.data

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.entities.PendingOperation
import com.emul8r.bizap.data.sync.ConflictResolver
import com.emul8r.bizap.data.sync.ConflictWinner
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit tests for Task 9B Offline Sync Engine.
 */
class SyncTest : BaseUnitTest() {
    
    private val conflictResolver = ConflictResolver()

    @Test
    fun `test conflict resolution server wins when newer`() {
        val result = conflictResolver.resolve(
            localUpdatedAt = 1000L,
            serverUpdatedAt = 2000L
        )
        assertEquals(ConflictWinner.SERVER, result)
    }

    @Test
    fun `test conflict resolution local wins when newer`() {
        val result = conflictResolver.resolve(
            localUpdatedAt = 3000L,
            serverUpdatedAt = 2000L
        )
        assertEquals(ConflictWinner.LOCAL, result)
    }

    @Test
    fun `test pending operation status flags`() {
        val op = PendingOperation(
            operationType = "CREATE",
            entityType = "INVOICE",
            entityId = 1L, // Corrected parameter
            businessProfileId = 1L,
            payload = "{}",
            status = "PENDING"
        )
        assertEquals(true, op.isPending())
        assertEquals(false, op.isSynced())
    }
}
