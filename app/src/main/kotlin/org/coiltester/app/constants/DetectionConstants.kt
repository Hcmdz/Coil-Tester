package org.coiltester.app.constants

import androidx.compose.ui.graphics.Color

/**
 * Centralized detection constants — avoids magic numbers throughout the codebase.
 */
object DetectionConstants {
    // ── Colors ──────────────────────────────────────────────
    val DC_DETECTION_COLOR = Color(0xFFFFA500)
    val ERROR_COLOR = Color(0xFFFF3B30)

    // ── Animation ───────────────────────────────────────────
    const val FRAME_DELAY_MS = 16L
    const val ROTATION_INCREMENT_DEG = 2.88f
    const val FULL_ROTATION_DEG = 360f
    const val BUTTON_WIDTH_FRACTION = 0.7f

    // ── Sampling rates ──────────────────────────────────────
    const val SAMPLING_RATE_20 = 20
    const val SAMPLING_RATE_64 = 64
    const val INJECT_AC_LOW = 0.5
    const val INJECT_DC_HIGH = 150.0
    const val INJECT_SAMPLE_COUNT = 100
    const val INJECT_BASE_DC = 48.76

    // ── UI ──────────────────────────────────────────────────
    const val SECTION_LETTER_SPACING = 0.5
}
