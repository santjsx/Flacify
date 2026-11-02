package com.aurel.music.core.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay

/**
 * A controlled marquee text component adhering to PRD Section 6.1:
 * - Shows the complete title.
 * - Performs ONE controlled marquee pass if overflowing.
 * - Stops and settles at the beginning.
 * - Never loops indefinitely to prevent visual fatigue.
 */
@Composable
fun MarqueeText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    var containerWidth by remember { mutableIntStateOf(0) }
    var textWidth by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    LaunchedEffect(text, containerWidth, textWidth) {
        if (textWidth > containerWidth && containerWidth > 0) {
            val overflow = textWidth - containerWidth
            // Pause before starting marquee
            delay(1200)
            // Scroll to end over a duration proportional to text length
            val duration = (overflow * 15).coerceIn(1500, 5000)
            scrollState.animateScrollTo(
                value = overflow,
                animationSpec = tween(durationMillis = duration, easing = LinearEasing)
            )
            // Pause at end
            delay(1000)
            // Smoothly return to start and remain resting
            scrollState.animateScrollTo(
                value = 0,
                animationSpec = tween(durationMillis = 600, easing = LinearEasing)
            )
        } else {
            scrollState.scrollTo(0)
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { containerWidth = it.width }
            .semantics { contentDescription = text }
            .horizontalScroll(scrollState, enabled = false)
    ) {
        Text(
            text = text,
            style = style,
            color = color,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = Modifier.onSizeChanged { textWidth = it.width }
        )
    }
}

