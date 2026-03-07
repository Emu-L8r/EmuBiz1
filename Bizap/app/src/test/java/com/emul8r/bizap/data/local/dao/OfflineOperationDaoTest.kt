package com.emul8r.bizap.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.entities.OfflineOperation
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class OfflineOperationDaoTest {
    
    private lateinit var db: AppDatabase
    private lateinit var dao: OfflineOperationDao
    
    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.offlineOperationDao()
    }
    
    @After
    fun tearDown() {
        db.close()
    }
    
    @Test
    fun testInsertOperation() = runBlocking {
        val op = OfflineOperation(
            operationType = "CREATE_INVOICE",
            entityId = 1L,
            entityData = "{}",
            businessProfileId = 1L
        )
        
        val id = dao.insert(op)
        assertTrue(id > 0)
    }
    
    @Test
    fun testGetPendingOperations() = runBlocking {
        val op1 = OfflineOperation(
            operationType = "CREATE_INVOICE",
            entityId = 1L,
            entityData = "{}",
            businessProfileId = 1L,
            status = "PENDING"
        )
        
        dao.insert(op1)
        val pending = dao.getPendingOperations(1L)
        assertEquals(1, pending.size)
    }
    
    @Test
    fun testUpdateStatus() = runBlocking {
        val op = OfflineOperation(
            operationType = "CREATE_INVOICE",
            entityId = 1L,
            entityData = "{}",
            businessProfileId = 1L
        )
        
        val id = dao.insert(op)
        dao.updateStatus(id, "SYNCED")
        
        val updated = dao.getById(id)
        assertNotNull(updated)
        assertEquals("SYNCED", updated.status)
    }
}
