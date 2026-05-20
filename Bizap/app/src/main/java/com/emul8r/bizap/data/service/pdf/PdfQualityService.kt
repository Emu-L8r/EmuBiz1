package com.emul8r.bizap.data.service.pdf

import com.emul8r.bizap.domain.model.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

/**
 * Quality validation and scoring service for PDF generation.
 *
 * **Responsibilities:**
 * - Validates invoice settings for potential quality issues
 * - Calculates quality score (0.0 - 1.0)
 * - Estimates PDF metrics (pages, file size, render time)
 * - Generates actionable improvement suggestions
 * - Checks accessibility (WCAG contrast ratios)
 *
 * **Scoring Algorithm:**
 * Starts at 1.0 (perfect score) and deducts for:
 * - ERROR warnings: -0.15 each
 * - WARNING warnings: -0.08 each
 * - INFO warnings: -0.03 each
 *
 * Bonuses awarded for:
 * - Good color contrast: +0.05
 * - Optimal page layout: +0.05
 * - Logo present: +0.05
 *
 * Final score: clamp(0.0, 1.0)
 */
@Singleton
class PdfQualityService @Inject constructor() {

    companion object {
        private const val TAG = "PdfQualityService"

        // WCAG AA contrast ratio minimum (4.5:1 for text)
        private const val MIN_CONTRAST_RATIO = 4.5f

        // Scoring multipliers
        private const val ERROR_PENALTY = 0.15f
        private const val WARNING_PENALTY = 0.08f
        private const val INFO_PENALTY = 0.03f
        private const val GOOD_CONTRAST_BONUS = 0.05f
        private const val OPTIMAL_LAYOUT_BONUS = 0.05f
        private const val LOGO_PRESENT_BONUS = 0.05f
    }

    /**
     * Validate resolved PDF settings for potential quality issues.
     *
     * @param settings The resolved PDF settings to validate
     * @return List of quality warnings (empty if no issues found)
     */
    fun validateSettings(settings: ResolvedPdfSettings): List<PdfQualityWarning> {
        val warnings = mutableListOf<PdfQualityWarning>()

        Timber.tag(TAG).d("🔍 Validating PDF settings...")

        // ─────────────────────────────────────────────────────────────────
        // CHECK 1: Color Contrast (Accessibility)
        // ─────────────────────────────────────────────────────────────────
        val contrastWarning = validateColorContrast(settings.primaryColor, settings.accentColor)
        contrastWarning?.let { warnings.add(it) }

        // ─────────────────────────────────────────────────────────────────
        // CHECK 2: Business Profile Completeness
        // ─────────────────────────────────────────────────────────────────
        val businessWarnings = validateBusinessProfile(settings)
        warnings.addAll(businessWarnings)

        // ─────────────────────────────────────────────────────────────────
        // CHECK 3: Visual Effects Balance
        // ─────────────────────────────────────────────────────────────────
        val effectsWarning = validateVisualEffects(settings)
        effectsWarning?.let { warnings.add(it) }

        // ─────────────────────────────────────────────────────────────────
        // CHECK 4: Layout Optimization
        // ─────────────────────────────────────────────────────────────────
        val layoutWarning = validateLayoutSelection(settings)
        layoutWarning?.let { warnings.add(it) }

        // ─────────────────────────────────────────────────────────────────
        // CHECK 5: Spacing & Typography
        // ─────────────────────────────────────────────────────────────────
        val spacingWarning = validateSpacingProfile(settings)
        spacingWarning?.let { warnings.add(it) }

        // Log summary
        if (warnings.isEmpty()) {
            Timber.tag(TAG).d("✅ All validation checks passed")
        } else {
            Timber.tag(TAG).w("⚠️ ${warnings.size} validation issue(s) found:")
            warnings.forEach { warning ->
                Timber.tag(TAG).w("  [${warning.severity.name}] ${warning.message}")
            }
        }

        return warnings
    }

    /**
     * Calculate overall PDF quality score (0.0 - 1.0).
     * Based on settings validation and best-practice factors.
     */
    fun calculateQualityScore(settings: ResolvedPdfSettings, warnings: List<PdfQualityWarning>): Float {
        var score = 1.0f

        // Deduct for issues
        score -= warnings.count { it.severity == PdfQualitySeverity.ERROR } * ERROR_PENALTY
        score -= warnings.count { it.severity == PdfQualitySeverity.WARNING } * WARNING_PENALTY
        score -= warnings.count { it.severity == PdfQualitySeverity.INFO } * INFO_PENALTY

        // Award bonuses for good practices
        if (hasGoodContrast(settings.primaryColor, settings.accentColor)) {
            score += GOOD_CONTRAST_BONUS
        }
        if (isOptimalLayout(settings.pageLayout)) {
            score += OPTIMAL_LAYOUT_BONUS
        }
        if (settings.businessLogo != null && settings.businessLogo.isNotEmpty()) {
            score += LOGO_PRESENT_BONUS
        }

        return score.coerceIn(0.0f, 1.0f)
    }

    /**
     * Estimate PDF metrics without actual rendering.
     * Used for quick preview feedback.
     */
    fun estimateMetrics(settings: ResolvedPdfSettings, itemCount: Int = 15): PdfMetrics {
        val maxItemsPerPage = getMaxItemsPerPage(settings.pageLayout)
        val pageCount = (itemCount + maxItemsPerPage - 1) / maxItemsPerPage

        val effectCount = countVisualEffects(settings)
        val estimatedFileSizeMb = 0.5f + (pageCount * 0.15f) + (effectCount * 0.05f)
        val renderTimeMs = (pageCount * 200) + (effectCount * 50)

        return PdfMetrics(
            estimatedPageCount = pageCount,
            estimatedFileSizeMb = estimatedFileSizeMb,
            estimatedRenderTimeMs = renderTimeMs,
            itemsPerPage = maxItemsPerPage,
            timestamp = System.currentTimeMillis()
        )
    }

    // ─────────────────────────────────────────────────────────────────
    // Private Validation Helpers
    // ─────────────────────────────────────────────────────────────────

    private fun validateColorContrast(primaryColor: String, accentColor: String): PdfQualityWarning? {
        val contrastRatio = calculateContrastRatio(primaryColor, accentColor)
        return if (contrastRatio < MIN_CONTRAST_RATIO) {
            PdfQualityWarning(
                message = "Color contrast ratio (${String.format("%.2f", contrastRatio)}:1) is below WCAG AA minimum (4.5:1). Text may be hard to read.",
                suggestion = "Choose colors with better contrast. Use online contrast checker at webaim.org/resources/contrastchecker/",
                severity = PdfQualitySeverity.WARNING,
                code = "LOW_CONTRAST"
            )
        } else {
            null
        }
    }

    private fun validateBusinessProfile(settings: ResolvedPdfSettings): List<PdfQualityWarning> {
        val warnings = mutableListOf<PdfQualityWarning>()

        if (settings.businessName.isEmpty() || settings.businessName == "Business Name") {
            warnings.add(
                PdfQualityWarning(
                    message = "Business name is missing or using placeholder",
                    suggestion = "Enter your actual business name for professional appearance",
                    severity = PdfQualitySeverity.WARNING,
                    code = "MISSING_BUSINESS_NAME"
                )
            )
        }

        if (settings.businessEmail.isEmpty()) {
            warnings.add(
                PdfQualityWarning(
                    message = "Business email not configured",
                    suggestion = "Add your email for customer communication",
                    severity = PdfQualitySeverity.INFO,
                    code = "MISSING_EMAIL"
                )
            )
        }

        if (settings.businessPhone.isEmpty()) {
            warnings.add(
                PdfQualityWarning(
                    message = "Business phone not configured",
                    suggestion = "Add your phone number for customer contact",
                    severity = PdfQualitySeverity.INFO,
                    code = "MISSING_PHONE"
                )
            )
        }

        return warnings
    }

    private fun validateVisualEffects(settings: ResolvedPdfSettings): PdfQualityWarning? {
        val effectCount = countVisualEffects(settings)
        return if (effectCount > 8) {
            PdfQualityWarning(
                message = "High number of visual effects ($effectCount) may increase PDF size and complexity",
                suggestion = "Disable non-essential effects (backgrounds, gradients) for simpler, faster PDFs",
                severity = PdfQualitySeverity.INFO,
                code = "TOO_MANY_EFFECTS"
            )
        } else {
            null
        }
    }

    private fun validateLayoutSelection(settings: ResolvedPdfSettings): PdfQualityWarning? {
        // LayoutSelectors handle items per page ratios
        // Warn if layout might not fit typical invoices well
        return null  // No warnings for now - all layouts are valid
    }

    private fun validateSpacingProfile(settings: ResolvedPdfSettings): PdfQualityWarning? {
        // All spacing profiles are pre-defined and valid
        // Could add warnings for extreme spacing in future
        return null
    }

    // ─────────────────────────────────────────────────────────────────
    // Private Helper Methods
    // ─────────────────────────────────────────────────────────────────

    private fun calculateContrastRatio(hex1: String, hex2: String): Float {
        return try {
            val rgb1 = hexToRgb(hex1)
            val rgb2 = hexToRgb(hex2)

            val lum1 = getRelativeLuminance(rgb1)
            val lum2 = getRelativeLuminance(rgb2)

            val lighter = max(lum1, lum2)
            val darker = min(lum1, lum2)

            (lighter + 0.05f) / (darker + 0.05f)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to calculate contrast ratio")
            0.0f  // Return 0 to trigger warning
        }
    }

    private fun hexToRgb(hex: String): Triple<Float, Float, Float> {
        val cleanHex = hex.removePrefix("#")
        val r = cleanHex.substring(0, 2).toInt(16) / 255f
        val g = cleanHex.substring(2, 4).toInt(16) / 255f
        val b = cleanHex.substring(4, 6).toInt(16) / 255f
        return Triple(r, g, b)
    }

    private fun getRelativeLuminance(rgb: Triple<Float, Float, Float>): Float {
        val (r, g, b) = rgb

        val rLinear = if (r <= 0.03928) r / 12.92f else ((r + 0.055f) / 1.055f).let { it * it }
        val gLinear = if (g <= 0.03928) g / 12.92f else ((g + 0.055f) / 1.055f).let { it * it }
        val bLinear = if (b <= 0.03928) b / 12.92f else ((b + 0.055f) / 1.055f).let { it * it }

        return 0.2126f * rLinear + 0.7152f * gLinear + 0.0722f * bLinear
    }

    private fun hasGoodContrast(hex1: String, hex2: String): Boolean {
        return calculateContrastRatio(hex1, hex2) >= MIN_CONTRAST_RATIO
    }

    private fun isOptimalLayout(layout: PageLayout): Boolean {
        return layout == PageLayout.MODERN || layout == PageLayout.ADVANCED_PAGINATED
    }

    private fun countVisualEffects(settings: ResolvedPdfSettings): Int {
        var count = 0
        if (settings.enableGradientHeader) count++
        if (settings.enableAlternatingRowColors) count++
        if (settings.enableDividers) count++
        if (settings.highlightTotals) count++
        if (settings.visualAccents.showShadows) count++
        if (settings.businessLogo != null) count++
        if (settings.visualAccents.showBorders) count++
        if (settings.visualAccents.showWatermark) count++
        return count
    }

    private fun getMaxItemsPerPage(layout: PageLayout): Int {
        return when (layout) {
            PageLayout.COMPACT -> 20
            PageLayout.MODERN -> 12
            PageLayout.MINIMAL -> 15
            PageLayout.CLASSIC -> 10
            PageLayout.ADVANCED_PAGINATED -> 15
            PageLayout.SIDEBAR -> 8
            else -> 12
        }
    }
}

/**
 * Single quality warning/issue with severity and actionable suggestion.
 */
data class PdfQualityWarning(
    val message: String,
    val suggestion: String,
    val severity: PdfQualitySeverity,
    val code: String
)

/**
 * Severity levels for quality warnings.
 */
enum class PdfQualitySeverity {
    ERROR,      // Critical issue, should fix before export
    WARNING,    // Should probably fix
    INFO        // Nice to have, informational only
}

/**
 * Estimated PDF metrics for preview/feedback.
 */
data class PdfMetrics(
    val estimatedPageCount: Int,
    val estimatedFileSizeMb: Float,
    val estimatedRenderTimeMs: Int,
    val itemsPerPage: Int,
    val timestamp: Long
)

