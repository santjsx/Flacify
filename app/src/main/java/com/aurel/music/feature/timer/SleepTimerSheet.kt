package com.aurel.music.feature.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurel.music.core.audio.timer.SleepTimerManager
import com.aurel.music.core.audio.timer.SleepTimerState
import com.aurel.music.core.designsystem.theme.AurelTheme

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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AurelTheme.colors.surfaceElevated,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = AurelTheme.colors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sleep Timer",
                        style = AurelTheme.typography.sectionTitle,
                        color = AurelTheme.colors.primaryText
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AurelTheme.colors.secondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (val state = timerState) {
                is SleepTimerState.Active -> {
                    // Active Timer View
                    val minutes = state.remainingSeconds / 60
                    val seconds = state.remainingSeconds % 60
                    val formattedTime = String.format("%02d:%02d", minutes, seconds)

                    Text(
                        text = formattedTime,
                        style = AurelTheme.typography.screenTitle,
                        color = AurelTheme.colors.accent
                    )
                    Text(
                        text = if (state.stopAtEndOfTrack) "Playback will stop at end of current track" else "Playback will pause automatically",
                        style = AurelTheme.typography.secondaryContent,
                        color = AurelTheme.colors.secondaryText,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                timerManager.extendTimer(5)
                            },
                            modifier = Modifier.weight(1f),
                            shape = AurelTheme.shapes.small
                        ) {
                            Text("+5 min", color = AurelTheme.colors.primaryText)
                        }

                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                timerManager.extendTimer(15)
                            },
                            modifier = Modifier.weight(1f),
                            shape = AurelTheme.shapes.small
                        ) {
                            Text("+15 min", color = AurelTheme.colors.primaryText)
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                timerManager.cancelTimer()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.error),
                            shape = AurelTheme.shapes.small
                        ) {
                            Text("Cancel", color = AurelTheme.colors.primaryText)
                        }
                    }
                }

                is SleepTimerState.Finished -> {
                    // Subtle completion popup (PRD Section 8.3)
                    Text(
                        text = "Sleep timer finished",
                        style = AurelTheme.typography.sectionTitle,
                        color = AurelTheme.colors.primaryText
                    )
                    Text(
                        text = "Playback paused.",
                        style = AurelTheme.typography.secondaryContent,
                        color = AurelTheme.colors.secondaryText,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )
                }

                SleepTimerState.Inactive -> {
                    // Select Duration View (PRD Section 8.1)
                    Text(
                        text = "Select when to stop playing",
                        style = AurelTheme.typography.secondaryContent,
                        color = AurelTheme.colors.secondaryText,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        presetDurations.forEach { minutes ->
                            Surface(
                                modifier = Modifier
                                    .clip(AurelTheme.shapes.small)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        timerManager.startTimer(minutes)
                                    },
                                color = AurelTheme.colors.surfaceVariant,
                                shape = AurelTheme.shapes.small
                            ) {
                                Text(
                                    text = "$minutes minutes",
                                    style = AurelTheme.typography.primaryContentBold,
                                    color = AurelTheme.colors.primaryText,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }

                        // End of Current Track (PRD Section 8.1)
                        Surface(
                            modifier = Modifier
                                .clip(AurelTheme.shapes.small)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    timerManager.startTimerForEndOfTrack()
                                },
                            color = AurelTheme.colors.surfaceVariant,
                            shape = AurelTheme.shapes.small
                        ) {
                            Text(
                                text = "End of current track",
                                style = AurelTheme.typography.primaryContentBold,
                                color = AurelTheme.colors.accent,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }

                        // Custom Duration (PRD Section 8.1)
                        Surface(
                            modifier = Modifier
                                .clip(AurelTheme.shapes.small)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showCustomDurationDialog = true
                                },
                            color = AurelTheme.colors.surfaceVariant,
                            shape = AurelTheme.shapes.small
                        ) {
                            Text(
                                text = "Custom...",
                                style = AurelTheme.typography.primaryContentBold,
                                color = AurelTheme.colors.primaryText,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Custom Duration Input Dialog
        if (showCustomDurationDialog) {
            AlertDialog(
                onDismissRequest = { showCustomDurationDialog = false },
                title = { Text(text = "Custom Duration", color = AurelTheme.colors.primaryText) },
                text = {
                    Column {
                        Text(
                            text = "Enter duration in minutes:",
                            style = AurelTheme.typography.secondaryContent,
                            color = AurelTheme.colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customMinutesInput,
                            onValueChange = { customMinutesInput = it.filter { ch -> ch.isDigit() } },
                            placeholder = { Text("e.g. 25") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val mins = customMinutesInput.toIntOrNull()
                            if (mins != null && mins > 0) {
                                timerManager.startTimer(mins)
                                showCustomDurationDialog = false
                            }
                        }
                    ) {
                        Text(text = "Start", color = AurelTheme.colors.accent)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomDurationDialog = false }) {
                        Text(text = "Cancel", color = AurelTheme.colors.primaryText)
                    }
                },
                containerColor = AurelTheme.colors.surfaceElevated
            )
        }
    }
}
