package com.emul8r.bizap.data.pdf

import com.emul8r.bizap.domain.model.InvoiceSettings
import timber.log.Timber

/**
 * Utility for injecting InvoiceSettings colors into HTML CSS variables.
 *
 * Converts InvoiceSettings branding colors into CSS custom properties that override
 * the default color scheme in invoice-styles.css.
 *
 * **Features:**
 * - Dynamic color injection from InvoiceSettings
 * - Color validation (hex, rgb, named colors)
 * - Fallback to defaults if colors are invalid
 * - Seamless CSS variable integration
 *
 * **Usage:**
 * ```kotlin
 * val injector = CssVariableInjector()
 * val htmlWithColors = injector.injectColorVariables(htmlContent, settings)
 * ```
 */
object CssVariableInjector {
    private const val TAG = "CssVariableInjector"

    /**
     * Default CSS variables if customization is not provided.
     *
     * These match the default values in invoice-styles.css :root block.
     */
    private val DEFAULT_COLORS = mapOf(
        "--primary-color" to "#6B4C9A",
        "--secondary-color" to "#f5f5f5",
        "--accent-color" to "#2c3e50",
        "--text-color" to "#333333",
        "--border-color" to "#e0e0e0"
    )

    /**
     * Inject brand colors from InvoiceSettings into HTML content.
     *
     * Creates a dynamic style block with CSS variable overrides that customize
     * the invoice appearance based on user-configured brand colors.
     *
     * **Process:**
     * 1. Validate color format from settings
     * 2. Build CSS variable override block
     * 3. Inject into HTML <head> before default stylesheet
     * 4. Falls back to defaults if any color is invalid
     *
     * @param htmlContent Original HTML invoice content
     * @param settings InvoiceSettings containing brand color customization
     * @return HTML with injected CSS variable overrides
     */
    fun injectColorVariables(htmlContent: String, settings: InvoiceSettings): String {
        return try {
            Timber.d("Injecting CSS color variables from InvoiceSettings")

            // Map settings colors to CSS variables
            val colorMap = buildColorMap(settings)

            // Create dynamic style block
            val styleBlock = buildDynamicStyleBlock(colorMap)

            // Inject into HTML head (before stylesheet link for proper cascade)
            val injectedHtml = htmlContent.replace(
                "</head>",
                "$styleBlock</head>",
                ignoreCase = true
            )

            Timber.d("CSS variables injected successfully")
            injectedHtml

        } catch (e: Exception) {
            Timber.e(e, "Error injecting CSS variables, using original HTML")
            htmlContent
        }
    }

    /**
     * Build CSS color variable map from InvoiceSettings.
     *
     * Maps user-configured brand colors to CSS variable names.
     * Validates colors and applies fallbacks if needed.
     *
     * @param settings InvoiceSettings with color configuration
     * @return Map of CSS variable name to color value
     */
    private fun buildColorMap(settings: InvoiceSettings): Map<String, String> {
        val colorMap = mutableMapOf<String, String>()

        // PRIMARY COLOR - Core brand color
        val primaryColor = if (settings.primaryColor.isNotBlank() && isValidColor(settings.primaryColor)) {
            settings.primaryColor
        } else {
            DEFAULT_COLORS["--primary-color"]!!
        }
        colorMap["--primary-color"] = primaryColor
        Timber.d("Primary color: $primaryColor")

        // SECONDARY COLOR - Background highlights
        val secondaryColor = if (settings.secondaryColor.isNotBlank() && isValidColor(settings.secondaryColor)) {
            settings.secondaryColor
        } else {
            DEFAULT_COLORS["--secondary-color"]!!
        }
        colorMap["--secondary-color"] = secondaryColor
        Timber.d("Secondary color: $secondaryColor")

        // ACCENT COLOR - Text and borders
        val accentColor = if (settings.accentColor.isNotBlank() && isValidColor(settings.accentColor)) {
            settings.accentColor
        } else {
            DEFAULT_COLORS["--accent-color"]!!
        }
        colorMap["--accent-color"] = accentColor
        Timber.d("Accent color: $accentColor")

        return colorMap
    }

    /**
     * Validate color format (hex, rgb, or named).
     *
     * Supports:
     * - Hex colors: #fff, #ffffff
     * - RGB colors: rgb(255, 255, 255)
     * - Named colors: white, blue, red, etc.
     *
     * @param color Color string to validate
     * @return true if color format is valid, false otherwise
     */
    private fun isValidColor(color: String): Boolean {
        val trimmed = color.trim()

        // Hex color validation (#fff or #ffffff)
        if (trimmed.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"))) {
            return true
        }

        // RGB color validation
        if (trimmed.matches(Regex("^rgb\\(\\d{1,3},\\s?\\d{1,3},\\s?\\d{1,3}\\)$"))) {
            return true
        }

        // Named color validation (basic CSS color names)
        val namedColors = setOf(
            "white", "black", "red", "green", "blue", "yellow",
            "cyan", "magenta", "gray", "grey", "silver", "maroon",
            "navy", "olive", "purple", "teal", "lime", "aqua"
        )

        if (trimmed.lowercase() in namedColors) {
            return true
        }

        Timber.w("Invalid color format: $color")
        return false
    }

    /**
     * Build dynamic CSS style block with variable overrides.
     *
     * Creates a <style> tag that overrides :root CSS variables with
     * values from InvoiceSettings, allowing per-invoice customization.
     *
     * @param colorMap Map of CSS variable names to color values
     * @return HTML style block with CSS variable overrides
     */
    private fun buildDynamicStyleBlock(colorMap: Map<String, String>): String {
        val variableDeclarations = colorMap.entries.joinToString("\n    ") { (name, color) ->
            "$name: $color;"
        }

        return """
            |<style>
            |    :root {
            |        $variableDeclarations
            |    }
            |</style>
        """.trimMargin()
    }

    /**
     * Validate all InvoiceSettings color fields.
     *
     * Checks that color fields contain valid CSS color values.
     * Used before PDF generation to catch issues early.
     *
     * @param settings InvoiceSettings to validate
     * @return List of validation errors (empty if valid)
     */
    fun validateColors(settings: InvoiceSettings): List<String> {
        val errors = mutableListOf<String>()

        if (settings.primaryColor.isNotBlank() && !isValidColor(settings.primaryColor)) {
            errors.add("Invalid primary color format: ${settings.primaryColor}")
        }

        if (settings.secondaryColor.isNotBlank() && !isValidColor(settings.secondaryColor)) {
            errors.add("Invalid secondary color format: ${settings.secondaryColor}")
        }

        if (settings.accentColor.isNotBlank() && !isValidColor(settings.accentColor)) {
            errors.add("Invalid accent color format: ${settings.accentColor}")
        }

        if (errors.isNotEmpty()) {
            Timber.w("Color validation failed: ${errors.joinToString(", ")}")
        }

        return errors
    }
}


