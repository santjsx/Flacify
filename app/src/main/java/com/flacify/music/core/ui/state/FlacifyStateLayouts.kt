package com.flacify.music.core.ui.state

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme

/**
 * Standard Empty State Layout (State 2 / State 22):
 * Displays icon hero, headline, explanation, and clear recovery action.
 */
@Composable
fun FlacifyEmptyStateLayout(
    title: String,
    explanation: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = FlacifyIcons.MusicNote,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = FlacifyTheme.spacing.xxl, vertical = FlacifyTheme.spacing.huge),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(FlacifyTheme.dimensions.playButtonHeroSize)
                    .clip(FlacifyTheme.shapes.medium)
                    .background(FlacifyTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = FlacifyTheme.colors.accent,
                    modifier = Modifier.size(FlacifyTheme.dimensions.iconLarge)
                )
            }

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))

            Text(
                text = title,
                style = FlacifyTheme.typography.heading2,
                color = FlacifyTheme.colors.primaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xxs))

            Text(
                text = explanation,
                style = FlacifyTheme.typography.bodyMedium,
                color = FlacifyTheme.colors.secondaryText,
                textAlign = TextAlign.Center
            )

            if (primaryActionLabel != null && onPrimaryAction != null) {
                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))
                Button(
                    onClick = onPrimaryAction,
                    colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                    shape = FlacifyTheme.shapes.medium
                ) {
                    Text(
                        text = primaryActionLabel,
                        color = FlacifyTheme.colors.primaryText,
                        style = FlacifyTheme.typography.primaryButton
                    )
                }
            }

            if (secondaryActionLabel != null && onSecondaryAction != null) {
                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xs))
                TextButton(onClick = onSecondaryAction) {
                    Text(
                        text = secondaryActionLabel,
                        color = FlacifyTheme.colors.secondaryText,
                        style = FlacifyTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Standard Error State Layout (State 3 / State 17):
 * Explains what failed, root cause, and provides retry/fallback actions.
 */
@Composable
fun FlacifyErrorStateLayout(
    title: String,
    explanation: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = "Try Again",
    icon: ImageVector = FlacifyIcons.ErrorCircle,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = FlacifyTheme.spacing.xxl, vertical = FlacifyTheme.spacing.huge),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(FlacifyTheme.dimensions.playButtonHeroSize)
                    .clip(FlacifyTheme.shapes.medium)
                    .background(FlacifyTheme.colors.error.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = FlacifyTheme.colors.error,
                    modifier = Modifier.size(FlacifyTheme.dimensions.iconLarge)
                )
            }

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))

            Text(
                text = title,
                style = FlacifyTheme.typography.heading2,
                color = FlacifyTheme.colors.primaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xxs))

            Text(
                text = explanation,
                style = FlacifyTheme.typography.bodyMedium,
                color = FlacifyTheme.colors.secondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                shape = FlacifyTheme.shapes.medium
            ) {
                Text(
                    text = retryLabel,
                    color = FlacifyTheme.colors.primaryText,
                    style = FlacifyTheme.typography.primaryButton
                )
            }

            if (secondaryActionLabel != null && onSecondaryAction != null) {
                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xs))
                TextButton(onClick = onSecondaryAction) {
                    Text(
                        text = secondaryActionLabel,
                        color = FlacifyTheme.colors.secondaryText,
                        style = FlacifyTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Standard Progress State Layout (State 5 / State 19 / State 20):
 * Displays progress percentage, transfer status, file size, and cancellation action.
 */
@Composable
fun FlacifyProgressStateLayout(
    title: String,
    statusText: String,
    modifier: Modifier = Modifier,
    progress: Float? = null, // null for indeterminate
    onCancel: (() -> Unit)? = null,
    cancelLabel: String = "Cancel"
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = FlacifyTheme.spacing.xxl, vertical = FlacifyTheme.spacing.huge),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = FlacifyTheme.typography.heading2,
                color = FlacifyTheme.colors.primaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xs))

            Text(
                text = statusText,
                style = FlacifyTheme.typography.bodyMedium,
                color = FlacifyTheme.colors.secondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))

            if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = FlacifyTheme.colors.accent,
                    trackColor = FlacifyTheme.colors.surfaceVariant
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = FlacifyTheme.colors.accent,
                    trackColor = FlacifyTheme.colors.surfaceVariant
                )
            }

            if (onCancel != null) {
                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))
                OutlinedButton(
                    onClick = onCancel,
                    shape = FlacifyTheme.shapes.medium
                ) {
                    Text(
                        text = cancelLabel,
                        color = FlacifyTheme.colors.primaryText,
                        style = FlacifyTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Standard Permission Denied Layout (State 8):
 * Explains *why* permission is needed for lossless music discovery and directs to Android App Settings.
 */
@Composable
fun FlacifyPermissionStateLayout(
    permissionName: String,
    explanation: String,
    modifier: Modifier = Modifier,
    onRetryRequest: (() -> Unit)? = null
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = FlacifyTheme.spacing.xxl, vertical = FlacifyTheme.spacing.huge),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(FlacifyTheme.dimensions.playButtonHeroSize)
                    .clip(FlacifyTheme.shapes.medium)
                    .background(FlacifyTheme.colors.warning.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = FlacifyIcons.Shield,
                    contentDescription = null,
                    tint = FlacifyTheme.colors.warning,
                    modifier = Modifier.size(FlacifyTheme.dimensions.iconLarge)
                )
            }

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))

            Text(
                text = "$permissionName Permission Required",
                style = FlacifyTheme.typography.heading2,
                color = FlacifyTheme.colors.primaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xxs))

            Text(
                text = explanation,
                style = FlacifyTheme.typography.bodyMedium,
                color = FlacifyTheme.colors.secondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))

            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                shape = FlacifyTheme.shapes.medium
            ) {
                Text(
                    text = "Open App Settings",
                    color = FlacifyTheme.colors.primaryText,
                    style = FlacifyTheme.typography.primaryButton
                )
            }

            if (onRetryRequest != null) {
                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xs))
                TextButton(onClick = onRetryRequest) {
                    Text(
                        text = "Check Permission Again",
                        color = FlacifyTheme.colors.secondaryText,
                        style = FlacifyTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Standard Partial Success Card (State 6):
 * Shows count of successful items alongside skipped/failed items with an expandable detail view.
 */
@Composable
fun FlacifyPartialSuccessCard(
    successCount: Int,
    skippedCount: Int,
    skippedDetails: List<String>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(FlacifyTheme.shapes.medium)
            .background(FlacifyTheme.colors.surfaceVariant)
            .padding(FlacifyTheme.spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = FlacifyIcons.Warning,
                contentDescription = null,
                tint = FlacifyTheme.colors.warning,
                modifier = Modifier.size(FlacifyTheme.dimensions.iconMedium)
            )

            Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$successCount tracks added, $skippedCount skipped",
                    style = FlacifyTheme.typography.songTitle,
                    color = FlacifyTheme.colors.primaryText
                )
                Text(
                    text = "Some files were corrupted or in an unsupported format.",
                    style = FlacifyTheme.typography.caption,
                    color = FlacifyTheme.colors.secondaryText
                )
            }

            TextButton(onClick = { isExpanded = !isExpanded }) {
                Text(
                    text = if (isExpanded) "Hide" else "Details",
                    color = FlacifyTheme.colors.accent,
                    style = FlacifyTheme.typography.caption
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = FlacifyTheme.spacing.sm)
            ) {
                skippedDetails.take(5).forEach { detail ->
                    Text(
                        text = "• $detail",
                        style = FlacifyTheme.typography.caption,
                        color = FlacifyTheme.colors.secondaryText,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                if (skippedDetails.size > 5) {
                    Text(
                        text = "+ ${skippedDetails.size - 5} more files",
                        style = FlacifyTheme.typography.caption,
                        color = FlacifyTheme.colors.tertiaryText,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Standard Loading State (State 1):
 * Smooth animated spinner with contextual progress description.
 */
@Composable
fun FlacifyStateLoader(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(FlacifyTheme.spacing.xl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm)
        ) {
            CircularProgressIndicator(
                color = FlacifyTheme.colors.accent,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = text,
                style = FlacifyTheme.typography.bodyMedium,
                color = FlacifyTheme.colors.secondaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}
