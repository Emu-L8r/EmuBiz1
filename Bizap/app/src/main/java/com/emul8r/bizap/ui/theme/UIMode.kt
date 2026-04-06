package com.emul8r.bizap.ui.theme

/**
 * UI display mode preference.
 * - MODERN: Spacious cards with full details and rich graphics
 * - COMPACT: Dense list views optimised for small screens and power users
 */
enum class UIMode {
    MODERN,
    COMPACT;

    companion object {
        val DEFAULT = MODERN
    }
}
