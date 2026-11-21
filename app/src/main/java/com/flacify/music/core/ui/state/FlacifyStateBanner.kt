package com.flacify.music.core.ui.state

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme

/**
 * Universal inline status banner for network, warning, and background notifications.
 */
@Composable
fun FlacifyStateBanner(
    visible: Boolean,
    message: String,
    modifier: Modifier = Modifier,
    explanation: String? = null,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    backgroundColor: Color = FlacifyTheme.colors.surfaceVariant,
    accentColor: Color = FlacifyTheme.colors.secondaryText
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = FlacifyTheme.spacing.md, vertical = FlacifyTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(FlacifyTheme.dimensions.iconSmall)
                )
                Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xs))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    style = FlacifyTheme.typography.caption,
                    color = FlacifyTheme.colors.primaryText
                )
                if (explanation != null) {
                    Text(
                        text = explanation,
                        style = FlacifyTheme.typography.caption,
                        color = FlacifyTheme.colors.secondaryText
                    )
                }
            }

            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(
                        text = actionLabel,
                        style = FlacifyTheme.typography.caption,
                        color = FlacifyTheme.colors.accent
                    )
                }
            }
        }
    }
}

/**
 * Offline State Banner (State 15):
 * Indicates no internet connection and that local/cached data is active.
 */
@Composable
fun FlacifyOfflineBanner(
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    FlacifyStateBanner(
        visible = isOffline,
        message = "You're offline",
        explanation = "Playing from cached device storage.",
        icon = FlacifyIcons.CloudOff,
        backgroundColor = FlacifyTheme.colors.surfaceVariant,
        accentColor = FlacifyTheme.colors.tertiaryText,
        modifier = modifier
    )
}

/**
 * Reconnecting State Banner (State 16):
 * Shows animated pulse while network reconnect is pending.
 */
@Composable
fun FlacifyReconnectingBanner(
    isReconnecting: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryInSeconds: Int? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    FlacifyStateBanner(
        visible = isReconnecting,
        message = if (retryInSeconds != null) "Reconnecting in ${retryInSeconds}s..." else "Restoring connection...",
        explanation = "Checking network reachability.",
        icon = FlacifyIcons.Refresh,
        actionLabel = "Retry Now",
        onAction = onRetry,
        backgroundColor = FlacifyTheme.colors.accent.copy(alpha = 0.12f),
        accentColor = FlacifyTheme.colors.accent.copy(alpha = alpha),
        modifier = modifier
    )
}

/**
 * Rate Limited State Banner (State 34):
 * Informs the user of upstream rate limits with a countdown.
 */
@Composable
fun FlacifyRateLimitedBanner(
    isRateLimited: Boolean,
    retryAfterSeconds: Int,
    modifier: Modifier = Modifier
) {
    FlacifyStateBanner(
        visible = isRateLimited,
        message = "Rate limit reached",
        explanation = "Too many requests. Retrying automatically in ${retryAfterSeconds}s.",
        icon = FlacifyIcons.Warning,
        backgroundColor = FlacifyTheme.colors.warning.copy(alpha = 0.15f),
        accentColor = FlacifyTheme.colors.warning,
        modifier = modifier
    )
}

/**
 * Background Processing Pill (State 7):
 * Subtle indicator showing that a task (scanning, streaming) is active in background.
 */
@Composable
fun FlacifyBackgroundPill(
    isProcessing: Boolean,
    taskName: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bgPulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    AnimatedVisibility(
        visible = isProcessing,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .clip(FlacifyTheme.shapes.small)
                .background(FlacifyTheme.colors.surfaceElevated)
                .padding(horizontal = FlacifyTheme.spacing.xs, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(FlacifyTheme.colors.accent)
                    .alpha(dotAlpha)
            )

            Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xxs))

            Text(
                text = taskName,
                style = FlacifyTheme.typography.caption,
                color = FlacifyTheme.colors.secondaryText
            )
        }
    }
}
