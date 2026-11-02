package com.aurel.music.feature.more

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurel.music.core.data.repository.ThemeMode
import com.aurel.music.core.designsystem.icon.AurelIcons
import com.aurel.music.core.designsystem.theme.AurelTheme

@Composable
fun MoreScreen(
    viewModel: MoreViewModel,
    onRescanClick: () -> Unit,
    onEqualizerClick: () -> Unit = {},
    onSleepTimerClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onPlaylistsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val minDuration by viewModel.minTrackDurationSec.collectAsStateWithLifecycle()
    val songCount by viewModel.songCount.collectAsStateWithLifecycle()
    val albumCount by viewModel.albumCount.collectAsStateWithLifecycle()
    val artistCount by viewModel.artistCount.collectAsStateWithLifecycle()
    val updateState by viewModel.updateUiState.collectAsStateWithLifecycle()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AurelTheme.colors.background)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "More",
                style = AurelTheme.typography.screenTitle,
                color = AurelTheme.colors.primaryText,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Section 1: Collections
        item {
            PreferenceGroup(title = "COLLECTIONS") {
                PreferenceRow(
                    icon = AurelIcons.HeartFilled,
                    title = "Favorites",
                    subtitle = "Your liked songs",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFavoritesClick()
                    }
                )
                PreferenceRow(
                    icon = AurelIcons.Queue,
                    title = "Playlists",
                    subtitle = "Custom track collections",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onPlaylistsClick()
                    }
                )
            }
        }

        // Section 2: Audio & Playback
        item {
            Spacer(modifier = Modifier.height(16.dp))
            PreferenceGroup(title = "AUDIO & PLAYBACK") {
                PreferenceRow(
                    icon = AurelIcons.Equalizer,
                    title = "Equalizer",
                    subtitle = "Presets and hardware audio bands",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onEqualizerClick()
                    }
                )
                PreferenceRow(
                    icon = AurelIcons.SleepTimer,
                    title = "Sleep Timer",
                    subtitle = "Automatically pause playback",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSleepTimerClick()
                    }
                )
            }
        }

        // Section 3: Appearance & Theme
        item {
            Spacer(modifier = Modifier.height(16.dp))
            PreferenceGroup(title = "APPEARANCE") {
                PreferenceRow(
                    icon = AurelIcons.Palette,
                    title = "Theme",
                    subtitle = when (themeMode) {
                        ThemeMode.SYSTEM -> "System Default"
                        ThemeMode.DARK -> "Dark (Charcoal)"
                        ThemeMode.LIGHT -> "Light (Warm Off-white)"
                    },
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showThemeDialog = true
                    }
                )
            }
        }

        // Section 4: Library & Storage (PRD Section 12)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            PreferenceGroup(title = "LIBRARY & STORAGE") {
                PreferenceRow(
                    icon = AurelIcons.Refresh,
                    title = "Rescan Media Library",
                    subtitle = "Detect newly added or modified tracks",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRescanClick()
                    }
                )
                PreferenceRow(
                    icon = AurelIcons.Storage,
                    title = "Library Storage Info",
                    subtitle = "$songCount songs · $albumCount albums · $artistCount artists",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showStorageDialog = true
                    }
                )
                PreferenceRow(
                    icon = AurelIcons.Clean,
                    title = "Clear Artwork Cache",
                    subtitle = "Free image cache without deleting audio files",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.clearArtworkCache(context)
                        Toast.makeText(context, "Artwork cache cleared", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Section 5: Updates & About (PRD Section 13)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            PreferenceGroup(title = "UPDATES & ABOUT") {
                PreferenceRow(
                    icon = AurelIcons.Update,
                    title = "Check for Updates",
                    subtitle = "GitHub Releases distribution",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.checkForUpdates()
                    }
                )
                PreferenceRow(
                    icon = AurelIcons.Info,
                    title = "About AUREL",
                    subtitle = "Version 1.0.0 · Offline-first player",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showAboutDialog = true
                    }
                )
            }
            Spacer(modifier = Modifier.height(120.dp))
        }
    }

    // Theme Selection Dialog (PRD Section 12)
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(text = "Appearance", color = AurelTheme.colors.primaryText) },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = AurelTheme.colors.accent)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = when (mode) {
                                    ThemeMode.SYSTEM -> "System Default"
                                    ThemeMode.DARK -> "Dark (Charcoal)"
                                    ThemeMode.LIGHT -> "Light (Off-white)"
                                },
                                color = AurelTheme.colors.primaryText,
                                style = AurelTheme.typography.primaryContent
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close", color = AurelTheme.colors.accent)
                }
            },
            containerColor = AurelTheme.colors.surfaceElevated
        )
    }

    // Storage Info Dialog (PRD Section 12)
    if (showStorageDialog) {
        AlertDialog(
            onDismissRequest = { showStorageDialog = false },
            title = { Text(text = "Library Information", color = AurelTheme.colors.primaryText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Indexed Songs: $songCount", color = AurelTheme.colors.secondaryText)
                    Text(text = "Unique Albums: $albumCount", color = AurelTheme.colors.secondaryText)
                    Text(text = "Unique Artists: $artistCount", color = AurelTheme.colors.secondaryText)
                    Text(
                        text = "Minimum track duration: $minDuration seconds (filters out ringtones and short clips)",
                        color = AurelTheme.colors.secondaryText,
                        style = AurelTheme.typography.secondaryContent
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showStorageDialog = false }) {
                    Text("OK", color = AurelTheme.colors.accent)
                }
            },
            containerColor = AurelTheme.colors.surfaceElevated
        )
    }

    // About AUREL Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(text = "AUREL Music Player", color = AurelTheme.colors.primaryText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Version 1.0.0 (Build 100)", color = AurelTheme.colors.primaryText, style = AurelTheme.typography.primaryContentBold)
                    Text(text = "A minimalist, offline-first Android audio player engineered for clarity, fluidity, and audiophile-grade fidelity.", color = AurelTheme.colors.secondaryText)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Target library: 50,000+ local tracks with Room Paging 3 and foreground Media3 background playback service.", color = AurelTheme.colors.tertiaryText, style = AurelTheme.typography.metadata)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close", color = AurelTheme.colors.accent)
                }
            },
            containerColor = AurelTheme.colors.surfaceElevated
        )
    }

    // GitHub Updates Dialog (PRD Section 13)
    when (val state = updateState) {
        is UpdateUiState.Checking -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(text = "Checking for Updates...", color = AurelTheme.colors.primaryText) },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = AurelTheme.colors.accent)
                    }
                },
                confirmButton = {},
                containerColor = AurelTheme.colors.surfaceElevated
            )
        }

        is UpdateUiState.Available -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                title = { Text(text = "A new version is available", color = AurelTheme.colors.primaryText) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Version ${state.version}", style = AurelTheme.typography.primaryContentBold, color = AurelTheme.colors.accent)
                        Text(text = state.releaseNotes, color = AurelTheme.colors.secondaryText)
                        val sizeMb = "%.1f".format(state.sizeBytes / (1024.0 * 1024.0))
                        Text(text = "Download size: $sizeMb MB", style = AurelTheme.typography.metadata, color = AurelTheme.colors.tertiaryText)
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.dismissUpdateDialog()
                            Toast.makeText(context, "Initiating update download...", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = "Update Now", color = AurelTheme.colors.accent)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                        Text(text = "Later", color = AurelTheme.colors.primaryText)
                    }
                },
                containerColor = AurelTheme.colors.surfaceElevated
            )
        }

        is UpdateUiState.UpToDate -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                title = { Text(text = "AUREL is Up to Date", color = AurelTheme.colors.primaryText) },
                text = { Text(text = "You are on the latest version (${state.version}).", color = AurelTheme.colors.secondaryText) },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                        Text(text = "OK", color = AurelTheme.colors.accent)
                    }
                },
                containerColor = AurelTheme.colors.surfaceElevated
            )
        }

        is UpdateUiState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                title = { Text(text = "Update Check Failed", color = AurelTheme.colors.error) },
                text = { Text(text = state.message, color = AurelTheme.colors.secondaryText) },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                        Text(text = "OK", color = AurelTheme.colors.primaryText)
                    }
                },
                containerColor = AurelTheme.colors.surfaceElevated
            )
        }

        UpdateUiState.Idle -> { /* Nothing */ }
    }
}

@Composable
private fun PreferenceGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = AurelTheme.typography.metadata,
            color = AurelTheme.colors.secondaryText,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AurelTheme.shapes.medium)
                .background(AurelTheme.colors.surface)
        ) {
            content()
        }
    }
}

@Composable
private fun PreferenceRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AurelTheme.colors.accent,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AurelTheme.typography.primaryContentBold,
                color = AurelTheme.colors.primaryText
            )
            Text(
                text = subtitle,
                style = AurelTheme.typography.secondaryContent,
                color = AurelTheme.colors.secondaryText
            )
        }
        Icon(
            imageVector = AurelIcons.ChevronRight,
            contentDescription = null,
            tint = AurelTheme.colors.tertiaryText,
            modifier = Modifier.size(20.dp)
        )
    }
}
