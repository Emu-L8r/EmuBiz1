package com.emul8r.bizap.gui2

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.ui.landing.GuiMode
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Unit tests for the GUI2 landing / mode selection system.
 */
class GuiModeTest : BaseUnitTest() {

    @Test
    fun `GuiMode enum has exactly two values GUI1 and GUI2`() {
        val values = GuiMode.entries
        assertEquals(2, values.size)
        assertEquals(GuiMode.GUI1, values[0])
        assertEquals(GuiMode.GUI2, values[1])
    }

    @Test
    fun `GuiMode values are distinct`() {
        assertNotEquals(GuiMode.GUI1, GuiMode.GUI2)
    }

    @Test
    fun `GuiMode valueOf round-trips correctly`() {
        assertEquals(GuiMode.GUI1, GuiMode.valueOf("GUI1"))
        assertEquals(GuiMode.GUI2, GuiMode.valueOf("GUI2"))
    }
}
