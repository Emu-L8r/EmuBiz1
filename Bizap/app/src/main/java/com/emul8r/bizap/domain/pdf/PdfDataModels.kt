package com.emul8r.bizap.domain.pdf

/**
 * Header section of a PDF document
 *
 * Optional section that appears at the top of the PDF.
 * Can be a company name, project name, or other header text.
 */
data class HeaderSection(
    val text: String = "",
    val fontSize: Float = 14f,
    val isBold: Boolean = false
)

/**
 * Subheader section of a PDF document
 *
 * Supporting information that appears below the header.
 * Usually contains details like location, department, shop number, etc.
 */
data class SubheaderSection(
    val lines: List<String> = emptyList()
) {
    companion object {
        fun from(vararg items: String): SubheaderSection {
            return SubheaderSection(items.toList())
        }
    }
}

/**
 * Color scheme for PDF styling
 *
 * Defines the colors used in PDF rendering.
 * Can be extracted from app theme or customized by user.
 */
data class PdfColorScheme(
    val primary: String = "#6750A4",              // Primary brand color
    val secondary: String = "#625B71",            // Secondary color
    val accent: String = "#7D5260",               // Accent/highlight color
    val text: String = "#1C1B1F",                 // Main text color
    val textLight: String = "#49454E",            // Secondary text color
    val background: String = "#FFFBFE",           // Background color
    val border: String = "#CAC7D0",               // Border/divider color
    val success: String = "#4CAF50",              // Success state (e.g., "Paid")
    val warning: String = "#FF9800",              // Warning state
    val error: String = "#B3261E"                 // Error state
)

