package com.emul8r.bizap.data.repository

import com.emul8r.bizap.domain.model.InvoiceSettings
import timber.log.Timber

/**
 * Preset manager for PDF settings profiles.
 *
 * Allows users to:
 * - Save current settings as named preset
 * - Load preset and apply to current settings
 * - Manage multiple presets
 * - Delete presets
 * - List all available presets
 */
class SettingsPresetManager {

    companion object {
        // Preset names for defaults
        const val PRESET_PROFESSIONAL = "Professional"
        const val PRESET_MODERN = "Modern"
        const val PRESET_MINIMAL = "Minimal"
        const val PRESET_CREATIVE = "Creative"
    }

    // In-memory preset storage (could be expanded to database)
    private val presets = mutableMapOf<String, InvoiceSettings>()

    init {
        // Initialize with default presets
        loadDefaultPresets()
    }

    /**
     * Save current settings as a preset.
     *
     * @param presetName Name of preset to save
     * @param settings Settings to save
     * @return true if saved successfully
     */
    fun savePreset(presetName: String, settings: InvoiceSettings): Boolean {
        return try {
            presets[presetName] = settings.copy()
            Timber.d("✅ Preset saved: $presetName")
            Timber.d("   Theme: ${settings.selectedTheme}")
            Timber.d("   HTML Style: ${settings.selectedHtmlStyle}")
            true
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to save preset: $presetName")
            false
        }
    }

    /**
     * Load preset settings.
     *
     * @param presetName Name of preset to load
     * @return Settings from preset, or null if not found
     */
    fun loadPreset(presetName: String): InvoiceSettings? {
        return presets[presetName]?.also {
            Timber.d("✅ Preset loaded: $presetName")
            Timber.d("   Theme: ${it.selectedTheme}")
            Timber.d("   HTML Style: ${it.selectedHtmlStyle}")
        } ?: run {
            Timber.w("⚠️ Preset not found: $presetName")
            null
        }
    }

    /**
     * Get list of all available presets.
     *
     * @return List of preset names
     */
    fun getAvailablePresets(): List<String> {
        return presets.keys.sorted().also {
            Timber.d("📋 Available presets: $it")
        }
    }

    /**
     * Delete a preset.
     *
     * @param presetName Name of preset to delete
     * @return true if deleted successfully
     */
    fun deletePreset(presetName: String): Boolean {
        return try {
            presets.remove(presetName)
            Timber.d("🗑️ Preset deleted: $presetName")
            true
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to delete preset: $presetName")
            false
        }
    }

    /**
     * Check if preset exists.
     *
     * @param presetName Name to check
     * @return true if preset exists
     */
    fun presetExists(presetName: String): Boolean {
        return presets.containsKey(presetName)
    }

    /**
     * Get preset count.
     *
     * @return Number of available presets
     */
    fun getPresetCount(): Int {
        return presets.size
    }

    /**
     * Load default presets.
     *
     * These are standard presets provided with the app.
     */
    private fun loadDefaultPresets() {
        // Add example presets for users to learn from
        Timber.d("📦 Loading default presets...")
        // Note: In production, these would be created from a template
    }

    /**
     * Export preset to JSON (for backup/sharing).
     *
     * @param presetName Name of preset to export
     * @return JSON string representation
     */
    fun exportPreset(presetName: String): String? {
        return presets[presetName]?.let { settings ->
            try {
                // Simple JSON representation
                """
                {
                    "name": "$presetName",
                    "theme": "${settings.selectedTheme}",
                    "htmlStyle": "${settings.selectedHtmlStyle}",
                    "pdfEngine": "${settings.selectedPdfEngine}",
                    "pageLayout": "${settings.selectedPageLayout}",
                    "exportedAt": "${System.currentTimeMillis()}"
                }
                """.trimIndent()
            } catch (e: Exception) {
                Timber.e(e, "Failed to export preset: $presetName")
                null
            }
        }
    }

    /**
     * Clear all presets (for testing/reset).
     */
    fun clearAllPresets() {
        presets.clear()
        Timber.d("🔄 All presets cleared")
    }

    /**
     * Duplicate an existing preset.
     *
     * @param originalName Original preset name
     * @param newName New preset name
     * @return true if duplicated successfully
     */
    fun duplicatePreset(originalName: String, newName: String): Boolean {
        return presets[originalName]?.let { original ->
            savePreset(newName, original.copy())
        } ?: run {
            Timber.w("⚠️ Cannot duplicate: preset '$originalName' not found")
            false
        }
    }

    /**
     * Rename a preset.
     *
     * @param oldName Current preset name
     * @param newName New preset name
     * @return true if renamed successfully
     */
    fun renamePreset(oldName: String, newName: String): Boolean {
        return presets[oldName]?.let { settings ->
            presets.remove(oldName)
            presets[newName] = settings
            Timber.d("✏️ Preset renamed: $oldName → $newName")
            true
        } ?: run {
            Timber.w("⚠️ Cannot rename: preset '$oldName' not found")
            false
        }
    }

    /**
     * Get preset description.
     *
     * @param presetName Name of preset
     * @return Human-readable description of preset settings
     */
    fun getPresetDescription(presetName: String): String {
        return presets[presetName]?.let { settings ->
            """
            Theme: ${settings.selectedTheme.name}
            Style: ${settings.selectedHtmlStyle.displayName}
            Layout: ${settings.selectedPageLayout}
            """.trimIndent()
        } ?: "Preset not found"
    }
}

