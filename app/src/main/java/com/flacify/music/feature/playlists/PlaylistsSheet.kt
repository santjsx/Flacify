package com.flacify.music.feature.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flacify.music.core.data.repository.PlaybackRepository
import com.flacify.music.core.data.repository.PlaylistRepository
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.designsystem.theme.bounceClick
import com.flacify.music.core.model.Playlist
import com.flacify.music.core.ui.component.FlacifyGlassDialog
import com.flacify.music.core.ui.component.FlacifyGlassSheet
import com.flacify.music.core.ui.component.FlacifyGlassTextField
import com.flacify.music.core.ui.component.SongRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsSheet(
    playlistRepository: PlaylistRepository,
    playbackRepository: PlaybackRepository,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val playlists by playlistRepository.getAllPlaylists().collectAsStateWithLifecycle(initialValue = emptyList())
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }

    FlacifyGlassSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FlacifyTheme.spacing.lg, vertical = FlacifyTheme.spacing.sm)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = FlacifyIcons.Queue,
                        contentDescription = null,
                        tint = FlacifyTheme.colors.accent,
                        modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                    )
                    Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xs))
                    Column {
                        Text(
                            text = "Playlists",
                            style = FlacifyTheme.typography.heading2,
                            color = FlacifyTheme.colors.primaryText
                        )
                        Text(
                            text = "${playlists.size} playlists",
                            style = FlacifyTheme.typography.albumMetadata,
                            color = FlacifyTheme.colors.secondaryText
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            newPlaylistName = ""
                            showCreateDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = FlacifyIcons.Add,
                            contentDescription = "New Playlist",
                            tint = FlacifyTheme.colors.accent,
                            modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = FlacifyIcons.Close,
                            contentDescription = "Close",
                            tint = FlacifyTheme.colors.secondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))

            if (playlists.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(FlacifyTheme.shapes.small)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedPlaylist = playlist
                                }
                                .padding(vertical = FlacifyTheme.spacing.sm, horizontal = FlacifyTheme.spacing.xxs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(FlacifyTheme.dimensions.songThumbnailSize)
                                    .clip(FlacifyTheme.shapes.medium)
                                    .background(FlacifyTheme.colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = FlacifyIcons.Queue,
                                    contentDescription = null,
                                    tint = FlacifyTheme.colors.accent,
                                    modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                                )
                            }
                            Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlist.name,
                                    style = FlacifyTheme.typography.playlistTitle,
                                    color = FlacifyTheme.colors.primaryText
                                )
                                Text(
                                    text = "Custom collection",
                                    style = FlacifyTheme.typography.albumMetadata,
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
                }
            } else {
                // Empty state per PRD Section 18.2
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = FlacifyIcons.Queue,
                            contentDescription = null,
                            tint = FlacifyTheme.colors.tertiaryText,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Create a playlist to organize your music.",
                            style = FlacifyTheme.typography.primaryContent,
                            color = FlacifyTheme.colors.secondaryText
                        )
                        Button(
                            onClick = {
                                newPlaylistName = ""
                                showCreateDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                            shape = FlacifyTheme.shapes.small
                        ) {
                            Text(text = "Create Playlist", color = FlacifyTheme.colors.primaryText)
                        }
                    }
                }
            }
        }
    }

    // Create Playlist Dialog
    if (showCreateDialog) {
        FlacifyGlassDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "New Playlist",
                    style = FlacifyTheme.typography.heading2,
                    color = FlacifyTheme.colors.primaryText
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a title for your new playlist:",
                        style = FlacifyTheme.typography.bodyMedium,
                        color = FlacifyTheme.colors.secondaryText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlacifyGlassTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        placeholderText = "Playlist Name",
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(FlacifyTheme.shapes.medium)
                        .background(FlacifyTheme.colors.accent)
                        .bounceClick {
                            if (newPlaylistName.isNotBlank()) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    playlistRepository.createPlaylist(newPlaylistName)
                                }
                                showCreateDialog = false
                            }
                        }
                        .padding(horizontal = FlacifyTheme.spacing.md, vertical = 10.dp)
                ) {
                    Text(text = "Create", color = androidx.compose.ui.graphics.Color.White, style = FlacifyTheme.typography.primaryButton)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreateDialog = false },
                    modifier = Modifier.bounceClick()
                ) {
                    Text(text = "Cancel", color = FlacifyTheme.colors.primaryText)
                }
            }
        )
    }

    // Playlist Details Sheet
    if (selectedPlaylist != null) {
        val playlist = selectedPlaylist!!
        val playlistSongs by playlistRepository.getSongsForPlaylist(playlist.id).collectAsStateWithLifecycle(initialValue = emptyList())
        val currentSong by playbackRepository.currentSong.collectAsStateWithLifecycle()
        val isPlaying by playbackRepository.isPlaying.collectAsStateWithLifecycle()

        ModalBottomSheet(
            onDismissRequest = { selectedPlaylist = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = FlacifyTheme.colors.surfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = playlist.name,
                            style = FlacifyTheme.typography.sectionTitle,
                            color = FlacifyTheme.colors.primaryText
                        )
                        Text(
                            text = "${playlistSongs.size} tracks",
                            style = FlacifyTheme.typography.secondaryContent,
                            color = FlacifyTheme.colors.secondaryText
                        )
                    }

                    IconButton(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                playlistRepository.deletePlaylist(playlist.id)
                            }
                            selectedPlaylist = null
                        }
                    ) {
                        Icon(
                            imageVector = FlacifyIcons.Delete,
                            contentDescription = "Delete Playlist",
                            tint = FlacifyTheme.colors.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (playlistSongs.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                playbackRepository.playSong(playlistSongs.first(), playlistSongs)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                            shape = FlacifyTheme.shapes.small
                        ) {
                            Icon(imageVector = FlacifyIcons.Play, contentDescription = null, tint = FlacifyTheme.colors.primaryText)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Play All", color = FlacifyTheme.colors.primaryText)
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val shuffled = playlistSongs.shuffled()
                                playbackRepository.playSong(shuffled.first(), shuffled)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.surfaceVariant),
                            shape = FlacifyTheme.shapes.small
                        ) {
                            Icon(imageVector = FlacifyIcons.Shuffle, contentDescription = null, tint = FlacifyTheme.colors.primaryText)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Shuffle", color = FlacifyTheme.colors.primaryText)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(playlistSongs, key = { it.id }) { song ->
                            SongRow(
                                song = song,
                                isPlaying = currentSong?.id == song.id && isPlaying,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    playbackRepository.playSong(song, playlistSongs)
                                },
                                onMoreClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        playlistRepository.removeSongFromPlaylist(playlist.id, song.id)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No songs added to this playlist yet.",
                            style = FlacifyTheme.typography.secondaryContent,
                            color = FlacifyTheme.colors.secondaryText
                        )
                    }
                }
            }
        }
    }
}

