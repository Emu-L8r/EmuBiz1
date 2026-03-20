package com.emul8r.bizap.ui.landing

/**
 * GUI selection mode persisted via DataStore.
 * Users choose between the legacy GUI1 and the new GUI2 on the landing screen.
 */
enum class GuiMode {
    /** Legacy GUI — original app experience, kept as-is. */
    GUI1,
    /** New GUI2 — clean, context-aware architecture built from scratch. */
    GUI2
}
