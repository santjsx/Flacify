package com.flacify.music.core.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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

    // Immediately reset scroll position when text changes to a new song
    LaunchedEffect(text) {
        scrollState.scrollTo(0)
    }

    // Controlled single scroll pass: Scroll once to reveal full text, then rest at start
    LaunchedEffect(text, containerWidth, textWidth) {
        if (textWidth > containerWidth && containerWidth > 0) {
            scrollState.scrollTo(0)
            val overflow = textWidth - containerWidth
            // 1. Initial pause so user can read the start of the title
            delay(1200)
            // 2. Controlled linear scroll to show the rest of the text
            val duration = (overflow * 15).coerceIn(1500, 6000)
            scrollState.animateScrollTo(
                value = overflow,
                animationSpec = tween(durationMillis = duration, easing = LinearEasing)
            )
            // 3. Pause at the end so the user can read the version/remix/subtitle
            delay(1200)
            // 4. Smoothly return to the start
            scrollState.animateScrollTo(
                value = 0,
                animationSpec = tween(durationMillis = 800, easing = LinearEasing)
            )
            // 5. Rest indefinitely at the start (no looping, no truncation with "...")
        } else {
            scrollState.scrollTo(0)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
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

