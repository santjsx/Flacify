package com.aurel.music.feature.more

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.aurel.music.core.designsystem.theme.AurelTheme

@Composable
fun MoreScreen(
    onRescanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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

        // Section: Library & Collections
        item {
            PreferenceGroup(title = "COLLECTIONS") {
                PreferenceRow(
                    icon = Icons.Default.Favorite,
                    title = "Favorites",
                    subtitle = "Your liked songs",
                    onClick = {}
                )
                PreferenceRow(
                    icon = Icons.Default.QueueMusic,
                    title = "Playlists",
                    subtitle = "Custom track collections",
                    onClick = {}
                )
            }
        }

        // Section: Audio & Playback
        item {
            Spacer(modifier = Modifier.height(16.dp))
            PreferenceGroup(title = "AUDIO & PLAYBACK") {
                PreferenceRow(
                    icon = Icons.Default.Equalizer,
                    title = "Equalizer",
                    subtitle = "Presets and custom audio bands",
                    onClick = {}
                )
                PreferenceRow(
                    icon = Icons.Default.Timer,
                    title = "Sleep Timer",
                    subtitle = "Automatically pause playback",
                    onClick = {}
                )
            }
        }

        // Section: System & Library Management
        item {
            Spacer(modifier = Modifier.height(16.dp))
            PreferenceGroup(title = "SYSTEM & PREFERENCES") {
                PreferenceRow(
                    icon = Icons.Default.Refresh,
                    title = "Rescan Media Library",
                    subtitle = "Index new local tracks from MediaStore",
                    onClick = onRescanClick
                )
                PreferenceRow(
                    icon = Icons.Default.Palette,
                    title = "Appearance",
                    subtitle = "Dark, Light, System themes",
                    onClick = {}
                )
                PreferenceRow(
                    icon = Icons.Default.Info,
                    title = "About AUREL",
                    subtitle = "Version 1.0.0 · Offline-first player",
                    onClick = {}
                )
            }
            Spacer(modifier = Modifier.height(120.dp))
        }
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
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = AurelTheme.colors.tertiaryText,
            modifier = Modifier.size(20.dp)
        )
    }
}
