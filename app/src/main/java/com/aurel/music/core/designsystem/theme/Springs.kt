package com.aurel.music.core.designsystem.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Calibrated spring physics specifications for physical, interruptible motion.
 * Damped harmonic motion: m*x'' + c*x' + k*x = 0
 */
object AurelSprings {
    /**
     * Immediate, non-bouncy press response for touch-down feedback.
     */
    val QuickPress: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )

    /**
     * Responsive modal sheet and mini-player expansion.
     */
    val SheetSlide: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    /**
     * Fluid layout transitions and queue reordering.
     */
    val FluidTransition: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /**
     * Artwork album cover transition between songs.
     */
    val ArtworkTransition: SpringSpec<Float> = spring(
        dampingRatio = 0.85f,
        stiffness = 350f
    )
}
