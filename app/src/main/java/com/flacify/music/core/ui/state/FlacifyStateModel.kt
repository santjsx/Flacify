package com.flacify.music.core.ui.state

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Severity level of a system state for visual accentuation.
 */
enum class StateSeverity {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
    DESTRUCTIVE
}

/**
 * Unified state model implementing Vikas's UX law:
 * Every state must answer:
 * 1. What happened?
 * 2. Why it happened?
 * 3. What the user can do next?
 */
@Immutable
data class FlacifyStateData(
    val whatHappened: String,
    val whyItHappened: String,
    val primaryActionLabel: String? = null,
    val onPrimaryAction: (() -> Unit)? = null,
    val secondaryActionLabel: String? = null,
    val onSecondaryAction: (() -> Unit)? = null,
    val icon: ImageVector? = null,
    val severity: StateSeverity = StateSeverity.INFO
)
