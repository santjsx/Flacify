package com.flacify.music.core.ui.component

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * State machine enforcing the two anti-flicker loader rules:
 * Rule 1: Delay the loader by [delayMs] (default 300ms). If data loads under 300ms, spinner NEVER shows.
 * Rule 2: If the loader appears, keep it visible for at least [minDurationMs] (default 500ms) minimum.
 *
 * Designed with reactive snapshotFlow and collectLatest to prevent cancellation bugs
 * when loading transitions quickly.
 */
@Composable
fun rememberSmoothLoaderVisibility(
    isLoading: Boolean,
    delayMs: Long = 300L,
    minDurationMs: Long = 500L
): State<Boolean> {
    val visibleState = remember { mutableStateOf(false) }
    val isLoadingState = rememberUpdatedState(isLoading)

    LaunchedEffect(Unit) {
        var shownAt = 0L
        snapshotFlow { isLoadingState.value }
            .distinctUntilChanged()
            .collectLatest { loading ->
                if (loading) {
                    if (!visibleState.value) {
                        delay(delayMs.coerceAtLeast(0L))
                        if (isLoadingState.value) {
                            shownAt = SystemClock.elapsedRealtime()
                            visibleState.value = true
                        }
                    }
                } else {
                    if (visibleState.value) {
                        val elapsed = SystemClock.elapsedRealtime() - shownAt
                        val remaining = minDurationMs.coerceAtLeast(0L) - elapsed
                        if (remaining > 0L) {
                            delay(remaining)
                        }
                        visibleState.value = false
                    }
                }
            }
    }

    return visibleState
}

/**
 * Premium glassmorphic circular loader with smooth accent gradient glow.
 */
@Composable
fun SmoothHandoffLoader(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 3.dp,
    text: String? = null
) {
    val transition = rememberInfiniteTransition(label = "loaderPulse")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseAlpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(size + 16.dp)
                .clip(FlacifyTheme.shapes.circle)
                .background(FlacifyTheme.colors.surfaceVariant.copy(alpha = 0.5f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val accent = Color(0xFFFA2D48)
                val gradient = Brush.sweepGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.05f),
                        accent.copy(alpha = 0.40f * pulseAlpha),
                        accent
                    )
                )

                drawArc(
                    brush = gradient,
                    startAngle = rotation,
                    sweepAngle = 280f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        if (!text.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.sm))
            Text(
                text = text,
                style = FlacifyTheme.typography.caption,
                color = FlacifyTheme.colors.secondaryText
            )
        }
    }
}

/**
 * Container that wraps content and seamlessly crossfades with [SmoothHandoffLoader]
 * adhering strictly to the 300ms delay & 500ms min display duration.
 */
@Composable
fun SmoothLoaderContainer(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    loadingText: String? = null,
    delayMs: Long = 300L,
    minDurationMs: Long = 500L,
    content: @Composable () -> Unit
) {
    val shouldShowLoader by rememberSmoothLoaderVisibility(
        isLoading = isLoading,
        delayMs = delayMs,
        minDurationMs = minDurationMs
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        content()

        AnimatedVisibility(
            visible = shouldShowLoader,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(250))
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(FlacifyTheme.colors.background.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                SmoothHandoffLoader(text = loadingText)
            }
        }
    }
}

