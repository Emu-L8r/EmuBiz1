package com.emul8r.bizap.gui2

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.ui.landing.GuiMode
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Unit tests for the [GuiMode] enum — GUI selection on the landing screen.
 */
class GuiModeTest : BaseUnitTest() {

    @Test
    fun `GuiMode has exactly three values`() {
        assertEquals(3, GuiMode.entries.size)
    }

    @Test
    fun `GuiMode entries are GUI1, GUI2, GUI3 in order`() {
        val values = GuiMode.entries
        assertEquals(GuiMode.GUI1, values[0])
        assertEquals(GuiMode.GUI2, values[1])
        assertEquals(GuiMode.GUI3, values[2])
    }

    @Test
    fun `GuiMode values are distinct`() {
        assertNotEquals(GuiMode.GUI1, GuiMode.GUI2)
        assertNotEquals(GuiMode.GUI2, GuiMode.GUI3)
        assertNotEquals(GuiMode.GUI3, GuiMode.GUI1)
    }

    @Test
    fun `GuiMode valueOf round-trips correctly`() {
        assertEquals(GuiMode.GUI1, GuiMode.valueOf("GUI1"))
        assertEquals(GuiMode.GUI2, GuiMode.valueOf("GUI2"))
        assertEquals(GuiMode.GUI3, GuiMode.valueOf("GUI3"))
    }

    @Test
    fun `GuiMode name matches ordinal`() {
        assertEquals("GUI1", GuiMode.GUI1.name)
        assertEquals("GUI2", GuiMode.GUI2.name)
        assertEquals("GUI3", GuiMode.GUI3.name)
    }

    @Test
    fun `GuiMode ordinals are sequential`() {
        assertEquals(0, GuiMode.GUI1.ordinal)
        assertEquals(1, GuiMode.GUI2.ordinal)
        assertEquals(2, GuiMode.GUI3.ordinal)
    }

    @Test
    fun `GuiMode can be used in when expression exhaustively`() {
        val description = when (GuiMode.GUI3) {
            GuiMode.GUI1 -> "legacy"
            GuiMode.GUI2 -> "modern"
            GuiMode.GUI3 -> "matrix"
        }
        assertEquals("matrix", description)
    }

    @Test
    fun `GuiMode entries list is not empty`() {
        assertTrue(GuiMode.entries.isNotEmpty())
    }
}

