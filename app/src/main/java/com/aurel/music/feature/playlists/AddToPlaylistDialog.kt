package com.aurel.music.feature.playlists

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurel.music.core.data.repository.PlaylistRepository
import com.aurel.music.core.designsystem.theme.AurelTheme
import com.aurel.music.core.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    song: Song,
    playlistRepository: PlaylistRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val playlists by playlistRepository.getAllPlaylists().collectAsStateWithLifecycle(initialValue = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                Text(
                    text = "Add to Playlist",
                    style = AurelTheme.typography.sectionTitle,
                    color = AurelTheme.colors.primaryText
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = AurelTheme.colors.secondaryText)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Create New Playlist Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AurelTheme.shapes.small)
                    .clickable {
                        newPlaylistName = ""
                        showCreateDialog = true
                    }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = AurelTheme.colors.accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "New Playlist...",
                    style = AurelTheme.typography.primaryContentBold,
                    color = AurelTheme.colors.accent
                )
            }

            LazyColumn(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                items(playlists, key = { it.id }) { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AurelTheme.shapes.small)
                            .clickable {
                                CoroutineScope(Dispatchers.IO).launch {
                                    playlistRepository.addSongToPlaylist(playlist.id, song.id)
                                }
                                Toast.makeText(context, "Added to ${playlist.name}", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = null,
                            tint = AurelTheme.colors.secondaryText,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = playlist.name,
                            style = AurelTheme.typography.primaryContent,
                            color = AurelTheme.colors.primaryText
                        )
                    }
                }
            }
        }

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
                                    val id = playlistRepository.createPlaylist(newPlaylistName)
                                    playlistRepository.addSongToPlaylist(id, song.id)
                                }
                                Toast.makeText(context, "Created & added to $newPlaylistName", Toast.LENGTH_SHORT).show()
                                showCreateDialog = false
                                onDismiss()
                            }
                        }
                    ) {
                        Text(text = "Create & Add", color = AurelTheme.colors.accent)
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
    }
}

