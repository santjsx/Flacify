package com.flacify.music.feature.more

import com.flacify.music.BuildConfig
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.flacify.music.core.ui.component.rememberFloatingDockBottomClearance
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
import com.flacify.music.core.data.repository.ThemeMode
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.designsystem.theme.bounceClick
import com.flacify.music.core.ui.component.FlacifyGlassDialog
import com.flacify.music.core.ui.component.SmoothHandoffLoader

@Composable
fun MoreScreen(
    viewModel: MoreViewModel,
    onRescanClick: () -> Unit,
    onEqualizerClick: () -> Unit = {},
    onSleepTimerClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onPlaylistsClick: () -> Unit = {},
    hasActiveTrack: Boolean = false,
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
    val bottomClearance = rememberFloatingDockBottomClearance(hasActiveTrack = hasActiveTrack)

    var showThemeDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDevCreditsDialog by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FlacifyTheme.colors.background)
    ) {
        // Fixed Top Header (Standardized across Home, Library, Search, More)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = FlacifyTheme.spacing.lg, top = FlacifyTheme.spacing.md, end = FlacifyTheme.spacing.lg, bottom = FlacifyTheme.spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Settings",
                    style = FlacifyTheme.typography.appScreenTitle,
                    color = FlacifyTheme.colors.primaryText
                )
                Text(
                    text = "Settings & preferences",
                    style = FlacifyTheme.typography.albumMetadata,
                    color = FlacifyTheme.colors.secondaryText
                )
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onRescanClick()
                },
                modifier = Modifier
                    .size(FlacifyTheme.dimensions.minTouchTarget)
                    .bounceClick()
            ) {
                Icon(
                    imageVector = FlacifyIcons.Refresh,
                    contentDescription = "Scan library",
                    tint = FlacifyTheme.colors.primaryText,
                    modifier = Modifier.size(FlacifyTheme.dimensions.iconMedium)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = FlacifyTheme.spacing.lg),
            contentPadding = PaddingValues(bottom = bottomClearance)
        ) {

        // Section 1: Collections
        item {
            PreferenceGroup(title = "COLLECTIONS") {
                PreferenceRow(
                    icon = FlacifyIcons.HeartFilled,
                    title = "Favorites",
                    subtitle = "Your liked songs",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFavoritesClick()
                    }
                )
                PreferenceRow(
                    icon = FlacifyIcons.Queue,
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
            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))
            PreferenceGroup(title = "AUDIO & PLAYBACK") {
                PreferenceRow(
                    icon = FlacifyIcons.Equalizer,
                    title = "Equalizer",
                    subtitle = "Presets and hardware audio bands",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onEqualizerClick()
                    }
                )
                PreferenceRow(
                    icon = FlacifyIcons.SleepTimer,
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
            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))
            PreferenceGroup(title = "APPEARANCE") {
                PreferenceRow(
                    icon = FlacifyIcons.Palette,
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
            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))
            PreferenceGroup(title = "LIBRARY & STORAGE") {
                PreferenceRow(
                    icon = FlacifyIcons.Refresh,
                    title = "Rescan Media Library",
                    subtitle = "Detect newly added or modified tracks",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRescanClick()
                    }
                )
                PreferenceRow(
                    icon = FlacifyIcons.Storage,
                    title = "Library Storage Info",
                    subtitle = "$songCount songs · $albumCount albums · $artistCount artists",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showStorageDialog = true
                    }
                )
                PreferenceRow(
                    icon = FlacifyIcons.Clean,
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
            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))
            PreferenceGroup(title = "UPDATES & ABOUT") {
                PreferenceRow(
                    icon = FlacifyIcons.Update,
                    title = "Check for Updates",
                    subtitle = "GitHub Releases distribution",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.checkForUpdates()
                    }
                )
                PreferenceRow(
                    icon = FlacifyIcons.Info,
                    title = "About FLACify",
                    subtitle = "Version ${BuildConfig.VERSION_NAME} · Offline-first player",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showAboutDialog = true
                    }
                )
                PreferenceRow(
                    icon = FlacifyIcons.Shield,
                    title = "Privacy Policy",
                    subtitle = "100% offline-first · Zero telemetry",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showPrivacyDialog = true
                    }
                )
                PreferenceRow(
                    icon = FlacifyIcons.Developer,
                    title = "Developer Credits",
                    subtitle = "Crafted by Santhosh Reddy",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showDevCreditsDialog = true
                    }
                )
            }
        }
    }
    }

    // Theme Selection Dialog (PRD Section 12)
    if (showThemeDialog) {
        FlacifyGlassDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(text = "Appearance", style = FlacifyTheme.typography.heading2, color = FlacifyTheme.colors.primaryText) },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(FlacifyTheme.shapes.small)
                                .bounceClick {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = FlacifyTheme.colors.accent)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = when (mode) {
                                    ThemeMode.SYSTEM -> "System Default"
                                    ThemeMode.DARK -> "Dark (Charcoal)"
                                    ThemeMode.LIGHT -> "Light (Off-white)"
                                },
                                color = FlacifyTheme.colors.primaryText,
                                style = FlacifyTheme.typography.primaryContent
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(FlacifyTheme.shapes.medium)
                        .background(FlacifyTheme.colors.accent)
                        .bounceClick { showThemeDialog = false }
                        .padding(horizontal = FlacifyTheme.spacing.md, vertical = 10.dp)
                ) {
                    Text("Close", color = androidx.compose.ui.graphics.Color.White, style = FlacifyTheme.typography.primaryButton)
                }
            }
        )
    }

    // Storage Info Dialog (PRD Section 12)
    if (showStorageDialog) {
        FlacifyGlassDialog(
            onDismissRequest = { showStorageDialog = false },
            title = { Text(text = "Library Information", style = FlacifyTheme.typography.heading2, color = FlacifyTheme.colors.primaryText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Indexed Songs: $songCount", color = FlacifyTheme.colors.secondaryText)
                    Text(text = "Unique Albums: $albumCount", color = FlacifyTheme.colors.secondaryText)
                    Text(text = "Unique Artists: $artistCount", color = FlacifyTheme.colors.secondaryText)
                    Text(
                        text = "Minimum track duration: $minDuration seconds (filters out ringtones and short clips)",
                        color = FlacifyTheme.colors.secondaryText,
                        style = FlacifyTheme.typography.secondaryContent
                    )
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(FlacifyTheme.shapes.medium)
                        .background(FlacifyTheme.colors.accent)
                        .bounceClick { showStorageDialog = false }
                        .padding(horizontal = FlacifyTheme.spacing.md, vertical = 10.dp)
                ) {
                    Text("OK", color = androidx.compose.ui.graphics.Color.White, style = FlacifyTheme.typography.primaryButton)
                }
            }
        )
    }

    // About FLACify Dialog
    if (showAboutDialog) {
        FlacifyGlassDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(text = "FLACify Music Player", style = FlacifyTheme.typography.heading2, color = FlacifyTheme.colors.primaryText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})", color = FlacifyTheme.colors.primaryText, style = FlacifyTheme.typography.primaryContentBold)
                    Text(text = "A minimalist, offline-first Android audio player engineered for clarity, fluidity, and audiophile-grade fidelity.", color = FlacifyTheme.colors.secondaryText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Crafted by Santhosh Reddy — Lead Architect & Developer.", color = FlacifyTheme.colors.accent, style = FlacifyTheme.typography.primaryContentBold)
                    Text(text = "Engineered for 50,000+ local tracks with Room Paging 3 and foreground Media3 background audio service.", color = FlacifyTheme.colors.tertiaryText, style = FlacifyTheme.typography.metadata)
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(FlacifyTheme.shapes.medium)
                        .background(FlacifyTheme.colors.accent)
                        .bounceClick { showAboutDialog = false }
                        .padding(horizontal = FlacifyTheme.spacing.md, vertical = 10.dp)
                ) {
                    Text("Close", color = androidx.compose.ui.graphics.Color.White, style = FlacifyTheme.typography.primaryButton)
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        FlacifyGlassDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(text = "Privacy Policy", style = FlacifyTheme.typography.heading2, color = FlacifyTheme.colors.primaryText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "100% Offline-First Architecture",
                        color = FlacifyTheme.colors.primaryText,
                        style = FlacifyTheme.typography.primaryContentBold
                    )
                    Text(
                        text = "FLACify operates strictly locally on your device. We do not operate remote databases, servers, or cloud endpoints.",
                        color = FlacifyTheme.colors.secondaryText,
                        style = FlacifyTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Zero Telemetry & Zero Analytics",
                        color = FlacifyTheme.colors.primaryText,
                        style = FlacifyTheme.typography.primaryContentBold
                    )
                    Text(
                        text = "Your music library, playlists, listening frequency, favorites, and settings remain 100% private and never leave your phone.",
                        color = FlacifyTheme.colors.secondaryText,
                        style = FlacifyTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Device Storage Permissions",
                        color = FlacifyTheme.colors.primaryText,
                        style = FlacifyTheme.typography.primaryContentBold
                    )
                    Text(
                        text = "Read storage permissions are exclusively used to discover local audio files, read embedded metadata, and render album art.",
                        color = FlacifyTheme.colors.secondaryText,
                        style = FlacifyTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(FlacifyTheme.shapes.medium)
                        .background(FlacifyTheme.colors.accent)
                        .bounceClick { showPrivacyDialog = false }
                        .padding(horizontal = FlacifyTheme.spacing.md, vertical = 10.dp)
                ) {
                    Text("Understood", color = androidx.compose.ui.graphics.Color.White, style = FlacifyTheme.typography.primaryButton)
                }
            }
        )
    }

    // Developer Credits Dialog
    if (showDevCreditsDialog) {
        FlacifyGlassDialog(
            onDismissRequest = { showDevCreditsDialog = false },
            title = { Text(text = "Developer Credits", style = FlacifyTheme.typography.heading2, color = FlacifyTheme.colors.primaryText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Santhosh Reddy",
                        color = FlacifyTheme.colors.accent,
                        style = FlacifyTheme.typography.heading2
                    )
                    Text(
                        text = "Lead Architect & Developer",
                        color = FlacifyTheme.colors.primaryText,
                        style = FlacifyTheme.typography.primaryContentBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dedicated to the belief that personal music software should be lightning-fast, private, visually tactile, and uncompromising in acoustic precision.",
                        color = FlacifyTheme.colors.secondaryText,
                        style = FlacifyTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(FlacifyTheme.shapes.medium)
                        .background(FlacifyTheme.colors.accent)
                        .bounceClick { showDevCreditsDialog = false }
                        .padding(horizontal = FlacifyTheme.spacing.md, vertical = 10.dp)
                ) {
                    Text("Done", color = androidx.compose.ui.graphics.Color.White, style = FlacifyTheme.typography.primaryButton)
                }
            }
        )
    }

    // GitHub Updates Dialog (PRD Section 13)
    when (val state = updateState) {
        is UpdateUiState.Checking -> {
            FlacifyGlassDialog(
                onDismissRequest = {},
                title = { Text(text = "Checking for Updates...", style = FlacifyTheme.typography.heading2, color = FlacifyTheme.colors.primaryText) },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmoothHandoffLoader(size = 32.dp, strokeWidth = 3.dp)
                    }
                },
                confirmButton = {}
            )
        }

        is UpdateUiState.Available -> {
            FlacifyGlassDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                title = { Text(text = "A new version is available", style = FlacifyTheme.typography.heading2, color = FlacifyTheme.colors.primaryText) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Version ${state.version}", style = FlacifyTheme.typography.primaryContentBold, color = FlacifyTheme.colors.accent)
                        val cleanedNotes = remember(state.releaseNotes) {
                            formatReleaseNotes(state.releaseNotes)
                        }
                        if (cleanedNotes.isNotBlank()) {
                            Text(
                                text = cleanedNotes,
                                color = FlacifyTheme.colors.secondaryText,
                                style = FlacifyTheme.typography.bodyMedium
                            )
                        }
                        val sizeMb = "%.1f".format(state.sizeBytes / (1024.0 * 1024.0))
                        Text(text = "Download size: $sizeMb MB", style = FlacifyTheme.typography.metadata, color = FlacifyTheme.colors.tertiaryText)
                        if (!state.releasePageUrl.isNullOrBlank()) {
                            Text(
                                text = "View on GitHub",
                                style = FlacifyTheme.typography.metadata,
                                color = FlacifyTheme.colors.accent,
                                modifier = Modifier
                                    .clip(FlacifyTheme.shapes.small)
                                    .clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.releasePageUrl)).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Unable to open browser", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            )
                        }
                    }
                },
                confirmButton = {
                    Box(
                        modifier = Modifier
                            .clip(FlacifyTheme.shapes.medium)
                            .background(FlacifyTheme.colors.accent)
                            .bounceClick {
                                viewModel.dismissUpdateDialog()
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.downloadUrl)).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Opening download...", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to open download URL", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = FlacifyTheme.spacing.md, vertical = 10.dp)
                    ) {
                        Text(text = "Update Now", color = androidx.compose.ui.graphics.Color.White, style = FlacifyTheme.typography.primaryButton)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.dismissUpdateDialog() },
                        modifier = Modifier.bounceClick()
                    ) {
                        Text(text = "Later", color = FlacifyTheme.colors.primaryText)
                    }
                }
            )
        }

        is UpdateUiState.NoApkFound -> {
            FlacifyGlassDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                title = { Text(text = "New Version Available", style = FlacifyTheme.typography.heading2, color = FlacifyTheme.colors.primaryText) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Version ${state.version} is published, but direct APK download is not yet attached.", color = FlacifyTheme.colors.secondaryText)
                        if (state.releaseNotes.isNotBlank()) {
                            Text(text = state.releaseNotes, color = FlacifyTheme.colors.tertiaryText)
                        }
                    }
                },
                confirmButton = {
                    Box(
                        modifier = Modifier
                            .clip(FlacifyTheme.shapes.medium)
                            .background(FlacifyTheme.colors.accent)
                            .bounceClick {
                                viewModel.dismissUpdateDialog()
                                if (!state.releasePageUrl.isNullOrBlank()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.releasePageUrl)).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Unable to open browser", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .padding(horizontal = FlacifyTheme.spacing.md, vertical = 10.dp)
                    ) {
                        Text(text = if (!state.releasePageUrl.isNullOrBlank()) "View Release" else "OK", color = androidx.compose.ui.graphics.Color.White, style = FlacifyTheme.typography.primaryButton)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.dismissUpdateDialog() },
                        modifier = Modifier.bounceClick()
                    ) {
                        Text(text = "Dismiss", color = FlacifyTheme.colors.primaryText)
                    }
                }
            )
        }

        is UpdateUiState.UpToDate -> {
            FlacifyGlassDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                title = { Text(text = "FLACify is Up to Date", style = FlacifyTheme.typography.heading2, color = FlacifyTheme.colors.primaryText) },
                text = { Text(text = "You are on the latest version (${state.version}).", color = FlacifyTheme.colors.secondaryText) },
                confirmButton = {
                    Box(
                        modifier = Modifier
                            .clip(FlacifyTheme.shapes.medium)
                            .background(FlacifyTheme.colors.accent)
                            .bounceClick { viewModel.dismissUpdateDialog() }
                            .padding(horizontal = FlacifyTheme.spacing.md, vertical = 10.dp)
                    ) {
                        Text(text = "OK", color = androidx.compose.ui.graphics.Color.White, style = FlacifyTheme.typography.primaryButton)
                    }
                }
            )
        }

        is UpdateUiState.Error -> {
            FlacifyGlassDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                title = { Text(text = "Update Check Failed", style = FlacifyTheme.typography.heading2, color = FlacifyTheme.colors.error) },
                text = { Text(text = state.message, color = FlacifyTheme.colors.secondaryText) },
                confirmButton = {
                    Box(
                        modifier = Modifier
                            .clip(FlacifyTheme.shapes.medium)
                            .background(FlacifyTheme.colors.accent)
                            .bounceClick { viewModel.dismissUpdateDialog() }
                            .padding(horizontal = FlacifyTheme.spacing.md, vertical = 10.dp)
                    ) {
                        Text(text = "OK", color = androidx.compose.ui.graphics.Color.White, style = FlacifyTheme.typography.primaryButton)
                    }
                }
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
            style = FlacifyTheme.typography.albumMetadata,
            color = FlacifyTheme.colors.secondaryText,
            modifier = Modifier.padding(start = FlacifyTheme.spacing.sm, bottom = FlacifyTheme.spacing.xs)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FlacifyTheme.shapes.large)
                .background(FlacifyTheme.colors.surface)
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
            .padding(horizontal = FlacifyTheme.spacing.md, vertical = FlacifyTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FlacifyTheme.colors.accent,
            modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
        )
        Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = FlacifyTheme.typography.primaryButton,
                color = FlacifyTheme.colors.primaryText
            )
            Text(
                text = subtitle,
                style = FlacifyTheme.typography.settingsDescription,
                color = FlacifyTheme.colors.secondaryText
            )
        }
        Icon(
            imageVector = FlacifyIcons.ChevronRight,
            contentDescription = null,
            tint = FlacifyTheme.colors.tertiaryText,
            modifier = Modifier.size(FlacifyTheme.dimensions.iconMedium)
        )
    }
}


private fun formatReleaseNotes(raw: String): String {
    return raw.lineSequence()
        .map { it.trim() }
        .filter { line ->
            line.isNotBlank() &&
            !line.startsWith("## ") &&
            !line.startsWith("### ") &&
            !line.startsWith("---") &&
            !line.contains("What's Changed", ignoreCase = true)
        }
        .map { line ->
            line.replace(Regex("""\*\*(.*?)\*\*"""), "$1")
                .replace("`", "")
                .replace(Regex("""^[-*]\s*"""), "• ")
        }
        .joinToString("\n")
}
