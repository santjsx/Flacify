package com.aurel.music.feature.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurel.music.core.audio.equalizer.EqualizerBand
import com.aurel.music.core.audio.equalizer.EqualizerController
import com.aurel.music.core.designsystem.icon.AurelIcons
import com.aurel.music.core.designsystem.theme.AurelTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerSheet(
    equalizerController: EqualizerController,
    onDismiss: () -> Unit
) {
    val uiState by equalizerController.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AurelTheme.colors.surfaceElevated,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Enable Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = AurelIcons.Equalizer,
                        contentDescription = null,
                        tint = AurelTheme.colors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Equalizer",
                        style = AurelTheme.typography.sectionTitle,
                        color = AurelTheme.colors.primaryText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = uiState.isEnabled,
                        onCheckedChange = { equalizerController.setEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AurelTheme.colors.primaryText,
                            checkedTrackColor = AurelTheme.colors.accent,
                            uncheckedThumbColor = AurelTheme.colors.secondaryText,
                            uncheckedTrackColor = AurelTheme.colors.surfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = AurelIcons.Close,
                            contentDescription = "Close",
                            tint = AurelTheme.colors.secondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!uiState.isAvailable) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hardware equalizer not available on this device.",
                        style = AurelTheme.typography.secondaryContent,
                        color = AurelTheme.colors.secondaryText
                    )
                }
            } else {
                // Presets Carousel
                Text(
                    text = "PRESETS",
                    style = AurelTheme.typography.metadata,
                    color = AurelTheme.colors.secondaryText,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.availablePresets) { preset ->
                        val isSelected = preset == uiState.currentPreset
                        Surface(
                            modifier = Modifier
                                .clip(AurelTheme.shapes.small)
                                .clickable(enabled = uiState.isEnabled) {
                                    equalizerController.setPreset(preset)
                                },
                            color = if (isSelected) AurelTheme.colors.accent else AurelTheme.colors.surfaceVariant,
                            shape = AurelTheme.shapes.small
                        ) {
                            Text(
                                text = preset,
                                style = AurelTheme.typography.primaryContentBold,
                                color = if (isSelected) AurelTheme.colors.primaryText else AurelTheme.colors.secondaryText,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Frequency Bands Sliders
                Text(
                    text = "FREQUENCY BANDS",
                    style = AurelTheme.typography.metadata,
                    color = AurelTheme.colors.secondaryText,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                uiState.bands.forEach { band ->
                    BandSlider(
                        band = band,
                        isEnabled = uiState.isEnabled,
                        onLevelChange = { newLevel ->
                            equalizerController.setBandLevel(band.index, newLevel)
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Bass Boost Slider (if supported)
                if (uiState.isBassBoostSupported) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "BASS ENHANCEMENT",
                        style = AurelTheme.typography.metadata,
                        color = AurelTheme.colors.secondaryText,
                        modifier = Modifier.padding(bottom = 8.dp)
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
                                thumbColor = AurelTheme.colors.accent,
                                activeTrackColor = AurelTheme.colors.accent,
                                inactiveTrackColor = AurelTheme.colors.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${(uiState.bassBoostStrength / 10)}%",
                            style = AurelTheme.typography.metadata,
                            color = AurelTheme.colors.primaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = freqLabel,
            style = AurelTheme.typography.secondaryContent,
            color = AurelTheme.colors.primaryText,
            modifier = Modifier.width(64.dp)
        )

        Slider(
            value = band.currentLevelMilliBel.toFloat(),
            onValueChange = { onLevelChange(it.toInt().toShort()) },
            valueRange = band.minLevelMilliBel.toFloat()..band.maxLevelMilliBel.toFloat(),
            enabled = isEnabled,
            colors = SliderDefaults.colors(
                thumbColor = AurelTheme.colors.primaryText,
                activeTrackColor = AurelTheme.colors.accent,
                inactiveTrackColor = AurelTheme.colors.surfaceVariant
            ),
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "$dbSign dB",
            style = AurelTheme.typography.metadata,
            color = AurelTheme.colors.secondaryText,
            modifier = Modifier.width(52.dp)
        )
    }
}

