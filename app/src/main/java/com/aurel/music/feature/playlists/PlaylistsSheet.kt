package com.aurel.music.feature.playlists

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import com.aurel.music.core.data.repository.PlaybackRepository
import com.aurel.music.core.data.repository.PlaylistRepository
import com.aurel.music.core.designsystem.icon.AurelIcons
import com.aurel.music.core.designsystem.theme.AurelTheme
import com.aurel.music.core.model.Playlist
import com.aurel.music.core.ui.component.SongRow
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AurelTheme.colors.surfaceElevated,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = AurelIcons.Queue,
                        contentDescription = null,
                        tint = AurelTheme.colors.accent,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Playlists",
                            style = AurelTheme.typography.sectionTitle,
                            color = AurelTheme.colors.primaryText
                        )
                        Text(
                            text = "${playlists.size} playlists",
                            style = AurelTheme.typography.secondaryContent,
                            color = AurelTheme.colors.secondaryText
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
                            imageVector = AurelIcons.Add,
                            contentDescription = "New Playlist",
                            tint = AurelTheme.colors.accent
                        )
                    }
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

            if (playlists.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(AurelTheme.shapes.small)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedPlaylist = playlist
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(AurelTheme.shapes.small)
                                    .background(AurelTheme.colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = AurelIcons.Queue,
                                    contentDescription = null,
                                    tint = AurelTheme.colors.accent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlist.name,
                                    style = AurelTheme.typography.primaryContentBold,
                                    color = AurelTheme.colors.primaryText
                                )
                                Text(
                                    text = "Custom collection",
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
                            imageVector = AurelIcons.Queue,
                            contentDescription = null,
                            tint = AurelTheme.colors.tertiaryText,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Create a playlist to organize your music.",
                            style = AurelTheme.typography.primaryContent,
                            color = AurelTheme.colors.secondaryText
                        )
                        Button(
                            onClick = {
                                newPlaylistName = ""
                                showCreateDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.accent),
                            shape = AurelTheme.shapes.small
                        ) {
                            Text(text = "Create Playlist", color = AurelTheme.colors.primaryText)
                        }
                    }
                }
            }
        }
    }

    // Create Playlist Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(text = "New Playlist", color = AurelTheme.colors.primaryText) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Playlist Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                playlistRepository.createPlaylist(newPlaylistName)
                            }
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text(text = "Create", color = AurelTheme.colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(text = "Cancel", color = AurelTheme.colors.primaryText)
                }
            },
            containerColor = AurelTheme.colors.surfaceElevated
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
            containerColor = AurelTheme.colors.surfaceElevated
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
                            style = AurelTheme.typography.sectionTitle,
                            color = AurelTheme.colors.primaryText
                        )
                        Text(
                            text = "${playlistSongs.size} tracks",
                            style = AurelTheme.typography.secondaryContent,
                            color = AurelTheme.colors.secondaryText
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
                            imageVector = AurelIcons.Delete,
                            contentDescription = "Delete Playlist",
                            tint = AurelTheme.colors.error
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
                            colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.accent),
                            shape = AurelTheme.shapes.small
                        ) {
                            Icon(imageVector = AurelIcons.Play, contentDescription = null, tint = AurelTheme.colors.primaryText)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Play All", color = AurelTheme.colors.primaryText)
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val shuffled = playlistSongs.shuffled()
                                playbackRepository.playSong(shuffled.first(), shuffled)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.surfaceVariant),
                            shape = AurelTheme.shapes.small
                        ) {
                            Icon(imageVector = AurelIcons.Shuffle, contentDescription = null, tint = AurelTheme.colors.primaryText)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Shuffle", color = AurelTheme.colors.primaryText)
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
                            style = AurelTheme.typography.secondaryContent,
                            color = AurelTheme.colors.secondaryText
                        )
                    }
                }
            }
        }
    }
}

