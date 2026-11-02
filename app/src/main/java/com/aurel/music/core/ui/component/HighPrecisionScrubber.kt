package com.aurel.music.core.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.aurel.music.core.designsystem.theme.AurelTheme

@Composable
fun HighPrecisionScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgressFraction by remember { mutableFloatStateOf(0f) }
    var trackWidthPx by remember { mutableFloatStateOf(1f) }

    val activeFraction = if (isDragging) {
        dragProgressFraction
    } else {
        if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    }

    val scrubberHeight by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 4.dp,
        label = "scrubberHeight"
    )

    val displayedElapsedMs = if (isDragging) {
        (dragProgressFraction * durationMs).toLong()
    } else {
        currentPositionMs
    }

    val remainingMs = (durationMs - displayedElapsedMs).coerceAtLeast(0L)

    Column(modifier = modifier.fillMaxWidth()) {
        // Scrubber Touch Container (minimum 44dp touch area)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .onSizeChanged { trackWidthPx = it.width.toFloat().coerceAtLeast(1f) }
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                        val seekTarget = (fraction * durationMs).toLong()
                        onSeek(seekTarget)
                    }
                }
                .pointerInput(durationMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgressFraction = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            val seekTarget = (dragProgressFraction * durationMs).toLong()
                            onSeek(seekTarget)
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val deltaFraction = dragAmount / trackWidthPx
                            dragProgressFraction = (dragProgressFraction + deltaFraction).coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Background track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scrubberHeight)
                    .clip(RoundedCornerShape(scrubberHeight / 2))
                    .background(AurelTheme.colors.surfaceVariant)
            )

            // Active progress fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(activeFraction)
                    .height(scrubberHeight)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(scrubberHeight / 2))
                    .background(AurelTheme.colors.primaryText)
            )
        }

        // Time labels (Elapsed vs -Remaining)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(displayedElapsedMs),
                style = AurelTheme.typography.playbackTimer,
                color = AurelTheme.colors.secondaryText
            )
            Text(
                text = "-${formatDuration(remainingMs)}",
                style = AurelTheme.typography.playbackTimer,
                color = AurelTheme.colors.secondaryText
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

