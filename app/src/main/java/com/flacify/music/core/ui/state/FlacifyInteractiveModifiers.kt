package com.flacify.music.core.ui.state

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.flacify.music.core.designsystem.theme.FlacifyTheme

/**
 * Focus Ring Modifier (State 27):
 * Renders an accessible 2dp glowing accent ring when the element receives keyboard, D-pad, or TV focus.
 */
fun Modifier.flacifyFocusRing(
    shape: Shape = RoundedCornerShape(8.dp),
    focusBorderWidth: Dp = 2.dp
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    this
        .focusable(interactionSource = interactionSource)
        .then(
            if (isFocused) {
                Modifier.border(
                    width = focusBorderWidth,
                    color = FlacifyTheme.colors.accent,
                    shape = shape
                )
            } else {
                Modifier
            }
        )
}

/**
 * Hover Elevation Modifier (State 25):
 * Subtly elevates background brightness when a mouse pointer or stylus hovers over the element.
 * Fully compatible with touch screens (inactive during standard touch).
 */
fun Modifier.flacifyHover(
    shape: Shape = RoundedCornerShape(8.dp),
    hoverColor: Color? = null
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val resolvedHoverColor = hoverColor ?: FlacifyTheme.colors.surfaceElevated.copy(alpha = 0.5f)

    val animatedBg by animateColorAsState(
        targetValue = if (isHovered) resolvedHoverColor else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "hoverColor"
    )

    this
        .hoverable(interactionSource = interactionSource)
        .clip(shape)
        .background(animatedBg)
}

/**
 * Selected Item Modifier (State 28):
 * Highlights a row or card when selected in multi-selection mode without shifting layouts.
 */
fun Modifier.flacifySelected(
    isSelected: Boolean,
    shape: Shape = RoundedCornerShape(8.dp)
): Modifier = composed {
    val selectedBg = FlacifyTheme.colors.accent.copy(alpha = 0.12f)
    val selectedBorder = FlacifyTheme.colors.accent.copy(alpha = 0.4f)

    if (isSelected) {
        this
            .clip(shape)
            .background(selectedBg)
            .border(width = 1.dp, color = selectedBorder, shape = shape)
    } else {
        this
    }
}
