package com.flacify.music.core.ui.state

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme

/**
 * Confirmation Dialog for Destructive Actions (State 14):
 * Requires explicit user confirmation before permanent data deletion.
 */
@Composable
fun FlacifyDestructiveConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmLabel: String = "Delete",
    dismissLabel: String = "Cancel",
    icon: ImageVector = FlacifyIcons.Delete,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(FlacifyTheme.shapes.large)
                .background(FlacifyTheme.colors.surfaceElevated)
                .padding(FlacifyTheme.spacing.xl)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(FlacifyTheme.dimensions.iconHero)
                        .clip(FlacifyTheme.shapes.medium)
                        .background(FlacifyTheme.colors.error.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = FlacifyTheme.colors.error,
                        modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
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
                    text = message,
                    style = FlacifyTheme.typography.bodyMedium,
                    color = FlacifyTheme.colors.secondaryText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = FlacifyTheme.shapes.medium
                    ) {
                        Text(
                            text = dismissLabel,
                            color = FlacifyTheme.colors.primaryText,
                            style = FlacifyTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.error),
                        shape = FlacifyTheme.shapes.medium
                    ) {
                        Text(
                            text = confirmLabel,
                            color = FlacifyTheme.colors.primaryText,
                            style = FlacifyTheme.typography.primaryButton
                        )
                    }
                }
            }
        }
    }
}

/**
 * Unsaved Changes Confirmation Dialog (State 13):
 * Warns before user leaves an editing flow with uncommitted data.
 */
@Composable
fun FlacifyUnsavedChangesDialog(
    visible: Boolean,
    onDiscard: () -> Unit,
    onKeepEditing: () -> Unit
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onKeepEditing,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(FlacifyTheme.shapes.large)
                .background(FlacifyTheme.colors.surfaceElevated)
                .padding(FlacifyTheme.spacing.xl)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(FlacifyTheme.dimensions.iconHero)
                        .clip(FlacifyTheme.shapes.medium)
                        .background(FlacifyTheme.colors.warning.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FlacifyIcons.Warning,
                        contentDescription = null,
                        tint = FlacifyTheme.colors.warning,
                        modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                    )
                }

                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))

                Text(
                    text = "Discard Unsaved Changes?",
                    style = FlacifyTheme.typography.heading2,
                    color = FlacifyTheme.colors.primaryText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xxs))

                Text(
                    text = "You have unsaved changes that will be lost if you leave this screen.",
                    style = FlacifyTheme.typography.bodyMedium,
                    color = FlacifyTheme.colors.secondaryText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm)
                ) {
                    OutlinedButton(
                        onClick = onKeepEditing,
                        modifier = Modifier.weight(1f),
                        shape = FlacifyTheme.shapes.medium
                    ) {
                        Text(
                            text = "Keep Editing",
                            color = FlacifyTheme.colors.primaryText,
                            style = FlacifyTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        onClick = onDiscard,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.error),
                        shape = FlacifyTheme.shapes.medium
                    ) {
                        Text(
                            text = "Discard",
                            color = FlacifyTheme.colors.primaryText,
                            style = FlacifyTheme.typography.primaryButton
                        )
                    }
                }
            }
        }
    }
}
