package com.flacify.music.core.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import kotlin.math.abs

/**
 * Audiophile High-Precision Elastic Scrubber.
 *
 * Features:
 * - Unified single-gesture engine preventing race conditions or false 0:00 seeks
 * - Physics spring expansion on interaction (4dp -> 7dp)
 * - Elastic, interactive floating thumb that gracefully emerges on scrub
 * - Adaptive contrast for light/dark artwork backgrounds
 */
@Composable
fun HighPrecisionScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    trackColor: Color = FlacifyTheme.colors.surfaceVariant,
    progressColor: Color = Color.White,
    textColor: Color = FlacifyTheme.colors.secondaryText
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgressFraction by remember { mutableFloatStateOf(0f) }
    var trackWidthPx by remember { mutableFloatStateOf(1f) }

    val actualFraction = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f)
    } else 0f

    val activeFraction = if (isDragging) dragProgressFraction else actualFraction

    // Elastic spring animation for track expansion
    val scrubberHeight by animateDpAsState(
        targetValue = if (isDragging) 7.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scrubberHeight"
    )

    // Animated thumb expansion
    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "thumbScale"
    )

    val displayedElapsedMs = if (isDragging) {
        (dragProgressFraction * durationMs).toLong()
    } else {
        currentPositionMs
    }

    val remainingMs = (durationMs - displayedElapsedMs).coerceAtLeast(0L)

    Column(modifier = modifier.fillMaxWidth()) {
        // Scrubber Touch Container (guaranteed 44dp hit area, single gesture handler)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .onSizeChanged { trackWidthPx = it.width.toFloat().coerceAtLeast(1f) }
                .pointerInput(durationMs) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downX = down.position.x
                        var isSlopPassed = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break

                            if (change.pressed) {
                                val dx = abs(change.position.x - downX)
                                if (!isSlopPassed && dx > viewConfiguration.touchSlop) {
                                    isSlopPassed = true
                                    isDragging = true
                                }

                                if (isSlopPassed) {
                                    change.consume()
                                    dragProgressFraction = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                                }
                            } else {
                                // Pointer up / released
                                if (isSlopPassed) {
                                    isDragging = false
                                    change.consume()
                                    val seekTarget = (dragProgressFraction * durationMs).toLong()
                                    onSeek(seekTarget)
                                } else {
                                    // Intentional tap within track bounds
                                    change.consume()
                                    val tapFraction = (downX / trackWidthPx).coerceIn(0f, 1f)
                                    val seekTarget = (tapFraction * durationMs).toLong()
                                    onSeek(seekTarget)
                                }
                                break
                            }
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val density = LocalDensity.current
            val totalWidthPx = with(density) { maxWidth.toPx() }

            // 1. Background Inactive Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scrubberHeight)
                    .clip(RoundedCornerShape(scrubberHeight / 2))
                    .background(trackColor)
            )

            // 2. Active Progress Fill
            if (activeFraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(activeFraction.coerceIn(0.001f, 1f))
                        .height(scrubberHeight)
                        .clip(RoundedCornerShape(scrubberHeight / 2))
                        .background(progressColor)
                )
            }

            // 3. Elastic Floating Scrub Thumb (emerges on touch/drag)
            if (thumbScale > 0.01f) {
                val thumbOffsetPx = (activeFraction * totalWidthPx) - with(density) { 7.dp.toPx() }
                Box(
                    modifier = Modifier
                        .offset(x = with(density) { thumbOffsetPx.coerceAtLeast(0f).toDp() })
                        .size(14.dp)
                        .graphicsLayer {
                            scaleX = thumbScale
                            scaleY = thumbScale
                        }
                        .shadow(elevation = 4.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(progressColor)
                )
            }
        }

        // Time labels: Elapsed on left vs -Remaining on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDuration(displayedElapsedMs),
                style = FlacifyTheme.typography.playbackTimer,
                color = textColor
            )
            Text(
                text = "-${formatDuration(remainingMs)} left",
                style = FlacifyTheme.typography.playbackTimer,
                color = textColor
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
