package com.emul8r.bizap.ui.landing

/**
 * GUI selection mode persisted via DataStore.
 * Users choose between GUI1 (legacy), GUI2 (modern), or GUI3 (Matrix) on the landing screen.
 */
enum class GuiMode {
    /** Legacy GUI — original app experience, kept as-is. */
    GUI1,
    /** New GUI2 — clean, context-aware architecture built from scratch. */
    GUI2,
    /** Matrix GUI3 — cyberpunk green-on-dark aesthetic, premium experience. */
    GUI3
}
