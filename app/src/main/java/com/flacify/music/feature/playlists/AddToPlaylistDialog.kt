package com.flacify.music.feature.playlists

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flacify.music.core.data.repository.PlaylistRepository
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.designsystem.theme.bounceClick
import com.flacify.music.core.model.Song
import com.flacify.music.core.ui.component.FlacifyGlassDialog
import com.flacify.music.core.ui.component.FlacifyGlassSheet
import com.flacify.music.core.ui.component.FlacifyGlassTextField
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    FlacifyGlassSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FlacifyTheme.spacing.md, vertical = FlacifyTheme.spacing.xs)
        ) {
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
                            imageVector = FlacifyIcons.PlaylistAdd,
                            contentDescription = null,
                            tint = FlacifyTheme.colors.accent,
                            modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                        )
                    }
                    Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))
                    Text(
                        text = "Add to Playlist",
                        style = FlacifyTheme.typography.heading2,
                        color = FlacifyTheme.colors.primaryText
                    )
                }

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

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))

            val isDark = FlacifyTheme.colors.isDark
            // Create New Playlist Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FlacifyTheme.shapes.medium)
                    .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f))
                    .border(
                        BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)),
                        shape = FlacifyTheme.shapes.medium
                    )
                    .bounceClick {
                        newPlaylistName = ""
                        showCreateDialog = true
                    }
                    .padding(vertical = 12.dp, horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(FlacifyTheme.shapes.circle)
                        .background(FlacifyTheme.colors.accent.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FlacifyIcons.Add,
                        contentDescription = null,
                        tint = FlacifyTheme.colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))
                Text(
                    text = "New Playlist...",
                    style = FlacifyTheme.typography.primaryButton,
                    color = FlacifyTheme.colors.accent
                )
            }

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.sm))

            LazyColumn(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                items(playlists, key = { it.id }) { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(FlacifyTheme.shapes.medium)
                            .bounceClick {
                                CoroutineScope(Dispatchers.IO).launch {
                                    playlistRepository.addSongToPlaylist(playlist.id, song.id)
                                }
                                Toast.makeText(context, "Added to ${playlist.name}", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(FlacifyTheme.shapes.small)
                                .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FlacifyIcons.Queue,
                                contentDescription = null,
                                tint = FlacifyTheme.colors.secondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = playlist.name,
                            style = FlacifyTheme.typography.bodyLarge,
                            color = FlacifyTheme.colors.primaryText
                        )
                    }
                }
            }
        }

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
                                        val id = playlistRepository.createPlaylist(newPlaylistName)
                                        playlistRepository.addSongToPlaylist(id, song.id)
                                    }
                                    Toast.makeText(context, "Created & added to $newPlaylistName", Toast.LENGTH_SHORT).show()
                                    showCreateDialog = false
                                    onDismiss()
                                }
                            }
                            .padding(horizontal = FlacifyTheme.spacing.md, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Create & Add",
                            color = Color.White,
                            style = FlacifyTheme.typography.primaryButton
                        )
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
    }
}
