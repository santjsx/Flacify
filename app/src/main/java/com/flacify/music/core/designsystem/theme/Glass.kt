package com.flacify.music.core.designsystem.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

@Immutable
data class FlacifyGlass(
    val sheetBackground: Brush,
    val dialogBackground: Brush,
    val cardBackground: Brush,
    val chipBackground: Brush,
    val borderLight: BorderStroke,
    val borderSubtle: BorderStroke,
    val topRimHighlight: Brush
)

val DarkGlass = FlacifyGlass(
    sheetBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xF21C1C24),
            Color(0xFA121216)
        )
    ),
    dialogBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xF222222A),
            Color(0xFA16161C)
        )
    ),
    cardBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xCC202028),
            Color(0xE6141418)
        )
    ),
    chipBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0x992C2C36),
            Color(0x991E1E24)
        )
    ),
    borderLight = BorderStroke(
        1.dp,
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.04f)
            )
        )
    ),
    borderSubtle = BorderStroke(
        1.dp,
        Color.White.copy(alpha = 0.08f)
    ),
    topRimHighlight = Brush.horizontalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.05f),
            Color.White.copy(alpha = 0.25f),
            Color.White.copy(alpha = 0.05f)
        )
    )
)

val LightGlass = FlacifyGlass(
    sheetBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xF5FFFFFF),
            Color(0xFAF4F4F8)
        )
    ),
    dialogBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xF8FFFFFF),
            Color(0xFAEDEDF2)
        )
    ),
    cardBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xD9FFFFFF),
            Color(0xE6E8E8EE)
        )
    ),
    chipBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0x80E2E2EA),
            Color(0x80D6D6E0)
        )
    ),
    borderLight = BorderStroke(
        1.dp,
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.60f),
                Color.Black.copy(alpha = 0.06f)
            )
        )
    ),
    borderSubtle = BorderStroke(
        1.dp,
        Color.Black.copy(alpha = 0.06f)
    ),
    topRimHighlight = Brush.horizontalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.4f),
            Color.White.copy(alpha = 0.9f),
            Color.White.copy(alpha = 0.4f)
        )
    )
)

/**
 * Tactile micro-animation modifier: Scales down subtly on touch press and springs back fluidly
 * on release with haptic feedback.
 *
 * Supports both standalone onClick and decoration of existing interactive components (such as
 * IconButton or NavigationBarItem) by non-destructively listening for gestures without event consumption.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.96f,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(isPressed) {
        if (isPressed && enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounceScale"
    )

    val scaledModifier = this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }

    if (onClick != null) {
        val interactionSource = remember { MutableInteractionSource() }
        val clickablePressed by interactionSource.collectIsPressedAsState()
        LaunchedEffect(clickablePressed) {
            isPressed = clickablePressed
        }
        scaledModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
    } else {
        scaledModifier.pointerInput(enabled) {
            if (!enabled) return@pointerInput
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                waitForUpOrCancellation()
                isPressed = false
            }
        }
    }
}

