package com.emul8r.bizap.domain.manager

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Unit tests for BusinessContextManager.
 * Verifies that the singleton correctly tracks the active business ID across navigation.
 */
class BusinessContextManagerTest : BaseUnitTest() {

    private lateinit var manager: BusinessContextManager

    @Before
    fun setup() {
        val mockRepository = mockk<BusinessProfileRepository>()
        manager = BusinessContextManager(mockRepository)
    }

    @Test
    fun `initial state has null businessId`() = runTest {
        assertNull(manager.getActiveBusinessId(), "Initial state should be null")
        assertNull(manager.activeBusinessId.first(), "Flow should emit null initially")
    }

    @Test
    fun `setActiveBusinessId updates the active business ID`() {
        manager.setActiveBusinessId(42L)
        assertEquals(42L, manager.getActiveBusinessId())
    }

    @Test
    fun `activeBusinessId flow emits updated value after setActiveBusinessId`() = runTest {
        manager.setActiveBusinessId(99L)
        assertEquals(99L, manager.activeBusinessId.first())
    }

    @Test
    fun `setActiveBusinessId correctly switches between businesses`() {
        manager.setActiveBusinessId(1L)
        assertEquals(1L, manager.getActiveBusinessId())

        manager.setActiveBusinessId(2L)
        assertEquals(2L, manager.getActiveBusinessId(), "Should switch to new business ID")
    }

    @Test
    fun `requireActiveBusinessId returns value when set`() {
        manager.setActiveBusinessId(123L)
        assertEquals(123L, manager.requireActiveBusinessId())
    }

    @Test
    fun `requireActiveBusinessId throws when no context is set`() {
        assertFailsWith<IllegalStateException> {
            manager.requireActiveBusinessId()
        }
    }

    @Test
    fun `clearActiveBusinessId resets to null`() {
        manager.setActiveBusinessId(55L)
        manager.clearActiveBusinessId()
        assertNull(manager.getActiveBusinessId(), "After clear, businessId should be null")
    }

    @Test
    fun `requireActiveBusinessId throws after clearActiveBusinessId`() {
        manager.setActiveBusinessId(77L)
        manager.clearActiveBusinessId()
        assertFailsWith<IllegalStateException> {
            manager.requireActiveBusinessId()
        }
    }
}



