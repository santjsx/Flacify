package com.flacify.music.feature.equalizer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flacify.music.core.audio.equalizer.EqualizerBand
import com.flacify.music.core.audio.equalizer.EqualizerController
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.designsystem.theme.bounceClick
import com.flacify.music.core.ui.component.FlacifyGlassSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerSheet(
    equalizerController: EqualizerController,
    onDismiss: () -> Unit
) {
    val uiState by equalizerController.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    FlacifyGlassSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FlacifyTheme.spacing.md, vertical = FlacifyTheme.spacing.xs)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Enable Switch
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
                            imageVector = FlacifyIcons.Equalizer,
                            contentDescription = null,
                            tint = FlacifyTheme.colors.accent,
                            modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                        )
                    }
                    Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))
                    Text(
                        text = "Equalizer",
                        style = FlacifyTheme.typography.heading2,
                        color = FlacifyTheme.colors.primaryText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = uiState.isEnabled,
                        onCheckedChange = { equalizerController.setEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = FlacifyTheme.colors.accent,
                            uncheckedThumbColor = FlacifyTheme.colors.secondaryText,
                            uncheckedTrackColor = if (FlacifyTheme.colors.isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.12f)
                        )
                    )
                    Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xs))
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
            }

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))

            if (!uiState.isAvailable) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = FlacifyTheme.spacing.xxl),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hardware equalizer not available on this device.",
                        style = FlacifyTheme.typography.bodyMedium,
                        color = FlacifyTheme.colors.secondaryText
                    )
                }
            } else {
                // Presets Carousel
                Text(
                    text = "PRESETS",
                    style = FlacifyTheme.typography.albumMetadata,
                    color = FlacifyTheme.colors.secondaryText,
                    modifier = Modifier.padding(bottom = FlacifyTheme.spacing.xs)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.xs),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.availablePresets) { preset ->
                        val isSelected = preset == uiState.currentPreset
                        val chipBrush = if (isSelected) {
                            Brush.horizontalGradient(
                                listOf(FlacifyTheme.colors.accent, FlacifyTheme.colors.accentGradientEnd)
                            )
                        } else {
                            FlacifyTheme.glass.chipBackground
                        }
                        val chipBorder = if (isSelected) {
                            BorderStroke(1.dp, FlacifyTheme.colors.accent.copy(alpha = 0.6f))
                        } else {
                            FlacifyTheme.glass.borderSubtle
                        }

                        Box(
                            modifier = Modifier
                                .clip(FlacifyTheme.shapes.small)
                                .background(chipBrush)
                                .border(chipBorder, shape = FlacifyTheme.shapes.small)
                                .bounceClick(
                                    onClick = if (uiState.isEnabled) {
                                        { equalizerController.setPreset(preset) }
                                    } else null
                                )
                                .padding(horizontal = FlacifyTheme.spacing.md, vertical = 10.dp)
                        ) {
                            Text(
                                text = preset,
                                style = if (isSelected) FlacifyTheme.typography.primaryButton else FlacifyTheme.typography.bodyMedium,
                                color = if (isSelected) Color.White else FlacifyTheme.colors.secondaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))

                // Frequency Bands Sliders
                Text(
                    text = "FREQUENCY BANDS",
                    style = FlacifyTheme.typography.albumMetadata,
                    color = FlacifyTheme.colors.secondaryText,
                    modifier = Modifier.padding(bottom = FlacifyTheme.spacing.xs)
                )

                uiState.bands.forEach { band ->
                    BandSlider(
                        band = band,
                        isEnabled = uiState.isEnabled,
                        onLevelChange = { newLevel ->
                            equalizerController.setBandLevel(band.index, newLevel)
                        }
                    )
                    Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xs))
                }

                // Bass Boost Slider (if supported)
                if (uiState.isBassBoostSupported) {
                    Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))
                    Text(
                        text = "BASS ENHANCEMENT",
                        style = FlacifyTheme.typography.albumMetadata,
                        color = FlacifyTheme.colors.secondaryText,
                        modifier = Modifier.padding(bottom = FlacifyTheme.spacing.xs)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = uiState.bassBoostStrength.toFloat(),
                            onValueChange = { equalizerController.setBassBoost(it.toInt().toShort()) },
                            valueRange = 0f..1000f,
                            enabled = uiState.isEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = if (FlacifyTheme.colors.isDark) Color.White else FlacifyTheme.colors.accent,
                                activeTrackColor = FlacifyTheme.colors.accent,
                                inactiveTrackColor = if (FlacifyTheme.colors.isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.10f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))
                        val isDark = FlacifyTheme.colors.isDark
                        Box(
                            modifier = Modifier
                                .clip(FlacifyTheme.shapes.small)
                                .background(if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
                                .border(
                                    BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)),
                                    shape = FlacifyTheme.shapes.small
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${(uiState.bassBoostStrength / 10)}%",
                                style = FlacifyTheme.typography.caption,
                                color = FlacifyTheme.colors.primaryText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xxl))
        }
    }
}

@Composable
private fun BandSlider(
    band: EqualizerBand,
    isEnabled: Boolean,
    onLevelChange: (Short) -> Unit
) {
    val freqLabel = if (band.centerFreqHz >= 1000) {
        "${band.centerFreqHz / 1000} kHz"
    } else {
        "${band.centerFreqHz} Hz"
    }

    val dbVal = band.currentLevelMilliBel / 100
    val dbSign = if (dbVal > 0) "+$dbVal" else "$dbVal"
    val isDark = FlacifyTheme.colors.isDark

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = freqLabel,
            style = FlacifyTheme.typography.caption,
            color = FlacifyTheme.colors.primaryText,
            modifier = Modifier.width(64.dp)
        )

        Slider(
            value = band.currentLevelMilliBel.toFloat(),
            onValueChange = { onLevelChange(it.toInt().toShort()) },
            valueRange = band.minLevelMilliBel.toFloat()..band.maxLevelMilliBel.toFloat(),
            enabled = isEnabled,
            colors = SliderDefaults.colors(
                thumbColor = if (isDark) Color.White else FlacifyTheme.colors.accent,
                activeTrackColor = FlacifyTheme.colors.accent,
                inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.10f)
            ),
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xs))

        Box(
            modifier = Modifier
                .width(48.dp)
                .clip(FlacifyTheme.shapes.small)
                .background(if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
                .border(
                    BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)),
                    shape = FlacifyTheme.shapes.small
                )
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$dbSign dB",
                style = FlacifyTheme.typography.caption,
                color = if (dbVal != 0) FlacifyTheme.colors.accent else FlacifyTheme.colors.secondaryText
            )
        }
    }
}
