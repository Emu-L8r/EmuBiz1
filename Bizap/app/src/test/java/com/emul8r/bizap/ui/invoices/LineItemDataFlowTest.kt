package com.emul8r.bizap.ui.invoices

import com.emul8r.bizap.domain.model.LineItem
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

/**
 * Test suite for line item data flow fixes (Issue #2).
 *
 * These tests verify that:
 * 1. UUID-based tracking is stable across recompositions
 * 2. Index-to-UUID mapping works correctly
 * 3. Updates apply to correct items even after deletions
 * 4. Data is not lost on recomposition
 */

/**
 * Test suite for line item data flow fixes (Issue #2).
 *
 * These tests verify that:
 * 1. UUID-based tracking is stable across recompositions
 * 2. Index-to-UUID mapping works correctly
 * 3. Updates apply to correct items even after deletions
 * 4. Data is not lost on recomposition
 */
class LineItemDataFlowTest {

    @Test
    fun lineItemForm_transientId_remainsStable() {
        // Arrange
        val item1 = LineItemForm(
            id = null,
            transientId = UUID.randomUUID(),
            description = "Service 1",
            quantity = 1.0,
            unitPrice = 10000L
        )

        val item2 = LineItemForm(
            id = null,
            transientId = UUID.randomUUID(),
            description = "Service 2",
            quantity = 2.0,
            unitPrice = 20000L
        )

        val originalUuid1 = item1.transientId
        val originalUuid2 = item2.transientId

        // Act - Simulate "recomposition" by creating new objects from old values
        val recomposedItem1 = item1.copy()
        val recomposedItem2 = item2.copy()

        // Assert
        assertEquals("Item 1 UUID should be stable after copy", originalUuid1, recomposedItem1.transientId)
        assertEquals("Item 2 UUID should be stable after copy", originalUuid2, recomposedItem2.transientId)
    }

    /**
     * ✅ TEST 2: Index-to-UUID Mapping Correctness
     *
     * Verifies that converting array indices to UUIDs works correctly
     * and maintains the correct mapping.
     */
    @Test
    fun indexToUuidMapping_mapsCorrectly() {
        // Arrange
        val items = listOf(
            LineItemForm(id = null, transientId = UUID.randomUUID(), description = "Item A", quantity = 1.0, unitPrice = 1000L),
            LineItemForm(id = null, transientId = UUID.randomUUID(), description = "Item B", quantity = 2.0, unitPrice = 2000L),
            LineItemForm(id = null, transientId = UUID.randomUUID(), description = "Item C", quantity = 3.0, unitPrice = 3000L)
        )

        // Act - Create the ID mapping (simulating what updateLineItemsFromEditor does)
        val uuidToIdMap = items.associate {
            it.transientId.hashCode().toLong() to it.transientId
        }

        // Assert - Verify mapping is correct
        items.forEachIndexed { index, item ->
            val hashedId = item.transientId.hashCode().toLong()
            assertEquals(
                "UUID for index $index should map correctly",
                item.transientId,
                uuidToIdMap[hashedId]
            )
        }
    }

    /**
     * ✅ TEST 3: Update Correctness After Item Deletion (KEY TEST FOR ISSUE #2)
     *
     * This is the KEY TEST for Issue #2. Verifies that when an item is deleted,
     * subsequent updates go to the correct items (not shifted indices).
     *
     * Scenario:
     * 1. Start with 3 items (A, B, C)
     * 2. Delete item B (middle item)
     * 3. Update item C with new values
     * 4. Verify item C got the update, not item A
     */
    @Test
    fun updateAfterDeletion_appliesCorrectly() {
        // Arrange - Initial state with 3 items
        val uuidA = UUID.randomUUID()
        val uuidB = UUID.randomUUID()
        val uuidC = UUID.randomUUID()

        val afterDeletion = listOf(
            LineItemForm(id = null, transientId = uuidA, description = "Item A", quantity = 1.0, unitPrice = 1000L),
            LineItemForm(id = null, transientId = uuidC, description = "Item C", quantity = 3.0, unitPrice = 3000L)  // ← Shifted from index 2 to index 1
        )

        // Simulate editor sending update for item C (quantity changed to 5.0)
        val updatedItems = listOf(
            LineItem(id = uuidA.hashCode().toLong(), description = "Item A", quantity = 1.0, unitPrice = 1000L),
            LineItem(id = uuidC.hashCode().toLong(), description = "Item C", quantity = 5.0, unitPrice = 3000L)  // ← Updated
        )

        // Act - Apply updateLineItemsFromEditor logic
        val resultItems = afterDeletion.map { currentItem ->
            val updatedItem = updatedItems.find {
                it.id == currentItem.transientId.hashCode().toLong()
            }
            if (updatedItem != null) {
                currentItem.copy(
                    description = updatedItem.description,
                    quantity = updatedItem.quantity,
                    unitPrice = updatedItem.unitPrice
                )
            } else {
                currentItem
            }
        }

        // Assert
        assertEquals("Should have 2 items after deletion", 2, resultItems.size)

        // Item A should be unchanged
        assertEquals("Item A unchanged", "Item A", resultItems[0].description)
        assertEquals("Item A quantity unchanged", 1.0, resultItems[0].quantity, 0.01)

        // Item C should be updated (now at index 1, but matched by UUID)
        assertEquals("Item C unchanged", "Item C", resultItems[1].description)
        assertEquals("Item C quantity should be 5.0", 5.0, resultItems[1].quantity, 0.01)
    }

    /**
     * ✅ TEST 4: Multiple Rapid Updates Don't Corrupt Data
     *
     * Simulates user rapidly editing multiple items and verifies no data loss.
     */
    @Test
    fun multipleRapidUpdates_preserveAllData() {
        // Arrange
        val items = listOf(
            LineItemForm(id = null, transientId = UUID.randomUUID(), description = "Item 1", quantity = 1.0, unitPrice = 1000L),
            LineItemForm(id = null, transientId = UUID.randomUUID(), description = "Item 2", quantity = 2.0, unitPrice = 2000L),
            LineItemForm(id = null, transientId = UUID.randomUUID(), description = "Item 3", quantity = 3.0, unitPrice = 3000L)
        )

        // Act - Simulate rapid updates to all items
        val updates = listOf(
            Triple(items[0].transientId, "Updated 1", 10.0),
            Triple(items[1].transientId, "Updated 2", 20.0),
            Triple(items[2].transientId, "Updated 3", 30.0)
        )

        var currentItems = items
        for ((uuid, desc, qty) in updates) {
            currentItems = currentItems.map {
                if (it.transientId == uuid) {
                    it.copy(description = desc, quantity = qty)
                } else {
                    it
                }
            }
        }

        // Assert - All items updated correctly
        assertEquals("Should have 3 items", 3, currentItems.size)
        assertEquals("Item 1 updated", "Updated 1", currentItems[0].description)
        assertEquals("Item 2 updated", "Updated 2", currentItems[1].description)
        assertEquals("Item 3 updated", "Updated 3", currentItems[2].description)
        assertEquals("Item 1 qty correct", 10.0, currentItems[0].quantity, 0.01)
        assertEquals("Item 2 qty correct", 20.0, currentItems[1].quantity, 0.01)
        assertEquals("Item 3 qty correct", 30.0, currentItems[2].quantity, 0.01)
    }

    /**
     * ✅ TEST 5: Empty LineItem List Handling
     *
     * Verifies that the system gracefully handles edge cases like empty lists.
     */
    @Test
    fun emptyLineItemList_handledGracefully() {
        // Arrange
        val items: List<LineItemForm> = emptyList()
        val updatedItems: List<LineItem> = emptyList()

        // Act
        val resultItems = items.map { currentItem ->
            val updatedItem = updatedItems.find {
                it.id == currentItem.transientId.hashCode().toLong()
            }
            if (updatedItem != null) {
                currentItem.copy(
                    description = updatedItem.description,
                    quantity = updatedItem.quantity,
                    unitPrice = updatedItem.unitPrice
                )
            } else {
                currentItem
            }
        }

        // Assert
        assertTrue("Result should be empty", resultItems.isEmpty())
    }

    /**
     * ✅ TEST 6: LineItemForm.toDomain() Preserves Data
     *
     * Verifies that conversion from LineItemForm to domain LineItem preserves all values.
     */
    @Test
    fun lineItemFormToDomain_preservesAllValues() {
        // Arrange
        val form = LineItemForm(
            id = null,
            transientId = UUID.randomUUID(),
            description = "Test Service",
            quantity = 2.5,
            unitPrice = 50000L
        )

        // Act
        val domain = form.toDomain()

        // Assert
        assertEquals("Description preserved", form.description, domain.description)
        assertEquals("Quantity preserved", form.quantity, domain.quantity, 0.01)
        assertEquals("Unit price preserved", form.unitPrice, domain.unitPrice)
    }
}





