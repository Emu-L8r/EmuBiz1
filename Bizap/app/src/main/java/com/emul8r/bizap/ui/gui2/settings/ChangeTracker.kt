package com.emul8r.bizap.ui.gui2.settings

import com.emul8r.bizap.domain.model.InvoiceSettings

/**
 * Tracks changes between original and current settings
 * Phase 1: Visual Change Indicators (WIN #3)
 *
 * Provides:
 * - hasChanges: Boolean flag
 * - changedFields: Set of field names that changed
 * - changeCount: Number of changes
 * - getChangedFieldsInGroup: Filter changes by setting group
 */
data class ChangeTracker(
    val originalSettings: InvoiceSettings?,
    val currentSettings: InvoiceSettings?
) {
    val hasChanges: Boolean
        get() = originalSettings != currentSettings

    val changedFields: Set<String>
        get() {
            if (originalSettings == null || currentSettings == null) return emptySet()

            val changes = mutableSetOf<String>()

            // Compare key settings
            if (originalSettings.selectedColorScheme != currentSettings.selectedColorScheme) {
                changes.add("selectedColorScheme")
            }
            if (originalSettings.selectedSpacingProfile != currentSettings.selectedSpacingProfile) {
                changes.add("selectedSpacingProfile")
            }
            if (originalSettings.selectedPageLayout != currentSettings.selectedPageLayout) {
                changes.add("selectedPageLayout")
            }
            if (originalSettings.totalBoxStyle != currentSettings.totalBoxStyle) {
                changes.add("totalBoxStyle")
            }
            if (originalSettings.enableDividers != currentSettings.enableDividers) {
                changes.add("enableDividers")
            }
            if (originalSettings.enableAlternatingRowColors != currentSettings.enableAlternatingRowColors) {
                changes.add("enableAlternatingRowColors")
            }
            if (originalSettings.footerMessage != currentSettings.footerMessage) {
                changes.add("footerMessage")
            }
            if (originalSettings.primaryColor != currentSettings.primaryColor) {
                changes.add("primaryColor")
            }
            if (originalSettings.accentColor != currentSettings.accentColor) {
                changes.add("accentColor")
            }

            return changes
        }

    val changeCount: Int
        get() = changedFields.size

    /**
     * Get count of changes in a specific settings group
     */
    fun getGroupChangeCount(groupType: SettingGroupType): Int {
        return when (groupType) {
            SettingGroupType.LAYOUT_STRUCTURE -> {
                changedFields.count { field ->
                    field in setOf(
                        "selectedPageLayout",
                        "selectedSpacingProfile",
                        "enableAlternatingRowColors"
                    )
                }
            }
            SettingGroupType.COLORS_APPEARANCE -> {
                changedFields.count { field ->
                    field in setOf(
                        "selectedColorScheme",
                        "totalBoxStyle",
                        "primaryColor",
                        "accentColor"
                    )
                }
            }
            SettingGroupType.ADVANCED_OPTIONS -> {
                changedFields.count { field ->
                    field in setOf(
                        "enableDividers",
                        "enableAlternatingRowColors",
                        "footerMessage"
                    )
                }
            }
        }
    }

    /**
     * Check if a specific field was changed
     */
    fun isFieldChanged(fieldName: String): Boolean {
        return changedFields.contains(fieldName)
    }

    companion object {
        /**
         * Create a tracker from current and original settings
         */
        fun create(
            original: InvoiceSettings?,
            current: InvoiceSettings?
        ): ChangeTracker {
            return ChangeTracker(original, current)
        }

        /**
         * Create an empty tracker (no changes)
         */
        fun empty(): ChangeTracker {
            return ChangeTracker(null, null)
        }
    }
}






