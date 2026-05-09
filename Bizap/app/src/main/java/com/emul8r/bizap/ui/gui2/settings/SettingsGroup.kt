package com.emul8r.bizap.ui.gui2.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Categorizes invoice settings into logical groups for better organization
 * Phase 1: Smart Settings Grouping (WIN #1)
 */
enum class SettingGroupType {
    LAYOUT_STRUCTURE,
    COLORS_APPEARANCE,
    ADVANCED_OPTIONS
}

data class SettingsGroup(
    val type: SettingGroupType,
    val title: String,
    val icon: ImageVector,
    val description: String
) {
    companion object {
        fun all() = listOf(
            SettingsGroup(
                SettingGroupType.LAYOUT_STRUCTURE,
                "Layout & Structure",
                Icons.Default.ViewWeek,
                "Page layout, spacing, and row styling"
            ),
            SettingsGroup(
                SettingGroupType.COLORS_APPEARANCE,
                "Colors & Appearance",
                Icons.Default.Palette,
                "Color schemes and visual styling"
            ),
            SettingsGroup(
                SettingGroupType.ADVANCED_OPTIONS,
                "Advanced Options",
                Icons.Default.Settings,
                "Additional customization options"
            )
        )

        fun byType(type: SettingGroupType): SettingsGroup? {
            return all().find { it.type == type }
        }
    }
}

