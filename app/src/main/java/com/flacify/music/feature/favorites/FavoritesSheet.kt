package com.flacify.music.feature.favorites

import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.flacify.music.core.model.Song
import com.flacify.music.core.ui.component.ConfirmRemovalDialog
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flacify.music.core.data.repository.PlaybackRepository
import com.flacify.music.core.data.repository.SongRepository
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.designsystem.theme.bounceClick
import com.flacify.music.core.ui.component.FlacifyGlassSheet
import com.flacify.music.core.ui.component.SongRow
import com.flacify.music.core.ui.state.FlacifyEmptyStateLayout
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesSheet(
    songRepository: SongRepository,
    playbackRepository: PlaybackRepository,
    onBrowseSongs: () -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var songToRemove by remember { mutableStateOf<Song?>(null) }
    val haptic = LocalHapticFeedback.current
    val favorites by songRepository.getFavoriteSongs().collectAsStateWithLifecycle(initialValue = emptyList())
    val currentSong by playbackRepository.currentSong.collectAsStateWithLifecycle()
    val isPlaying by playbackRepository.isPlaying.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    FlacifyGlassSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FlacifyTheme.spacing.md, vertical = FlacifyTheme.spacing.xs)
        ) {
            // Header
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
                            imageVector = FlacifyIcons.HeartFilled,
                            contentDescription = null,
                            tint = FlacifyTheme.colors.accent,
                            modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                        )
                    }
                    Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))
                    Column {
                        Text(
                            text = "Favorites",
                            style = FlacifyTheme.typography.heading2,
                            color = FlacifyTheme.colors.primaryText
                        )
                        Text(
                            text = "${favorites.size} tracks",
                            style = FlacifyTheme.typography.albumMetadata,
                            color = FlacifyTheme.colors.secondaryText
                        )
                    }
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

            if (favorites.isNotEmpty()) {
                // Play All & Shuffle Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm)
                ) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playbackRepository.playSong(favorites.first(), favorites)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .bounceClick(),
                        colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                        shape = FlacifyTheme.shapes.medium
                    ) {
                        Icon(imageVector = FlacifyIcons.Play, contentDescription = null, tint = FlacifyTheme.colors.primaryText)
                        Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xs))
                        Text(text = "Play All", color = FlacifyTheme.colors.primaryText, style = FlacifyTheme.typography.primaryButton)
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val shuffled = favorites.shuffled()
                            playbackRepository.playSong(shuffled.first(), shuffled)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .bounceClick(),
                        colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.surfaceVariant),
                        shape = FlacifyTheme.shapes.medium
                    ) {
                        Icon(imageVector = FlacifyIcons.Shuffle, contentDescription = null, tint = FlacifyTheme.colors.primaryText)
                        Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xs))
                        Text(text = "Shuffle", color = FlacifyTheme.colors.primaryText, style = FlacifyTheme.typography.primaryButton)
                    }
                }

                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))

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
                            onMoreClick = { songToRemove = song },
                            actionIcon = FlacifyIcons.HeartFilled,
                            actionLabel = "Remove ${song.title} from favorites"
                        )
                    }
                }
            } else {
                FlacifyEmptyStateLayout(
                    title = "No Favorite Songs Yet",
                    explanation = "Tap the heart icon or choose 'Add to Favorites' from any track options to collect your favorite music.",
                    icon = FlacifyIcons.HeartOutline,
                    primaryActionLabel = "Browse Songs",
                    onPrimaryAction = onBrowseSongs,
                    modifier = Modifier.padding(vertical = FlacifyTheme.spacing.xl)
                )
            }
        }
    }
    songToRemove?.let { song ->
        ConfirmRemovalDialog(
            title = "Remove from favorites?",
            message = "Remove “${song.title}” from your favorites?",
            action = "Remove",
            onDismiss = { songToRemove = null },
            onConfirm = {
                songToRemove = null
                scope.launch {
                    songRepository.setFavorite(song.id, false)
                    playbackRepository.updateFavoriteStatus(song.id, false)
                }
            }
        )
    }
}
