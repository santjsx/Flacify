package com.flacify.music.feature.timer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flacify.music.core.audio.timer.SleepTimerManager
import com.flacify.music.core.audio.timer.SleepTimerState
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.designsystem.theme.bounceClick
import com.flacify.music.core.ui.component.FlacifyGlassDialog
import com.flacify.music.core.ui.component.FlacifyGlassSheet
import com.flacify.music.core.ui.component.FlacifyGlassTextField

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SleepTimerSheet(
    timerManager: SleepTimerManager,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val timerState by timerManager.timerState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val presetDurations = listOf(5, 10, 15, 30, 45, 60)
    var showCustomDurationDialog by remember { mutableStateOf(false) }
    var customMinutesInput by remember { mutableStateOf("") }

    FlacifyGlassSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FlacifyTheme.spacing.md, vertical = FlacifyTheme.spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(FlacifyTheme.shapes.circle)
                            .background(FlacifyTheme.colors.accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FlacifyIcons.SleepTimer,
                            contentDescription = null,
                            tint = FlacifyTheme.colors.accent,
                            modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                        )
                    }
                    Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))
                    Text(
                        text = "Sleep Timer",
                        style = FlacifyTheme.typography.heading2,
                        color = FlacifyTheme.colors.primaryText
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.bounceClick()
                ) {
                    Icon(
                        imageVector = FlacifyIcons.Close,
                        contentDescription = "Close",
                        tint = FlacifyTheme.colors.secondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))

            when (val state = timerState) {
                is SleepTimerState.Active -> {
                    // Active Timer View
                    val minutes = state.remainingSeconds / 60
                    val seconds = state.remainingSeconds % 60
                    val formattedTime = String.format("%02d:%02d", minutes, seconds)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(FlacifyTheme.shapes.large)
                            .background(FlacifyTheme.glass.cardBackground)
                            .border(FlacifyTheme.glass.borderSubtle, shape = FlacifyTheme.shapes.large)
                            .padding(vertical = FlacifyTheme.spacing.xl, horizontal = FlacifyTheme.spacing.md),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formattedTime,
                                style = FlacifyTheme.typography.display,
                                color = FlacifyTheme.colors.accent
                            )
                            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xs))
                            Text(
                                text = if (state.stopAtEndOfTrack) "Playback will stop at end of current track" else "Playback will pause automatically",
                                style = FlacifyTheme.typography.caption,
                                color = FlacifyTheme.colors.secondaryText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(FlacifyTheme.shapes.medium)
                                .background(FlacifyTheme.glass.chipBackground)
                                .border(FlacifyTheme.glass.borderSubtle, shape = FlacifyTheme.shapes.medium)
                                .bounceClick {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    timerManager.extendTimer(5)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+5 min",
                                color = FlacifyTheme.colors.primaryText,
                                style = FlacifyTheme.typography.primaryButton
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(FlacifyTheme.shapes.medium)
                                .background(FlacifyTheme.glass.chipBackground)
                                .border(FlacifyTheme.glass.borderSubtle, shape = FlacifyTheme.shapes.medium)
                                .bounceClick {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    timerManager.extendTimer(15)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+15 min",
                                color = FlacifyTheme.colors.primaryText,
                                style = FlacifyTheme.typography.primaryButton
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(FlacifyTheme.shapes.medium)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            FlacifyTheme.colors.error.copy(alpha = 0.25f),
                                            FlacifyTheme.colors.error.copy(alpha = 0.15f)
                                        )
                                    )
                                )
                                .border(
                                    BorderStroke(1.dp, FlacifyTheme.colors.error.copy(alpha = 0.50f)),
                                    shape = FlacifyTheme.shapes.medium
                                )
                                .bounceClick {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    timerManager.cancelTimer()
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
                                color = FlacifyTheme.colors.primaryText,
                                style = FlacifyTheme.typography.primaryButton
                            )
                        }
                    }
                }

                is SleepTimerState.Finished -> {
                    // Subtle completion popup
                    Text(
                        text = "Sleep timer finished",
                        style = FlacifyTheme.typography.heading2,
                        color = FlacifyTheme.colors.primaryText
                    )
                    Text(
                        text = "Playback paused.",
                        style = FlacifyTheme.typography.bodyMedium,
                        color = FlacifyTheme.colors.secondaryText,
                        modifier = Modifier.padding(top = FlacifyTheme.spacing.xxs, bottom = FlacifyTheme.spacing.lg)
                    )
                }

                SleepTimerState.Inactive -> {
                    // Select Duration View
                    Text(
                        text = "Select when to stop playing",
                        style = FlacifyTheme.typography.bodyMedium,
                        color = FlacifyTheme.colors.secondaryText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = FlacifyTheme.spacing.md)
                    )

                    // 2x3 Preset Grid with balanced 3 columns
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm)
                    ) {
                        presetDurations.chunked(3).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm)
                            ) {
                                row.forEach { minutes ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .clip(FlacifyTheme.shapes.medium)
                                            .background(FlacifyTheme.glass.chipBackground)
                                            .border(FlacifyTheme.glass.borderSubtle, shape = FlacifyTheme.shapes.medium)
                                            .bounceClick {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                timerManager.startTimer(minutes)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$minutes min",
                                            style = FlacifyTheme.typography.primaryButton,
                                            color = FlacifyTheme.colors.primaryText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(FlacifyTheme.spacing.sm))

                    // Secondary Actions Row: End of Track & Custom Duration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm)
                    ) {
                        // End of Track action
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(FlacifyTheme.shapes.medium)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            FlacifyTheme.colors.accent.copy(alpha = 0.16f),
                                            FlacifyTheme.colors.accent.copy(alpha = 0.08f)
                                        )
                                    )
                                )
                                .border(
                                    BorderStroke(1.dp, FlacifyTheme.colors.accent.copy(alpha = 0.45f)),
                                    shape = FlacifyTheme.shapes.medium
                                )
                                .bounceClick {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    timerManager.startTimerForEndOfTrack()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = FlacifyIcons.MusicNote,
                                    contentDescription = null,
                                    tint = FlacifyTheme.colors.accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "End of track",
                                    style = FlacifyTheme.typography.button,
                                    color = FlacifyTheme.colors.accent
                                )
                            }
                        }

                        // Custom Duration action
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(FlacifyTheme.shapes.medium)
                                .background(FlacifyTheme.glass.chipBackground)
                                .border(FlacifyTheme.glass.borderSubtle, shape = FlacifyTheme.shapes.medium)
                                .bounceClick {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showCustomDurationDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = FlacifyIcons.SleepTimer,
                                    contentDescription = null,
                                    tint = FlacifyTheme.colors.secondaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Custom...",
                                    style = FlacifyTheme.typography.button,
                                    color = FlacifyTheme.colors.primaryText
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xl))
        }

        // Custom Duration Input Dialog
        if (showCustomDurationDialog) {
            FlacifyGlassDialog(
                onDismissRequest = { showCustomDurationDialog = false },
                title = {
                    Text(
                        text = "Custom Duration",
                        style = FlacifyTheme.typography.heading2,
                        color = FlacifyTheme.colors.primaryText
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter duration in minutes:",
                            style = FlacifyTheme.typography.bodyMedium,
                            color = FlacifyTheme.colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FlacifyGlassTextField(
                            value = customMinutesInput,
                            onValueChange = { customMinutesInput = it.filter { ch -> ch.isDigit() } },
                            placeholderText = "e.g. 25",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Box(
                        modifier = Modifier
                            .clip(FlacifyTheme.shapes.medium)
                            .background(FlacifyTheme.colors.accent)
                            .bounceClick {
                                val mins = customMinutesInput.toIntOrNull()
                                if (mins != null && mins > 0) {
                                    timerManager.startTimer(mins)
                                    showCustomDurationDialog = false
                                }
                            }
                            .padding(horizontal = FlacifyTheme.spacing.md, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Start",
                            color = Color.White,
                            style = FlacifyTheme.typography.primaryButton
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showCustomDurationDialog = false },
                        modifier = Modifier.bounceClick()
                    ) {
                        Text(text = "Cancel", color = FlacifyTheme.colors.primaryText)
                    }
                }
            )
        }
    }
}
