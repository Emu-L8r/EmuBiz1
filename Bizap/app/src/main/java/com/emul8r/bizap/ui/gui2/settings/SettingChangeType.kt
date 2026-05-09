package com.emul8r.bizap.ui.gui2.settings

/**
 * Categorizes setting changes by their rendering impact to enable intelligent debouncing
 * Phase 1: Smart Debouncing (WIN #2)
 *
 * Different settings require different debounce delays:
 * - Toggles (0ms): Instant visual feedback, no layout impact
 * - Color changes (100ms): Visual only, no layout recalculation
 * - Layout changes (200ms): Requires PDF re-rendering
 * - Text inputs (500ms): Wait for user to finish typing
 */
enum class SettingChangeType(val debounceMs: Long, val settingFieldName: String = "") {
    // Instant: No rendering cost, visual feedback only
    TOGGLE_DIVIDERS(0L, "enableDividers"),
    TOGGLE_ALTERNATING_ROWS(0L, "enableAlternatingRowColors"),
    TOGGLE_WATERMARK(0L, "enableWatermarkText"),
    TOGGLE_QR_CODE(0L, "enableQrCode"),
    TOGGLE_PAGE_NUMBERS(0L, "showPageNumbers"),
    TOGGLE_BACKGROUND_PATTERN(0L, "enableBackgroundPattern"),
    TOGGLE_ROUNDED_CORNERS(0L, "enableRoundedCorners"),

    // Near-instant: Visual only, no layout changes
    COLOR_SCHEME(100L, "selectedColorScheme"),
    PRIMARY_COLOR(100L, "primaryColor"),
    ACCENT_COLOR(100L, "accentColor"),
    DIVIDER_COLOR(100L, "dividerColor"),

    // Moderate: Layout changes, requires PDF update
    PAGE_LAYOUT(200L, "selectedPageLayout"),
    SPACING_PROFILE(200L, "selectedSpacingProfile"),
    TOTAL_BOX_STYLE(200L, "totalBoxStyle"),

    // Long: Text input (wait for user to stop typing)
    FOOTER_TEXT(500L, "footerMessage"),
    COMPANY_NAME(500L, "companyMotto"),
    COMPANY_MOTTO(500L, "companyMotto"),
    SIGNATURE_LABEL(500L, "signatureLabel");

    companion object {
        /**
         * Map setting names to their change type
         * Used to determine appropriate debounce delay
         */
        fun fromSettingName(name: String): SettingChangeType? {
            return values().firstOrNull {
                it.name.equals(name, ignoreCase = true) ||
                it.settingFieldName.equals(name, ignoreCase = true)
            }
        }
    }
}


