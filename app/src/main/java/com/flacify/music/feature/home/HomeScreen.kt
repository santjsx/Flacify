package com.flacify.music.feature.home

import com.flacify.music.feature.playlists.AddToPlaylistSheet
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.flacify.music.core.data.scanner.ScanStatus
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.designsystem.theme.bounceClick
import com.flacify.music.core.model.Song
import com.flacify.music.core.ui.component.FlacifyGlassSheet
import com.flacify.music.core.ui.component.SmoothHandoffLoader
import com.flacify.music.core.ui.component.SongRow
import com.flacify.music.core.ui.component.rememberFloatingDockBottomClearance
import com.flacify.music.core.ui.component.rememberSmoothLoaderVisibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSongs: () -> Unit = {},
    onNavigateToAlbums: () -> Unit = {},
    onNavigateToArtists: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val songCount by viewModel.songCount.collectAsStateWithLifecycle()
    val albumCount by viewModel.albumCount.collectAsStateWithLifecycle()
    val artistCount by viewModel.artistCount.collectAsStateWithLifecycle()
    val recentlyAdded by viewModel.recentlyAdded.collectAsStateWithLifecycle()
    val mostPlayed by viewModel.mostPlayed.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentPlayingSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val scanStatus by viewModel.scanStatus.collectAsStateWithLifecycle()
    val bottomClearance = rememberFloatingDockBottomClearance(hasActiveTrack = currentSong != null)

    var selectedSongForOptions by remember { mutableStateOf<Song?>(null) }
    var showAddToPlaylistSong by remember { mutableStateOf<Song?>(null) }

    // Intercept back button when any sheet or dialog is open
    BackHandler(enabled = selectedSongForOptions != null || showAddToPlaylistSong != null) {
        when {
            showAddToPlaylistSong != null -> showAddToPlaylistSong = null
            selectedSongForOptions != null -> selectedSongForOptions = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FlacifyTheme.colors.background)
    ) {
        // Section 1: Sticky Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = FlacifyTheme.spacing.lg, top = FlacifyTheme.spacing.md, end = FlacifyTheme.spacing.lg, bottom = FlacifyTheme.spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FLACify",
                    style = FlacifyTheme.typography.appScreenTitle,
                    color = FlacifyTheme.colors.primaryText
                )
                Text(
                    text = "Your Music Collection",
                    style = FlacifyTheme.typography.albumMetadata,
                    color = FlacifyTheme.colors.secondaryText
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateToSearch,
                    modifier = Modifier
                        .size(FlacifyTheme.dimensions.minTouchTarget)
                        .bounceClick()
                ) {
                    Icon(
                        imageVector = FlacifyIcons.Search,
                        contentDescription = "Search shortcut",
                        tint = FlacifyTheme.colors.primaryText,
                        modifier = Modifier.size(FlacifyTheme.dimensions.iconMedium)
                    )
                }
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(FlacifyTheme.dimensions.minTouchTarget)
                        .bounceClick()
                ) {
                    Icon(
                        imageVector = FlacifyIcons.Settings,
                        contentDescription = "Settings shortcut",
                        tint = FlacifyTheme.colors.primaryText,
                        modifier = Modifier.size(FlacifyTheme.dimensions.iconMedium)
                    )
                }

                // Anti-flicker Smooth Scan Loader Handoff
                val isScanningLoaderVisible by rememberSmoothLoaderVisibility(
                    isLoading = scanStatus is ScanStatus.Scanning,
                    delayMs = 300L,
                    minDurationMs = 500L
                )

                if (isScanningLoaderVisible) {
                    Box(
                        modifier = Modifier.size(FlacifyTheme.dimensions.minTouchTarget),
                        contentAlignment = Alignment.Center
                    ) {
                        SmoothHandoffLoader(
                            size = 20.dp,
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    IconButton(
                        onClick = { viewModel.scanLibrary() },
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
            }
        }

        // Section 2: Sticky Library Statistics Overview (Songs, Albums, Artists)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FlacifyTheme.spacing.lg, vertical = FlacifyTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm)
        ) {
            StatCard(
                title = "Songs",
                count = songCount,
                icon = FlacifyIcons.MusicNote,
                onClick = onNavigateToSongs,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Albums",
                count = albumCount,
                icon = FlacifyIcons.Album,
                onClick = onNavigateToAlbums,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Artists",
                count = artistCount,
                icon = FlacifyIcons.Artist,
                onClick = onNavigateToArtists,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xs))

        // Section 3: Scrollable Content List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomClearance) // Clearance for floating MiniPlayer & bottom bar
        ) {

        // Empty Library State
        if (songCount == 0 && scanStatus !is ScanStatus.Scanning) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FlacifyTheme.spacing.xl, vertical = FlacifyTheme.spacing.huge),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.md)
                    ) {
                        Icon(
                            imageVector = FlacifyIcons.MusicNote,
                            contentDescription = null,
                            tint = FlacifyTheme.colors.tertiaryText,
                            modifier = Modifier.size(FlacifyTheme.dimensions.iconHuge)
                        )
                        Text(
                            text = "Your music library is empty.",
                            style = FlacifyTheme.typography.sectionHeading,
                            color = FlacifyTheme.colors.primaryText
                        )
                        Text(
                            text = "Scan your device to index your local audio files.",
                            style = FlacifyTheme.typography.albumMetadata,
                            color = FlacifyTheme.colors.secondaryText
                        )
                        Button(
                            onClick = { viewModel.scanLibrary() },
                            colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                            shape = FlacifyTheme.shapes.small
                        ) {
                            Text("Scan Music", color = FlacifyTheme.colors.primaryText, style = FlacifyTheme.typography.primaryButton)
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
                    style = FlacifyTheme.typography.sectionHeading,
                    color = FlacifyTheme.colors.primaryText,
                    modifier = Modifier.padding(start = FlacifyTheme.spacing.lg, top = FlacifyTheme.spacing.xl, bottom = FlacifyTheme.spacing.sm)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = FlacifyTheme.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm)
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
                    style = FlacifyTheme.typography.sectionHeading,
                    color = FlacifyTheme.colors.primaryText,
                    modifier = Modifier.padding(start = FlacifyTheme.spacing.lg, top = FlacifyTheme.spacing.xl, bottom = FlacifyTheme.spacing.sm)
                )
            }

            items(recentlyAdded, key = { it.id }) { song ->
                SongRow(
                    song = song,
                    isPlaying = currentSong?.id == song.id && isPlaying,
                    onClick = { viewModel.playSong(song, recentlyAdded) },
                    onMoreClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedSongForOptions = song
                    }
                )
            }
        }
    }
}

    // Song Options Bottom Sheet
    if (selectedSongForOptions != null) {
        val song = selectedSongForOptions!!
        FlacifyGlassSheet(
            onDismissRequest = { selectedSongForOptions = null }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Header info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(FlacifyTheme.shapes.small)
                            .background(FlacifyTheme.colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "content://media/external/audio/albumart/${song.albumId}",
                            contentDescription = "${song.title} artwork",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (song.albumId <= 0L) {
                            Icon(
                                imageVector = FlacifyIcons.MusicNote,
                                contentDescription = null,
                                tint = FlacifyTheme.colors.tertiaryText,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = FlacifyTheme.typography.primaryContentBold,
                            color = FlacifyTheme.colors.primaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${song.artistName} · ${song.albumTitle}",
                            style = FlacifyTheme.typography.secondaryContent,
                            color = FlacifyTheme.colors.secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Play Next
                ActionSheetRow(
                    icon = FlacifyIcons.Play,
                    title = "Play Next",
                    onClick = {
                        viewModel.playNext(song)
                        selectedSongForOptions = null
                        Toast.makeText(context, "Playing next: ${song.title}", Toast.LENGTH_SHORT).show()
                    }
                )

                // Favorite Toggle
                ActionSheetRow(
                    icon = if (song.isFavorite) FlacifyIcons.HeartFilled else FlacifyIcons.HeartOutline,
                    title = if (song.isFavorite) "Remove from Favorites" else "Add to Favorites",
                    iconTint = if (song.isFavorite) FlacifyTheme.colors.accent else FlacifyTheme.colors.secondaryText,
                    onClick = {
                        viewModel.toggleFavorite(song)
                        selectedSongForOptions = null
                    }
                )

                // Add to Playlist
                ActionSheetRow(
                    icon = FlacifyIcons.PlaylistAdd,
                    title = "Add to Playlist...",
                    onClick = {
                        val currentSongTarget = song
                        selectedSongForOptions = null
                        showAddToPlaylistSong = currentSongTarget
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    showAddToPlaylistSong?.let { song ->
        AddToPlaylistSheet(
            song = song,
            playlists = playlists,
            onAdd = { viewModel.addSongToPlaylist(it, song.id) },
            onCreate = { viewModel.createPlaylist(it, song.id) },
            onDismiss = { showAddToPlaylistSong = null }
        )
    }

}

@Composable
private fun ActionSheetRow(
    icon: ImageVector,
    title: String,
    iconTint: Color = FlacifyTheme.colors.accent,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FlacifyTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = FlacifyTheme.spacing.sm, horizontal = FlacifyTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
        )
        Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))
        Text(
            text = title,
            style = FlacifyTheme.typography.bodyLarge,
            color = FlacifyTheme.colors.primaryText
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = FlacifyTheme.colors.isDark
    Box(
        modifier = modifier
            .clip(FlacifyTheme.shapes.large)
            .background(FlacifyTheme.glass.cardBackground)
            .border(FlacifyTheme.glass.borderSubtle, shape = FlacifyTheme.shapes.large)
            .bounceClick(onClick = onClick)
    ) {
        // Subtle top rim highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(FlacifyTheme.glass.topRimHighlight)
        )

        Column(
            modifier = Modifier.padding(horizontal = FlacifyTheme.spacing.md, vertical = FlacifyTheme.spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = FlacifyTheme.typography.caption,
                    color = FlacifyTheme.colors.secondaryText
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(FlacifyTheme.shapes.circle)
                        .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = FlacifyTheme.colors.accent,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xxs))
            AnimatedContent(
                targetState = count,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "statCounter"
            ) { value ->
                Text(
                    text = value.toString(),
                    style = FlacifyTheme.typography.heading1,
                    color = FlacifyTheme.colors.primaryText
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
            .clip(FlacifyTheme.shapes.medium)
            .bounceClick(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(FlacifyTheme.shapes.medium)
                .background(FlacifyTheme.colors.surfaceVariant),
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
                    imageVector = FlacifyIcons.MusicNote,
                    contentDescription = null,
                    tint = FlacifyTheme.colors.tertiaryText,
                    modifier = Modifier.size(FlacifyTheme.dimensions.iconLarge)
                )
            }
        }
        Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xs))
        Text(
            text = song.title,
            style = FlacifyTheme.typography.songTitle,
            color = FlacifyTheme.colors.primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artistName,
            style = FlacifyTheme.typography.artistName,
            color = FlacifyTheme.colors.secondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
