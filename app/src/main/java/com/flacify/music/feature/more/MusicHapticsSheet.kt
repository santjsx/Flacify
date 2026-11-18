package com.flacify.music.feature.more

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.designsystem.theme.bounceClick
import com.flacify.music.core.model.HapticMode
import com.flacify.music.core.model.HapticStrength
import com.flacify.music.core.ui.component.FlacifyGlassSheet
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicHapticsSheet(
    currentMode: HapticMode,
    currentStrength: HapticStrength = HapticStrength.BALANCED,
    currentSyncOffsetMs: Int = 0,
    currentAudioRouteName: String = "Speaker / Wired (~65ms base sync)",
    onModeSelected: (HapticMode) -> Unit,
    onStrengthSelected: (HapticStrength) -> Unit = {},
    onSyncOffsetChanged: (Int) -> Unit = {},
    onTestBeatClick: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    FlacifyGlassSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FlacifyTheme.spacing.lg)
                .padding(bottom = FlacifyTheme.spacing.xl)
                .verticalScroll(scrollState)
        ) {
            // Header with Lucide Vibration icon & Master Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(FlacifyTheme.shapes.medium)
                        .background(FlacifyTheme.colors.accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FlacifyIcons.Vibration,
                        contentDescription = null,
                        tint = FlacifyTheme.colors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Music Haptics",
                        style = FlacifyTheme.typography.heading2,
                        color = FlacifyTheme.colors.primaryText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Apple-Grade Tactile Beat Engine",
                        style = FlacifyTheme.typography.caption,
                        color = FlacifyTheme.colors.secondaryText
                    )
                }

                Switch(
                    checked = currentMode != HapticMode.OFF,
                    onCheckedChange = { isChecked ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onModeSelected(if (isChecked) HapticMode.FULL_MIX else HapticMode.OFF)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = FlacifyTheme.colors.accent,
                        uncheckedThumbColor = FlacifyTheme.colors.tertiaryText,
                        uncheckedTrackColor = FlacifyTheme.colors.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))

            Text(
                text = "Music Haptics synchronizes precise linear motor impulses with each beat of the song in real time, delivering deep kick thuds, crisp percussion, and smooth vocal phrasing with sub-millisecond phase alignment.",
                style = FlacifyTheme.typography.bodyMedium,
                color = FlacifyTheme.colors.secondaryText,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))

            // SECTION 1: Haptic Mapping Mode
            Text(
                text = "HAPTIC MAPPING MODE",
                style = FlacifyTheme.typography.caption,
                color = FlacifyTheme.colors.secondaryText,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.sm))

            // Option 1: Full Mix
            HapticModeCard(
                title = "Full Mix",
                subtitle = "Kick drum thuds, sub-bass rumble, vocal phrasing, and crisp snare/hi-hat ticks.",
                isSelected = currentMode == HapticMode.FULL_MIX,
                isEnabled = currentMode != HapticMode.OFF,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onModeSelected(HapticMode.FULL_MIX)
                }
            )

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xs))

            // Option 2: Vocals Only
            HapticModeCard(
                title = "Vocals Only",
                subtitle = "Isolates the 300 Hz – 3 kHz midrange band for melodic vocal phrasing without drum impacts.",
                isSelected = currentMode == HapticMode.VOCALS_ONLY,
                isEnabled = currentMode != HapticMode.OFF,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onModeSelected(HapticMode.VOCALS_ONLY)
                }
            )

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))

            // SECTION 2: Haptic Strength (Gentle / Balanced / Powerful)
            Text(
                text = "HAPTIC STRENGTH",
                style = FlacifyTheme.typography.caption,
                color = FlacifyTheme.colors.secondaryText,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.sm))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FlacifyTheme.shapes.medium)
                    .background(FlacifyTheme.colors.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HapticStrength.entries.forEach { strength ->
                    val isSelected = strength == currentStrength
                    val isEnabled = currentMode != HapticMode.OFF
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(FlacifyTheme.shapes.small)
                            .background(
                                when {
                                    isSelected && isEnabled -> FlacifyTheme.colors.accent
                                    isSelected -> FlacifyTheme.colors.surfaceElevated
                                    else -> Color.Transparent
                                }
                            )
                            .clickable(enabled = isEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onStrengthSelected(strength)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strength.label,
                            style = FlacifyTheme.typography.caption,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = when {
                                isSelected && isEnabled -> Color.White
                                isEnabled -> FlacifyTheme.colors.secondaryText
                                else -> FlacifyTheme.colors.tertiaryText
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))

            // SECTION 3: Beat Synchronization & Micro-Tuning
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BEAT SYNCHRONIZATION",
                    style = FlacifyTheme.typography.caption,
                    color = FlacifyTheme.colors.secondaryText,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                )

                Text(
                    text = if (currentSyncOffsetMs == 0) "0 ms (Exact)" else "${if (currentSyncOffsetMs > 0) "+" else ""}$currentSyncOffsetMs ms",
                    style = FlacifyTheme.typography.caption,
                    color = FlacifyTheme.colors.accent,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.sm))

            // Route Detection Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FlacifyTheme.shapes.small)
                    .background(FlacifyTheme.colors.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = FlacifyTheme.spacing.md, vertical = FlacifyTheme.spacing.sm),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = FlacifyIcons.AudioWaveform,
                        contentDescription = null,
                        tint = FlacifyTheme.colors.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xs))
                    Column {
                        Text(
                            text = currentAudioRouteName,
                            style = FlacifyTheme.typography.caption,
                            color = FlacifyTheme.colors.primaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Automatic hardware presentation delay compensation active (<5ms phase error)",
                            style = FlacifyTheme.typography.caption,
                            color = FlacifyTheme.colors.tertiaryText,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.sm))

            // Micro-Tuner Slider (-60ms to +120ms)
            Slider(
                value = currentSyncOffsetMs.toFloat(),
                onValueChange = { newValue ->
                    onSyncOffsetChanged(newValue.roundToInt())
                },
                valueRange = -60f..120f,
                steps = 35, // 5ms increments
                enabled = currentMode != HapticMode.OFF,
                colors = SliderDefaults.colors(
                    thumbColor = FlacifyTheme.colors.accent,
                    activeTrackColor = FlacifyTheme.colors.accent,
                    inactiveTrackColor = FlacifyTheme.colors.surfaceVariant
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("-60 ms (Earlier)", style = FlacifyTheme.typography.caption, color = FlacifyTheme.colors.tertiaryText, fontSize = 11.sp)
                Text("Default", style = FlacifyTheme.typography.caption, color = FlacifyTheme.colors.tertiaryText, fontSize = 11.sp)
                Text("+120 ms (Later)", style = FlacifyTheme.typography.caption, color = FlacifyTheme.colors.tertiaryText, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))

            // Test Beat Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FlacifyTheme.shapes.medium)
                    .background(FlacifyTheme.colors.surfaceElevated)
                    .border(BorderStroke(1.dp, FlacifyTheme.colors.surfaceVariant), FlacifyTheme.shapes.medium)
                    .bounceClick(enabled = currentMode != HapticMode.OFF) {
                        onTestBeatClick()
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = FlacifyIcons.Play,
                        contentDescription = null,
                        tint = if (currentMode != HapticMode.OFF) FlacifyTheme.colors.accent else FlacifyTheme.colors.tertiaryText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xs))
                    Text(
                        text = "Test Beat Pulse",
                        style = FlacifyTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        color = if (currentMode != HapticMode.OFF) FlacifyTheme.colors.primaryText else FlacifyTheme.colors.tertiaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun HapticModeCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected && isEnabled) {
        FlacifyTheme.colors.accent
    } else {
        FlacifyTheme.colors.surfaceVariant
    }

    val backgroundColor = if (isSelected && isEnabled) {
        FlacifyTheme.colors.accent.copy(alpha = 0.10f)
    } else {
        FlacifyTheme.colors.surfaceVariant.copy(alpha = 0.35f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FlacifyTheme.shapes.medium)
            .background(backgroundColor)
            .border(BorderStroke(1.dp, borderColor), FlacifyTheme.shapes.medium)
            .clickable(
                enabled = isEnabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = FlacifyTheme.spacing.md, vertical = FlacifyTheme.spacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = FlacifyTheme.typography.bodyMedium,
                    color = if (isEnabled) FlacifyTheme.colors.primaryText else FlacifyTheme.colors.tertiaryText,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = FlacifyTheme.typography.caption,
                    color = if (isEnabled) FlacifyTheme.colors.secondaryText else FlacifyTheme.colors.tertiaryText,
                    lineHeight = 15.sp
                )
            }

            if (isSelected && isEnabled) {
                Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(FlacifyTheme.shapes.pill)
                        .background(FlacifyTheme.colors.accent)
                )
            }
        }
    }
}
