package com.flacify.music.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * FLACify Border Radius System
 *
 * Centralized reusable corner-radius tokens:
 * - None: 0dp (sharp structural elements)
 * - Micro: 4dp (indicators, subtle chips)
 * - Small: 8dp (small controls, compact items, song rows)
 * - Medium: 12dp (buttons, compact surfaces, dialog elements)
 * - Large: 16dp (cards, standard containers, album tiles)
 * - Extra Large: 24dp (prominent player surfaces, bottom sheets)
 * - Pill: 999dp / 50% (pills and capsule controls)
 * - Circle: 50% circular (artwork avatars, playback hero button)
 */
@Immutable
data class FlacifyShapes(
    val none: RoundedCornerShape = RoundedCornerShape(0.dp),
    val micro: RoundedCornerShape = RoundedCornerShape(4.dp),
    val small: RoundedCornerShape = RoundedCornerShape(8.dp),
    val medium: RoundedCornerShape = RoundedCornerShape(12.dp),
    val large: RoundedCornerShape = RoundedCornerShape(16.dp),
    val extraLarge: RoundedCornerShape = RoundedCornerShape(24.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(999.dp),
    val circle: Shape = CircleShape
)

val DefaultFlacifyShapes = FlacifyShapes()
