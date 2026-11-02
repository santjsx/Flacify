package com.flacify.music.core.designsystem.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable

/**
 * FLACify Motion & Animation Tokens
 *
 * Provides standardized duration, easing curves, and spring specs
 * for purposeful, lightweight, and fluid micro-interactions.
 */
@Immutable
data class FlacifyMotion(
    val durationFast: Int = 150,
    val durationNormal: Int = 250,
    val durationSlow: Int = 380,

    val fastTween: AnimationSpec<Float> = tween(durationMillis = 150, easing = FastOutSlowInEasing),
    val normalTween: AnimationSpec<Float> = tween(durationMillis = 250, easing = FastOutSlowInEasing),
    val slowTween: AnimationSpec<Float> = tween(durationMillis = 380, easing = FastOutSlowInEasing),

    val springGentle: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    ),
    val springResponsive: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )
)

val DefaultFlacifyMotion = FlacifyMotion()

