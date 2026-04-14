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
    fun `GuiMode enum has exactly three values GUI1, GUI2, and GUI3`() {
        val values = GuiMode.entries
        assertEquals(3, values.size)
        assertEquals(GuiMode.GUI1, values[0])
        assertEquals(GuiMode.GUI2, values[1])
        assertEquals(GuiMode.GUI3, values[2])
    }

    @Test
        assertNotEquals(GuiMode.GUI1, GuiMode.GUI2)
    }
        assertNotEquals(GuiMode.GUI2, GuiMode.GUI3)
        assertNotEquals(GuiMode.GUI3, GuiMode.GUI1)

    @Test
    fun `GuiMode valueOf round-trips correctly`() {
    }
}

        assertEquals(GuiMode.GUI3, GuiMode.valueOf("GUI3"))


