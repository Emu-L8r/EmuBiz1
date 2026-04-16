package com.emul8r.bizap.ui.gui3.util

/**
 * MATRIX ANIMATION STANDARD - Single Source of Truth for Animation Settings
 *
 * This enum defines standardized animation intensity values for different screen types,
 * ensuring consistent, immersive Matrix aesthetic throughout GUI3.
 *
 * **Design Philosophy:**
 * - Core action screens (Dashboard): 1.2f (highest immersion, hero screen)
 * - Form & Detail screens: 1.0f-1.1f (readable, immersive but not distracting)
 * - Analytics & Settings: 0.8f-0.9f (subtle, focus on content)
 * - Debug panel: 1.2f (match dashboard for testing)
 *
 * **Intensity Breakdown:**
 * - 1.2f: HERO - Full cascade immersion, maximum presence (Dashboard, Debug)
 * - 1.1f: STRONG - List/Detail views with readable focus
 * - 1.0f: STANDARD - Forms, lighter immersion for data entry
 * - 0.9f: SUBTLE - Analytics, balance content with animation
 * - 0.8f: MINIMAL - Settings, UI configuration (least distracting)
 *
 * @property animationIntensity Float value (0.5-1.5) controlling cascade animation intensity
 *   - <0.8f: Minimal (mostly black, hard to notice)
 *   - 0.8f-0.9f: Subtle (light cascade, focus on content)
 *   - 1.0f-1.1f: Standard/Strong (balanced immersion)
 *   - 1.2f-1.5f: Hero/Maximum (full cyberpunk presence)
 *
 * **How to Use:**
 * ```kotlin
 * @Composable
 * fun MyScreenV3(businessId: Long, navController: NavHostController) {
 *     val screenType = ScreenType.FORM
 *     MatrixBackgroundWrapper(screenType) {
 *         Scaffold(...) { /* Your UI here */ }
 *     }
 * }
 * ```
 *
 * **Adding a New Screen Type:**
 * 1. Add enum value with appropriate intensity
 * 2. Update AGENTS.md Pattern #5 with justification
 * 3. Test on low-end device (Pixel 3a) for 60 FPS
 * 4. Submit PR with before/after screenshots
 */
enum class ScreenType(val animationIntensity: Float) {
    /**
     * Core action hub - Dashboard & primary navigation
     * Highest immersion, deep cascade presence
     * Use: Dashboard, Main entry points
     * Target Frame Time: 12-14ms on Pixel 6a
     */
    CORE_ACTION(1.2f),

    /**
     * Form-based data entry - Create/Edit screens
     * Readable focus while maintaining immersion
     * Use: CreateCustomer, EditInvoice, CreateInvoice, EditCustomer
     * Target Frame Time: 14-16ms on Pixel 6a
     */
    FORM(1.0f),

    /**
     * List views with items - Scrollable collections
     * Strong immersion but scrolling performance priority
     * Use: InvoiceList, CustomerList, PaymentTracking
     * Target Frame Time: 14-16ms on Pixel 6a
     */
    LIST(1.1f),

    /**
     * Detail views - Single item focus
     * Balanced immersion for reading/browsing
     * Use: InvoiceDetail, CustomerDetail
     * Target Frame Time: 14-16ms on Pixel 6a
     */
    DETAIL(1.1f),

    /**
     * Analytics & reporting screens - Content-heavy
     * Minimal animation to avoid chart distraction
     * Use: PaymentAnalytics, RevenueAnalytics, Reports
     * Target Frame Time: 14-16ms on Pixel 6a
     */
    ANALYTICS(0.9f),

    /**
     * Settings & configuration screens
     * Lightest animation, focus on toggles/options
     * Use: Settings, AppSettings
     * Target Frame Time: 14-16ms on Pixel 6a
     */
    SETTINGS(0.8f),

    /**
     * Debug & admin screens (Android Studio/Beta only)
     * Matches Dashboard for immersion feedback
     * Use: MatrixDebugPanel
     * Target Frame Time: 14-16ms on Pixel 6a (not production)
     */
    DEBUG(1.2f),

    /**
     * Utility screens - Help, Vault, etc.
     * Moderate immersion, secondary features
     * Use: Help, Vault, Preferences
     * Target Frame Time: 14-16ms on Pixel 6a
     */
    UTILITY(0.95f);

    /**
     * Human-readable description for logging/debugging
     */
    fun description(): String = when (this) {
        CORE_ACTION -> "Core Action (Hero) - Intensity: 1.2f"
        FORM -> "Form Entry - Intensity: 1.0f"
        LIST -> "List View - Intensity: 1.1f"
        DETAIL -> "Detail View - Intensity: 1.1f"
        ANALYTICS -> "Analytics - Intensity: 0.9f"
        SETTINGS -> "Settings - Intensity: 0.8f"
        DEBUG -> "Debug Panel - Intensity: 1.2f"
        UTILITY -> "Utility Screen - Intensity: 0.95f"
    }
}

/**
 * Configuration constants for Matrix animation behavior
 * Centralized to enable quick tuning and A/B testing
 */
object MatrixAnimationConfig {
    /**
     * Enable/disable Matrix cascade animations globally
     * Useful for performance testing or accessibility
     */
    const val ENABLE_CASCADE_ANIMATION = true

    /**
     * Enable/disable glitch effects globally
     * Can be disabled for less flashy appearance
     */
    const val ENABLE_GLITCH_EFFECTS = true

    /**
     * Maximum cascade animation intensity (safety limit)
     * Prevents runaway intensity values > 1.5f
     */
    const val MAX_INTENSITY = 1.5f

    /**
     * Minimum cascade animation intensity
     * Prevents flat black screens
     */
    const val MIN_INTENSITY = 0.5f

    /**
     * Default glow effect intensity for UI elements
     * Used by GlowingMatrixButton and similar components
     */
    const val DEFAULT_GLOW_INTENSITY = 0.15f

    /**
     * Z-index for background layer (animations)
     * Keep below content layer
     */
    const val Z_INDEX_BACKGROUND = 0f

    /**
     * Z-index for content layer (UI elements, scaffolds)
     * Keep above background layer
     */
    const val Z_INDEX_CONTENT = 1f

    /**
     * Enable adaptive performance (auto-reduce intensity on jank)
     * Set to false for maximum immersion (production)
     * Set to true for device compatibility (beta testing)
     */
    const val ENABLE_ADAPTIVE_PERFORMANCE = false
}

/**
 * Helper function to validate intensity values
 */
fun Float.isValidMatrixIntensity(): Boolean {
    return this >= MatrixAnimationConfig.MIN_INTENSITY &&
            this <= MatrixAnimationConfig.MAX_INTENSITY
}

/**
 * Helper function to clamp intensity to valid range
 */
fun Float.clampMatrixIntensity(): Float {
    return this.coerceIn(
        MatrixAnimationConfig.MIN_INTENSITY,
        MatrixAnimationConfig.MAX_INTENSITY
    )
}

