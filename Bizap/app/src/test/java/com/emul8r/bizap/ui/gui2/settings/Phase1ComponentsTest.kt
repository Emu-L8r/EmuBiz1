package com.emul8r.bizap.ui.gui2.settings

import com.emul8r.bizap.domain.model.InvoiceSettings
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Phase 1 components:
 * - WIN #1: Settings grouping
 * - WIN #2: Change detection
 * - WIN #3: Preset application
 * - WIN #4: Change tracking
 */
class Phase1ComponentsTest {

    // ─────────────────────────────────────────────────────────────────
    // WIN #1: SETTINGS GROUPING
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun testSettingsGroupEnumValues() {
        val groups = SettingsGroup.all()
        assertEquals("Should have 3 groups", 3, groups.size)

        assertTrue("Should include LAYOUT_STRUCTURE",
            groups.any { it.type == SettingGroupType.LAYOUT_STRUCTURE })
        assertTrue("Should include COLORS_APPEARANCE",
            groups.any { it.type == SettingGroupType.COLORS_APPEARANCE })
        assertTrue("Should include ADVANCED_OPTIONS",
            groups.any { it.type == SettingGroupType.ADVANCED_OPTIONS })
    }

    @Test
    fun testSettingsGroupByType() {
        val group = SettingsGroup.byType(SettingGroupType.LAYOUT_STRUCTURE)
        assertNotNull("Should find group by type", group)
        assertEquals("Layout & Structure", group?.title)
    }

    // ─────────────────────────────────────────────────────────────────
    // WIN #2: SETTING CHANGE TYPES (DEBOUNCING)
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun testSettingChangeTypeDebounceValues() {
        assertEquals("Toggles should be instant (0ms)", 0L, SettingChangeType.TOGGLE_DIVIDERS.debounceMs)
        assertEquals("Colors should be fast (100ms)", 100L, SettingChangeType.COLOR_SCHEME.debounceMs)
        assertEquals("Layout should be moderate (200ms)", 200L, SettingChangeType.PAGE_LAYOUT.debounceMs)
        assertEquals("Text input should be long (500ms)", 500L, SettingChangeType.FOOTER_TEXT.debounceMs)
    }

    @Test
    fun testSettingChangeTypeFromName() {
        val changeType = SettingChangeType.fromSettingName("FOOTER_TEXT")
        assertEquals("Should find by name", SettingChangeType.FOOTER_TEXT, changeType)
    }

    // ─────────────────────────────────────────────────────────────────
    // WIN #3: CHANGE TRACKER
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun testChangeTrackerDetectsNoChanges() {
        val settings = InvoiceSettings.default("user123")
        val tracker = ChangeTracker.create(settings, settings)

        assertFalse("Should not show changes for identical settings", tracker.hasChanges)
        assertEquals("Changed field count should be 0", 0, tracker.changeCount)
    }

    @Test
    fun testChangeTrackerDetectsChanges() {
        val original = InvoiceSettings.default("user123")
        val changed = original.copy(enableDividers = !original.enableDividers)

        val tracker = ChangeTracker.create(original, changed)

        assertTrue("Should detect changes", tracker.hasChanges)
        assertTrue("Should contain enableDividers change", tracker.changedFields.contains("enableDividers"))
        assertEquals("Should have at least 1 change", 1, tracker.changeCount)
    }

    @Test
    fun testChangeTrackerGroupFiltering() {
        val original = InvoiceSettings.default("user123")

        // Simulate changes to layout group
        val changed = original.copy(
            enableDividers = !original.enableDividers,
            enableAlternatingRowColors = !original.enableAlternatingRowColors
        )

        val tracker = ChangeTracker.create(original, changed)

        val layoutGroupCount = tracker.getGroupChangeCount(SettingGroupType.LAYOUT_STRUCTURE)
        assertTrue("Layout group should have changes", layoutGroupCount > 0)
    }

    @Test
    fun testChangeTrackerEmpty() {
        val tracker = ChangeTracker.empty()

        assertFalse("Empty tracker should have no changes", tracker.hasChanges)
        assertEquals("Empty tracker should have no fields changed", 0, tracker.changeCount)
    }

    // ─────────────────────────────────────────────────────────────────
    // WIN #4: INVOICE PRESETS
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun testInvoicePresetsExist() {
        val presets = com.emul8r.bizap.domain.model.InvoicePreset.all()
        assertEquals("Should have 4 presets", 4, presets.size)

        assertNotNull("Should have Professional preset",
            com.emul8r.bizap.domain.model.InvoicePreset.byId("professional"))
        assertNotNull("Should have Modern preset",
            com.emul8r.bizap.domain.model.InvoicePreset.byId("modern"))
        assertNotNull("Should have Minimal preset",
            com.emul8r.bizap.domain.model.InvoicePreset.byId("minimal"))
        assertNotNull("Should have Creative preset",
            com.emul8r.bizap.domain.model.InvoicePreset.byId("creative"))
    }

    @Test
    fun testPresetApplication() {
        val original = InvoiceSettings.default("user123")
        val professional = com.emul8r.bizap.domain.model.InvoicePreset.Professional

        val applied = professional.applyTo(original)

        // Professional preset should set specific values
        assertNotNull("Preset should update settings", applied)
        // Verify key settings were changed
        assertTrue("Preset should configure layout", applied != original)
    }

    @Test
    fun testDefaultPreset() {
        val defaultPreset = com.emul8r.bizap.domain.model.InvoicePreset.default()
        assertEquals("Default should be Professional",
            com.emul8r.bizap.domain.model.InvoicePreset.Professional, defaultPreset)
    }
}

