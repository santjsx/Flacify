package com.aurel.music.feature.favorites

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurel.music.core.data.repository.PlaybackRepository
import com.aurel.music.core.data.repository.SongRepository
import com.aurel.music.core.designsystem.theme.AurelTheme
import com.aurel.music.core.ui.component.SongRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesSheet(
    songRepository: SongRepository,
    playbackRepository: PlaybackRepository,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val favorites by songRepository.getFavoriteSongs().collectAsStateWithLifecycle(initialValue = emptyList())
    val currentSong by playbackRepository.currentSong.collectAsStateWithLifecycle()
    val isPlaying by playbackRepository.isPlaying.collectAsStateWithLifecycle()
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
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = AurelTheme.colors.accent,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Favorites",
                            style = AurelTheme.typography.sectionTitle,
                            color = AurelTheme.colors.primaryText
                        )
                        Text(
                            text = "${favorites.size} tracks",
                            style = AurelTheme.typography.secondaryContent,
                            color = AurelTheme.colors.secondaryText
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AurelTheme.colors.secondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (favorites.isNotEmpty()) {
                // Play All & Shuffle Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playbackRepository.playSong(favorites.first(), favorites)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.accent),
                        shape = AurelTheme.shapes.small
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = AurelTheme.colors.primaryText)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Play All", color = AurelTheme.colors.primaryText)
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val shuffled = favorites.shuffled()
                            playbackRepository.playSong(shuffled.first(), shuffled)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.surfaceVariant),
                        shape = AurelTheme.shapes.small
                    ) {
                        Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, tint = AurelTheme.colors.primaryText)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Shuffle", color = AurelTheme.colors.primaryText)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(favorites, key = { it.id }) { song ->
                        SongRow(
                            song = song,
                            isPlaying = currentSong?.id == song.id && isPlaying,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                playbackRepository.playSong(song, favorites)
                            },
                            onMoreClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    songRepository.setFavorite(song.id, false)
                                }
                            }
                        )
                    }
                }
            } else {
                // PRD Section 18.2 Defined Empty State
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
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = AurelTheme.colors.tertiaryText,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Your favorite tracks will appear here.",
                            style = AurelTheme.typography.primaryContent,
                            color = AurelTheme.colors.secondaryText
                        )
                    }
                }
            }
        }
    }
}

