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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val timerState by timerManager.timerState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val presetDurations = listOf(5, 10, 15, 30, 45, 60)

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
                        text = "Playback will pause automatically",
                        style = AurelTheme.typography.secondaryContent,
                        color = AurelTheme.colors.secondaryText,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { timerManager.extendTimer(5) },
                            modifier = Modifier.weight(1f),
                            shape = AurelTheme.shapes.small
                        ) {
                            Text("+5 min", color = AurelTheme.colors.primaryText)
                        }

                        OutlinedButton(
                            onClick = { timerManager.extendTimer(15) },
                            modifier = Modifier.weight(1f),
                            shape = AurelTheme.shapes.small
                        ) {
                            Text("+15 min", color = AurelTheme.colors.primaryText)
                        }

                        Button(
                            onClick = { timerManager.cancelTimer() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.error),
                            shape = AurelTheme.shapes.small
                        ) {
                            Text("Cancel", color = AurelTheme.colors.primaryText)
                        }
                    }
                }
                else -> {
                    // Select Duration View
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
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

