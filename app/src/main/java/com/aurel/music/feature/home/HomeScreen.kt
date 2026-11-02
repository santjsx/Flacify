package com.aurel.music.feature.home

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.aurel.music.core.data.scanner.ScanStatus
import com.aurel.music.core.designsystem.theme.AurelTheme
import com.aurel.music.core.model.Song
import com.aurel.music.core.ui.component.SongRow

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val songCount by viewModel.songCount.collectAsStateWithLifecycle()
    val albumCount by viewModel.albumCount.collectAsStateWithLifecycle()
    val artistCount by viewModel.artistCount.collectAsStateWithLifecycle()
    val recentlyAdded by viewModel.recentlyAdded.collectAsStateWithLifecycle()
    val mostPlayed by viewModel.mostPlayed.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentPlayingSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val scanStatus by viewModel.scanStatus.collectAsStateWithLifecycle()

    // Scan feedback toast (PRD Section 15.1)
    LaunchedEffect(scanStatus) {
        when (val status = scanStatus) {
            is ScanStatus.Completed -> {
                val msg = if (status.addedCount > 0) {
                    "Music library updated · ${status.addedCount} new tracks"
                } else {
                    "Your music library is up to date."
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AurelTheme.colors.background),
        contentPadding = PaddingValues(bottom = 120.dp) // Clearance for floating MiniPlayer & bottom bar
    ) {
        // Section 1: Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AUREL",
                        style = AurelTheme.typography.screenTitle,
                        color = AurelTheme.colors.primaryText
                    )
                    Text(
                        text = "Your Music Collection",
                        style = AurelTheme.typography.secondaryContent,
                        color = AurelTheme.colors.secondaryText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search shortcut",
                            tint = AurelTheme.colors.primaryText
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings shortcut",
                            tint = AurelTheme.colors.primaryText
                        )
                    }

                    // Scan / Refresh Button
                    if (scanStatus is ScanStatus.Scanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = AurelTheme.colors.accent,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.scanLibrary() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan library",
                                tint = AurelTheme.colors.primaryText
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Library Statistics Overview
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Songs",
                    count = songCount,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Albums",
                    count = albumCount,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Artists",
                    count = artistCount,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Empty Library State
        if (songCount == 0 && scanStatus !is ScanStatus.Scanning) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = AurelTheme.colors.tertiaryText,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Your music library is empty.",
                            style = AurelTheme.typography.sectionTitle,
                            color = AurelTheme.colors.primaryText
                        )
                        Text(
                            text = "Scan your device to index your local audio files.",
                            style = AurelTheme.typography.secondaryContent,
                            color = AurelTheme.colors.secondaryText
                        )
                        Button(
                            onClick = { viewModel.scanLibrary() },
                            colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.accent),
                            shape = AurelTheme.shapes.small
                        ) {
                            Text("Scan Music", color = AurelTheme.colors.primaryText)
                        }
                    }
                }
            }
        }

        // Section 3: Most Played
        if (mostPlayed.isNotEmpty()) {
            item {
                Text(
                    text = "Most Played",
                    style = AurelTheme.typography.sectionTitle,
                    color = AurelTheme.colors.primaryText,
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(mostPlayed, key = { it.id }) { song ->
                        MostPlayedCard(
                            song = song,
                            onClick = { viewModel.playSong(song, mostPlayed) }
                        )
                    }
                }
            }
        }

        // Section 4: Recently Added (15 tracks max)
        if (recentlyAdded.isNotEmpty()) {
            item {
                Text(
                    text = "Recently Added",
                    style = AurelTheme.typography.sectionTitle,
                    color = AurelTheme.colors.primaryText,
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp)
                )
            }

            items(recentlyAdded, key = { it.id }) { song ->
                SongRow(
                    song = song,
                    isPlaying = currentSong?.id == song.id && isPlaying,
                    onClick = { viewModel.playSong(song, recentlyAdded) },
                    onMoreClick = { /* More actions */ }
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(AurelTheme.shapes.medium)
            .background(AurelTheme.colors.surface)
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                style = AurelTheme.typography.metadata,
                color = AurelTheme.colors.secondaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedContent(
                targetState = count,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "statCounter"
            ) { value ->
                Text(
                    text = value.toString(),
                    style = AurelTheme.typography.sectionTitle,
                    color = AurelTheme.colors.primaryText
                )
            }
        }
    }
}

@Composable
private fun MostPlayedCard(
    song: Song,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clip(AurelTheme.shapes.small)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(AurelTheme.shapes.small)
                .background(AurelTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "content://media/external/audio/albumart/${song.albumId}",
                contentDescription = "${song.title} artwork",
                modifier = Modifier.size(130.dp),
                contentScale = ContentScale.Crop
            )
            if (song.albumId <= 0L) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = AurelTheme.colors.tertiaryText,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = song.title,
            style = AurelTheme.typography.primaryContentBold,
            color = AurelTheme.colors.primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artistName,
            style = AurelTheme.typography.secondaryContent,
            color = AurelTheme.colors.secondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

