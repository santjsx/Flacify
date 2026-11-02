package com.flacify.music.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * FLACify Dimensions & Sizing System
 *
 * Centralized component dimensions and icon sizing tokens.
 */
@Immutable
data class FlacifyDimensions(
    // Component Heights & Targets
    val minTouchTarget: Dp = 48.dp,
    val miniPlayerHeight: Dp = 64.dp,
    val bottomNavHeight: Dp = 64.dp,
    val songThumbnailSize: Dp = 48.dp,
    val albumCardThumbnailSize: Dp = 160.dp,
    val artistAvatarSize: Dp = 48.dp,
    val playButtonHeroSize: Dp = 72.dp,
    val skipButtonSize: Dp = 44.dp,
    val scrubberTrackRestingHeight: Dp = 4.dp,
    val scrubberTrackDraggingHeight: Dp = 8.dp,
    val scrubberTouchTargetHeight: Dp = 44.dp,

    // Icon Sizing Tokens (Calibrated with Lucide 2.5px stroke & 36px/24px geometry)
    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 20.dp,
    val iconRegular: Dp = 24.dp,
    val iconLarge: Dp = 36.dp,
    val iconHero: Dp = 48.dp,
    val iconHuge: Dp = 72.dp
)

val DefaultFlacifyDimensions = FlacifyDimensions()

