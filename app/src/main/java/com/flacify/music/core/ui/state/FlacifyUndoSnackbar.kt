package com.flacify.music.core.ui.state

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import kotlinx.coroutines.delay

/**
 * Deleted State Snackbar with Undo Option (State 32):
 * Displays a 5-second countdown window allowing users to reverse destructive actions.
 */
@Composable
fun FlacifyUndoSnackbar(
    visible: Boolean,
    message: String,
    onUndo: () -> Unit,
    onDismissTimeout: () -> Unit,
    modifier: Modifier = Modifier,
    durationMs: Long = 5000L
) {
    var progress by remember(visible) { mutableFloatStateOf(1f) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = durationMs.toInt(),
            easing = LinearEasing
        ),
        label = "undoCountdown"
    )

    LaunchedEffect(visible) {
        if (visible) {
            progress = 0f
            delay(durationMs)
            onDismissTimeout()
        } else {
            progress = 1f
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FlacifyTheme.spacing.md, vertical = FlacifyTheme.spacing.sm)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FlacifyTheme.colors.surfaceElevated)
                    .padding(horizontal = FlacifyTheme.spacing.md, vertical = FlacifyTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(24.dp),
                        color = FlacifyTheme.colors.accent,
                        trackColor = FlacifyTheme.colors.surfaceVariant,
                        strokeWidth = 2.dp
                    )
                    Icon(
                        imageVector = FlacifyIcons.Delete,
                        contentDescription = null,
                        tint = FlacifyTheme.colors.accent,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = message,
                        style = FlacifyTheme.typography.songTitle,
                        color = FlacifyTheme.colors.primaryText
                    )
                    Text(
                        text = "Tap Undo to restore immediately.",
                        style = FlacifyTheme.typography.caption,
                        color = FlacifyTheme.colors.secondaryText
                    )
                }

                TextButton(
                    onClick = onUndo,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Undo",
                        style = FlacifyTheme.typography.primaryButton,
                        color = FlacifyTheme.colors.accent
                    )
                }
            }
        }
    }
}
