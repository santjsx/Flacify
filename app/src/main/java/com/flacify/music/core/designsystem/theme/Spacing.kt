package com.flacify.music.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * FLACify Spacing System
 *
 * Strict 4dp-based spacing scale for consistent vertical rhythm and horizontal padding:
 * - XXS: 4dp
 * - XS: 8dp
 * - SM: 12dp
 * - MD: 16dp
 * - LG: 20dp
 * - XL: 24dp
 * - XXL: 32dp
 * - XXXL: 40dp
 * - Huge: 48dp
 */
@Immutable
data class FlacifySpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 40.dp,
    val huge: Dp = 48.dp
)

val DefaultFlacifySpacing = FlacifySpacing()

