package com.flacify.music.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * FLACify Elevation & Surface Tokens
 */
@Immutable
data class FlacifyElevation(
    val none: Dp = 0.dp,
    val low: Dp = 4.dp,
    val medium: Dp = 8.dp,
    val high: Dp = 16.dp,
    val playerSurface: Dp = 24.dp
)

val DefaultFlacifyElevation = FlacifyElevation()

