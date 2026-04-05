package com.emul8r.bizap.data.service.svg

import android.util.Base64

/**
 * Provides reusable SVG decorative elements for invoice PDF templates.
 *
 * All SVGs are generated as self-contained strings and Base64-encoded for embedding
 * as data URIs in HTML (`<img src="data:image/svg+xml;base64,...">`).
 *
 * All elements are iText7-compatible — they render via the `<img>` tag with a data URI,
 * which iText7's html2pdf module renders correctly without requiring external file I/O.
 */
object SvgElementsProvider {

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generates a wavy horizontal divider SVG.
     *
     * @param color  Hex color string (e.g. "#00C9A7")
     * @param width  SVG viewport width in pixels (default 600)
     * @param height SVG viewport height in pixels (default 20)
     * @return Base64-encoded SVG data URI string ready for use in an `src` attribute
     */
    fun waveDividerDataUri(color: String, width: Int = 600, height: Int = 20): String {
        val mid = height / 2
        val amp = (height / 2) - 2
        val svg = """<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height">
  <path d="M0,$mid Q${width / 8},${mid - amp} ${width / 4},$mid T${width / 2},$mid T${3 * width / 4},$mid T$width,$mid"
        fill="none" stroke="$color" stroke-width="2"/>
</svg>"""
        return toDataUri(svg)
    }

    /**
     * Generates a thin horizontal accent separator line.
     *
     * @param color  Hex color string
     * @param width  SVG viewport width (default 600)
     * @param height SVG viewport height (default 6)
     * @return Base64-encoded SVG data URI
     */
    fun accentLineDataUri(color: String, width: Int = 600, height: Int = 6): String {
        val y = height / 2
        val svg = """<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height">
  <line x1="0" y1="$y" x2="$width" y2="$y" stroke="$color" stroke-width="$height"/>
</svg>"""
        return toDataUri(svg)
    }

    /**
     * Generates a corner decoration (quarter-circle arc) for top-right placement.
     *
     * @param color  Hex color string
     * @param size   Bounding box size in pixels (default 50)
     * @return Base64-encoded SVG data URI
     */
    fun cornerDecorationDataUri(color: String, size: Int = 50): String {
        val svg = """<svg xmlns="http://www.w3.org/2000/svg" width="$size" height="$size" viewBox="0 0 $size $size">
  <path d="M$size,0 L$size,$size L0,$size Z" fill="$color" opacity="0.15"/>
  <path d="M$size,0 Q${size / 2},0 0,$size" fill="none" stroke="$color" stroke-width="2" opacity="0.6"/>
</svg>"""
        return toDataUri(svg)
    }

    /**
     * Generates a simple geometric accent shape (small filled circle row).
     *
     * @param color    Hex color string
     * @param count    Number of circles (default 3)
     * @param radius   Circle radius (default 4)
     * @return Base64-encoded SVG data URI
     */
    fun dotsAccentDataUri(color: String, count: Int = 3, radius: Int = 4): String {
        val spacing = radius * 3
        val totalWidth = count * spacing
        val height = radius * 2 + 4
        val circles = (0 until count).joinToString("\n") { i ->
            val cx = (i * spacing) + radius + 2
            val cy = height / 2
            """  <circle cx="$cx" cy="$cy" r="$radius" fill="$color"/>"""
        }
        val svg = """<svg xmlns="http://www.w3.org/2000/svg" width="$totalWidth" height="$height" viewBox="0 0 $totalWidth $height">
$circles
</svg>"""
        return toDataUri(svg)
    }

    /**
     * Generates a dashed separator line suitable for signature fields.
     *
     * @param color  Hex color string
     * @param width  Line width in pixels (default 200)
     * @return Base64-encoded SVG data URI
     */
    fun dashedLineDataUri(color: String, width: Int = 200): String {
        val height = 10
        val y = height / 2
        val svg = """<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height">
  <line x1="0" y1="$y" x2="$width" y2="$y" stroke="$color" stroke-width="1.5" stroke-dasharray="6,4"/>
</svg>"""
        return toDataUri(svg)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Encodes an SVG string to a Base64 data URI for embedding in HTML `<img>` tags.
     */
    private fun toDataUri(svg: String): String {
        val encoded = Base64.encodeToString(svg.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return "data:image/svg+xml;base64,$encoded"
    }
}
