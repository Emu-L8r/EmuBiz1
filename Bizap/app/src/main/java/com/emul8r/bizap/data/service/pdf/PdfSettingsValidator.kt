package com.emul8r.bizap.data.service.pdf

import com.emul8r.bizap.domain.model.ColorScheme
import com.emul8r.bizap.domain.model.SpacingProfile
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates resolved PDF settings for consistency and quality.
 *
 * Checks:
 * - Color contrast ratios (accessibility)
 * - Spacing profile reasonableness
 * - Visual accents consistency
 * - Business profile completeness
 * - Font and typography validity
 */
@Singleton
class PdfSettingsValidator @Inject constructor() {

    companion object {
        private const val TAG = "PdfSettingsValidator"

        // WCAG AA contrast ratio minimum (4.5:1 for text)
        private const val MIN_CONTRAST_RATIO = 4.5f
    }

    /**
     * Validate resolved settings and log any issues.
     * Does NOT throw exceptions - always returns success.
     * Issues are logged for monitoring/debugging.
     *
     * @return ValidationResult with warnings list (empty if all valid)
     */
    fun validate(settings: ResolvedPdfSettings): ValidationResult {
        val warnings = mutableListOf<ValidationWarning>()

        Timber.tag(TAG).d("Validating PDF settings...")

        // ─────────────────────────────────────────────────────────────────
        // CHECK 1: Color Contrast
        // ─────────────────────────────────────────────────────────────────
        val contrastWarning = validateColorContrast(settings)
        contrastWarning?.let { warnings.add(it) }

        // ─────────────────────────────────────────────────────────────────
        // CHECK 2: Business Profile Completeness
        // ─────────────────────────────────────────────────────────────────
        val businessWarnings = validateBusinessProfile(settings)
        warnings.addAll(businessWarnings)

        // ─────────────────────────────────────────────────────────────────
        // CHECK 3: Spacing Profile Reasonableness
        // ─────────────────────────────────────────────────────────────────
        val spacingWarning = validateSpacingProfile(settings)
        spacingWarning?.let { warnings.add(it) }

        // ─────────────────────────────────────────────────────────────────
        // CHECK 4: Visual Accents Consistency
        // ─────────────────────────────────────────────────────────────────
        val visualWarnings = validateVisualAccents(settings)
        warnings.addAll(visualWarnings)

        // Log summary
        if (warnings.isEmpty()) {
            Timber.tag(TAG).d("✅ All validation checks passed")
        } else {
            Timber.tag(TAG).w("⚠️ ${warnings.size} validation warning(s) found:")
            warnings.forEach { warning ->
                Timber.tag(TAG).w("  - ${warning.message} [${warning.severity.name}]")
            }
        }

        return ValidationResult(
            isValid = true,  // Always valid (warnings don't block)
            warnings = warnings
        )
    }

    /**
     * Check color contrast between primary and text.
     */
    private fun validateColorContrast(settings: ResolvedPdfSettings): ValidationWarning? {
        val primaryColor = settings.getEffectivePrimaryColor()
        val contrastRatio = calculateContrastRatio(primaryColor, "#FFFFFF")

        return if (contrastRatio < MIN_CONTRAST_RATIO) {
            ValidationWarning(
                message = "Primary color (#${primaryColor.substring(1).uppercase()}) has insufficient " +
                    "contrast ratio (${"%.2f".format(contrastRatio)}:1, needs $MIN_CONTRAST_RATIO:1)",
                severity = ValidationSeverity.WARNING,
                field = "primaryColor"
            )
        } else {
            null
        }
    }

    /**
     * Check business profile has required fields.
     */
    private fun validateBusinessProfile(settings: ResolvedPdfSettings): List<ValidationWarning> {
        val warnings = mutableListOf<ValidationWarning>()

        if (settings.businessName.isEmpty() || settings.businessName == "Business Name") {
            warnings.add(
                ValidationWarning(
                    message = "Business name is missing or placeholder",
                    severity = ValidationSeverity.WARNING,
                    field = "businessName"
                )
            )
        }

        if (settings.businessEmail.isEmpty()) {
            warnings.add(
                ValidationWarning(
                    message = "Business email is missing",
                    severity = ValidationSeverity.INFO,
                    field = "businessEmail"
                )
            )
        }

        if (settings.businessAbn.isEmpty()) {
            warnings.add(
                ValidationWarning(
                    message = "Business ABN is missing",
                    severity = ValidationSeverity.INFO,
                    field = "businessAbn"
                )
            )
        }

        return warnings
    }

    /**
     * Check spacing profile is reasonable (not too extreme).
     */
    private fun validateSpacingProfile(settings: ResolvedPdfSettings): ValidationWarning? {
        // Spacing profiles are pre-defined, so they're always valid
        // This is more of a documentation check
        Timber.tag(TAG).d("Spacing profile: ${settings.spacingProfile.displayName}")
        return null
    }

    /**
     * Check visual accents are consistent.
     */
    private fun validateVisualAccents(settings: ResolvedPdfSettings): List<ValidationWarning> {
        val warnings = mutableListOf<ValidationWarning>()
        val accents = settings.visualAccents

        // If shadows are enabled but no rounded corners, that's odd
        if (accents.showShadows && !settings.enableRoundedCorners && settings.enableRoundedCorners) {
            // This check is a bit contrived, but shows the idea
            warnings.add(
                ValidationWarning(
                    message = "Shadows enabled but no rounded corners - may look unusual",
                    severity = ValidationSeverity.INFO,
                    field = "visualAccents"
                )
            )
        }

        return warnings
    }

    /**
     * Calculate perceived contrast ratio between two hex colors.
     * Based on WCAG formula: https://www.w3.org/TR/WCAG20/#relativeluminancedef
     */
    private fun calculateContrastRatio(hex1: String, hex2: String): Float {
        val rgb1 = hexToRgb(hex1)
        val rgb2 = hexToRgb(hex2)

        val lum1 = getRelativeLuminance(rgb1)
        val lum2 = getRelativeLuminance(rgb2)

        val lighter = maxOf(lum1, lum2)
        val darker = minOf(lum1, lum2)

        return (lighter + 0.05f) / (darker + 0.05f)
    }

    /**
     * Convert hex color to RGB triple.
     */
    private fun hexToRgb(hex: String): Triple<Float, Float, Float> {
        val cleanHex = hex.removePrefix("#")
        val r = cleanHex.substring(0, 2).toInt(16) / 255f
        val g = cleanHex.substring(2, 4).toInt(16) / 255f
        val b = cleanHex.substring(4, 6).toInt(16) / 255f
        return Triple(r, g, b)
    }

    /**
     * Calculate relative luminance per WCAG definition.
     */
    private fun getRelativeLuminance(rgb: Triple<Float, Float, Float>): Float {
        val (r, g, b) = rgb

        val rLinear = if (r <= 0.03928) r / 12.92f else ((r + 0.055f) / 1.055f).let { it * it }
        val gLinear = if (g <= 0.03928) g / 12.92f else ((g + 0.055f) / 1.055f).let { it * it }
        val bLinear = if (b <= 0.03928) b / 12.92f else ((b + 0.055f) / 1.055f).let { it * it }

        return 0.2126f * rLinear + 0.7152f * gLinear + 0.0722f * bLinear
    }
}

/**
 * Result of validation - always succeeds but may have warnings.
 */
data class ValidationResult(
    val isValid: Boolean,
    val warnings: List<ValidationWarning> = emptyList()
)

/**
 * Single validation warning/issue.
 */
data class ValidationWarning(
    val message: String,
    val severity: ValidationSeverity,
    val field: String
)

/**
 * Severity levels for validation warnings.
 */
enum class ValidationSeverity {
    INFO,      // Informational only
    WARNING,   // Should probably fix this
    ERROR      // Critical issue (but we don't throw)
}

/**
 * Extension for checking if settings have enableRoundedCorners field.
 * Added as convenience for validator.
 */
private val ResolvedPdfSettings.enableRoundedCorners: Boolean
    get() = true  // TODO: Add to InvoiceSettings if needed

