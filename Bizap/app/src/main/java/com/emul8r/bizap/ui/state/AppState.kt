package com.emul8r.bizap.ui.state

import com.emul8r.bizap.ui.landing.GuiMode

/**
 * Single source of truth for the top-level UI state of the app.
 *
 * MainActivity observes [AppStateViewModel.appState] and renders exactly one
 * composable based on the current state, eliminating the previous multi-layer
 * conditional structure that caused multiple visible screen transitions on launch.
 */
sealed class AppState {
    /** DataStore preferences are still loading — show branded splash screen. */
    data object SplashLoading : AppState()

    /** No PIN has been configured yet — show PIN setup screen. */
    data object PINSetup : AppState()

    /**
     * Session expired, locked out, or invalid PIN — show login screen.
     */
    data object Login : AppState()

    /**
     * Authenticated but first-launch data-loss warning has not been acknowledged.
     * Show [com.emul8r.bizap.ui.onboarding.FirstLaunchWarningDialog].
     */
    data object FirstLaunchWarning : AppState()

    /**
     * Authenticated, warning acknowledged, but no GUI mode saved yet.
     * Show [com.emul8r.bizap.ui.landing.LandingScreen] so the user can pick GUI1 or GUI2.
     */
    data object GUISelection : AppState()

    /**
     * Authenticated, warning acknowledged, and a GUI mode has been persisted.
     * Render the chosen GUI directly.
     *
     * @param gui The GUI mode to display ([GuiMode.GUI1] or [GuiMode.GUI2]).
     */
    data class AppReady(val gui: GuiMode) : AppState()
}
