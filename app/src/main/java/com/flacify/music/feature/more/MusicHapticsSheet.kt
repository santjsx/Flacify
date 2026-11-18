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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.flacify.music.core.ui.component.FlacifyGlassSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicHapticsSheet(
    currentMode: HapticMode,
    onModeSelected: (HapticMode) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    FlacifyGlassSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FlacifyTheme.spacing.lg)
                .padding(bottom = FlacifyTheme.spacing.xl)
        ) {
            // Header with Lucide Vibration icon
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
                        text = "Real-Time Acoustic Tactile Engine",
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
                text = "Music Haptics synchronizes precise linear vibrations with rhythm, basslines, and vocals in real time, turning audio into rich tactile sensations.",
                style = FlacifyTheme.typography.bodyMedium,
                color = FlacifyTheme.colors.secondaryText,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))

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
                subtitle = "Bass drops, kick drum thuds, vocal swells, and crisp snare/hi-hat ticks.",
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
                subtitle = "Isolates the 300 Hz – 3 kHz midrange band for smooth melodic vocal phrasing.",
                isSelected = currentMode == HapticMode.VOCALS_ONLY,
                isEnabled = currentMode != HapticMode.OFF,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onModeSelected(HapticMode.VOCALS_ONLY)
                }
            )

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))

            // Latency & Calibration Info Badge
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
                    Text(
                        text = "Automatic latency compensation active (<15ms phase sync)",
                        style = FlacifyTheme.typography.caption,
                        color = FlacifyTheme.colors.tertiaryText
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
