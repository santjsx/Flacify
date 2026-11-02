package com.aurel.music.feature.nowplaying

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.aurel.music.core.audio.equalizer.EqualizerController
import com.aurel.music.core.audio.timer.SleepTimerManager
import com.aurel.music.core.data.repository.PlaylistRepository
import com.aurel.music.core.designsystem.icon.AurelIcons
import com.aurel.music.core.designsystem.theme.AurelTheme
import com.aurel.music.core.designsystem.theme.rememberArtworkBackgroundBrush
import com.aurel.music.core.model.RepeatMode
import com.aurel.music.core.model.ShuffleMode
import com.aurel.music.core.ui.component.HighPrecisionScrubber
import com.aurel.music.core.ui.component.MarqueeText
import com.aurel.music.core.ui.component.SongRow
import com.aurel.music.feature.equalizer.EqualizerSheet
import com.aurel.music.feature.lyrics.LyricsScreen
import com.aurel.music.feature.playlists.AddToPlaylistSheet
import com.aurel.music.feature.timer.SleepTimerSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    viewModel: NowPlayingViewModel,
    sleepTimerManager: SleepTimerManager,
    equalizerController: EqualizerController,
    playlistRepository: PlaylistRepository,
    onDismiss: () -> Unit,
    onGoToAlbum: (Long) -> Unit = {},
    onGoToArtist: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val shuffleMode by viewModel.shuffleMode.collectAsStateWithLifecycle()
    val queue by viewModel.queue.collectAsStateWithLifecycle()

    var showQueueSheet by remember { mutableStateOf(false) }
    var showClearQueueDialog by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showLyricsScreen by remember { mutableStateOf(false) }
    var showMoreMenuSheet by remember { mutableStateOf(false) }
    var showAudioInfoDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }

    // Intercept system Back gesture (Gesture Nav edge-swipe) and 3-Button Nav back
    BackHandler(enabled = true) {
        if (showLyricsScreen) {
            showLyricsScreen = false
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onDismiss()
        }
    }

    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val animatedDragOffsetY by animateFloatAsState(
        targetValue = dragOffsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "nowPlayingDragY"
    )

    if (currentSong == null) {
        onDismiss()
        return
    }

    val song = currentSong!!
    val backgroundBrush = rememberArtworkBackgroundBrush(
        artworkUri = "content://media/external/audio/albumart/${song.albumId}",
        backgroundColor = AurelTheme.colors.background,
        isDarkTheme = AurelTheme.colors.isDark
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AurelTheme.colors.background)
            .background(backgroundBrush)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .graphicsLayer {
                    translationY = animatedDragOffsetY
                    if (animatedDragOffsetY > 0f) {
                        alpha = (1f - (animatedDragOffsetY / 1200f)).coerceIn(0.6f, 1f)
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section (Top bar + Spacer + Artwork) with vertical drag-down-to-dismiss gesture
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, dragAmount ->
                                if (dragAmount > 0f || dragOffsetY > 0f) {
                                    change.consume()
                                    dragOffsetY = (dragOffsetY + dragAmount).coerceAtLeast(0f)
                                }
                            },
                            onDragEnd = {
                                if (dragOffsetY > 160f) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onDismiss()
                                }
                                dragOffsetY = 0f
                            },
                            onDragCancel = {
                                dragOffsetY = 0f
                            }
                        )
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top bar: Dismiss chevron & Playback context
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = AurelIcons.ChevronDown,
                            contentDescription = "Dismiss Now Playing",
                            tint = AurelTheme.colors.primaryText,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "PLAYING FROM LIBRARY",
                        style = AurelTheme.typography.metadata,
                        color = AurelTheme.colors.secondaryText
                    )

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showSleepTimerSheet = true
                        }
                    ) {
                        Icon(
                            imageVector = AurelIcons.SleepTimer,
                            contentDescription = "Sleep timer",
                            tint = AurelTheme.colors.secondaryText,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Large Album Artwork (Square, squircle corners, shadow)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(1f)
                        .shadow(elevation = 24.dp, shape = AurelTheme.shapes.extraLarge)
                        .clip(AurelTheme.shapes.extraLarge)
                        .background(AurelTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = "content://media/external/audio/albumart/${song.albumId}",
                        contentDescription = "${song.title} album artwork",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (song.albumId <= 0L) {
                        Icon(
                            imageVector = AurelIcons.MusicNote,
                            contentDescription = null,
                            tint = AurelTheme.colors.tertiaryText,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Track Title & Artist & Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    MarqueeText(
                        text = song.title,
                        style = AurelTheme.typography.sectionTitle,
                        color = AurelTheme.colors.primaryText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${song.artistName} — ${song.albumTitle}",
                        style = AurelTheme.typography.primaryContent,
                        color = AurelTheme.colors.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleFavorite(song)
                    }
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) AurelIcons.HeartFilled else AurelIcons.HeartOutline,
                        contentDescription = if (song.isFavorite) "Remove ${song.title} from favorites" else "Add ${song.title} to favorites",
                        tint = if (song.isFavorite) AurelTheme.colors.accent else AurelTheme.colors.secondaryText,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // 4. Playback Scrubber (4dp resting, 44dp touch target, -remaining time)
            HighPrecisionScrubber(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                onSeek = { seekTargetMs ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.seekTo(seekTargetMs)
                }
            )

            // 5. Primary Transport Controls (Prev, 72dp Play/Pause, Next)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.skipToPrevious()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = AurelIcons.SkipBack,
                        contentDescription = "Previous track",
                        tint = AurelTheme.colors.primaryText,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Prominent Play/Pause 72dp button
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.togglePlayPause()
                    },
                    shape = CircleShape,
                    color = AurelTheme.colors.primaryText,
                    modifier = Modifier.size(72.dp),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) AurelIcons.Pause else AurelIcons.Play,
                            contentDescription = if (isPlaying) "Pause playback" else "Start playback",
                            tint = AurelTheme.colors.background,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.skipToNext()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = AurelIcons.SkipForward,
                        contentDescription = "Next track",
                        tint = AurelTheme.colors.primaryText,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // 6. Secondary Controls: Shuffle, Repeat, Queue, Sleep Timer, More (PRD Section 6.1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleShuffle()
                    }
                ) {
                    Icon(
                        imageVector = AurelIcons.Shuffle,
                        contentDescription = "Toggle shuffle",
                        tint = if (shuffleMode == ShuffleMode.ON) AurelTheme.colors.accent else AurelTheme.colors.secondaryText
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleRepeat()
                    }
                ) {
                    Icon(
                        imageVector = if (repeatMode == RepeatMode.ONE) AurelIcons.RepeatOne else AurelIcons.Repeat,
                        contentDescription = "Toggle repeat",
                        tint = if (repeatMode != RepeatMode.OFF) AurelTheme.colors.accent else AurelTheme.colors.secondaryText
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showQueueSheet = true
                    }
                ) {
                    Icon(
                        imageVector = AurelIcons.Queue,
                        contentDescription = "Open Up Next queue",
                        tint = AurelTheme.colors.secondaryText
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showSleepTimerSheet = true
                    }
                ) {
                    Icon(
                        imageVector = AurelIcons.SleepTimer,
                        contentDescription = "Sleep timer",
                        tint = AurelTheme.colors.secondaryText
                    )
                }

                // 5th Secondary Action: More Menu (PRD Section 6.1)
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showMoreMenuSheet = true
                    }
                ) {
                    Icon(
                        imageVector = AurelIcons.More,
                        contentDescription = "More playback options",
                        tint = AurelTheme.colors.secondaryText
                    )
                }
            }
        }

        // Up Next Queue Bottom Sheet
        if (showQueueSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQueueSheet = false },
                containerColor = AurelTheme.colors.surfaceElevated,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Up Next (${queue.size} tracks)",
                            style = AurelTheme.typography.sectionTitle,
                            color = AurelTheme.colors.primaryText
                        )

                        if (queue.isNotEmpty()) {
                            TextButton(
                                onClick = { showClearQueueDialog = true }
                            ) {
                                Text(
                                    text = "Clear",
                                    color = AurelTheme.colors.error,
                                    style = AurelTheme.typography.primaryContentBold
                                )
                            }
                        }
                    }

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        itemsIndexed(queue, key = { index, item -> "${item.id}_$index" }) { index, queueSong ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    SongRow(
                                        song = queueSong,
                                        isPlaying = song.id == queueSong.id && isPlaying,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.playQueueItem(queueSong)
                                            showQueueSheet = false
                                        },
                                        onMoreClick = {}
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.removeFromQueue(index)
                                    }
                                ) {
                                    Icon(
                                        imageVector = AurelIcons.Close,
                                        contentDescription = "Remove from queue",
                                        tint = AurelTheme.colors.tertiaryText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // More Menu Bottom Sheet (PRD Section 6.1)
        if (showMoreMenuSheet) {
            ModalBottomSheet(
                onDismissRequest = { showMoreMenuSheet = false },
                containerColor = AurelTheme.colors.surfaceElevated,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Options",
                        style = AurelTheme.typography.sectionTitle,
                        color = AurelTheme.colors.primaryText,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    MoreActionRow(
                        icon = AurelIcons.Lyrics,
                        title = "Synchronized Lyrics",
                        onClick = {
                            showMoreMenuSheet = false
                            showLyricsScreen = true
                        }
                    )
                    MoreActionRow(
                        icon = AurelIcons.Equalizer,
                        title = "Audio Equalizer",
                        onClick = {
                            showMoreMenuSheet = false
                            showEqualizerSheet = true
                        }
                    )
                    MoreActionRow(
                        icon = AurelIcons.PlaylistAdd,
                        title = "Add to Playlist",
                        onClick = {
                            showMoreMenuSheet = false
                            showAddToPlaylistSheet = true
                        }
                    )
                    MoreActionRow(
                        icon = AurelIcons.Album,
                        title = "Go to Album",
                        onClick = {
                            showMoreMenuSheet = false
                            onGoToAlbum(song.albumId)
                        }
                    )
                    MoreActionRow(
                        icon = AurelIcons.Artist,
                        title = "Go to Artist",
                        onClick = {
                            showMoreMenuSheet = false
                            onGoToArtist(song.artistId)
                        }
                    )
                    MoreActionRow(
                        icon = AurelIcons.Info,
                        title = "Audio Information",
                        onClick = {
                            showMoreMenuSheet = false
                            showAudioInfoDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Add to Playlist Sheet
        if (showAddToPlaylistSheet) {
            AddToPlaylistSheet(
                song = song,
                playlistRepository = playlistRepository,
                onDismiss = { showAddToPlaylistSheet = false }
            )
        }

        // Audio Information Dialog (PRD Section 14.2)
        if (showAudioInfoDialog) {
            AudioInformationDialog(
                song = song,
                onDismiss = { showAudioInfoDialog = false }
            )
        }

        // Clear Queue Confirmation Dialog (PRD Section 6.1)
        if (showClearQueueDialog) {
            AlertDialog(
                onDismissRequest = { showClearQueueDialog = false },
                title = { Text(text = "Clear Queue?", color = AurelTheme.colors.primaryText) },
                text = { Text(text = "This will remove all upcoming tracks from your queue.", color = AurelTheme.colors.secondaryText) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.clearQueue()
                            showClearQueueDialog = false
                        }
                    ) {
                        Text(text = "Clear", color = AurelTheme.colors.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearQueueDialog = false }) {
                        Text(text = "Cancel", color = AurelTheme.colors.primaryText)
                    }
                },
                containerColor = AurelTheme.colors.surfaceElevated
            )
        }

        // Sleep Timer Sheet
        if (showSleepTimerSheet) {
            SleepTimerSheet(
                timerManager = sleepTimerManager,
                onDismiss = { showSleepTimerSheet = false }
            )
        }

        // Equalizer Sheet
        if (showEqualizerSheet) {
            EqualizerSheet(
                equalizerController = equalizerController,
                onDismiss = { showEqualizerSheet = false }
            )
        }

        // Synchronized Lyrics Full Screen Overlay
        if (showLyricsScreen) {
            LyricsScreen(
                song = song,
                currentPositionMs = currentPositionMs,
                lyrics = null,
                onSeek = { seekPosMs ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.seekTo(seekPosMs)
                },
                onDismiss = { showLyricsScreen = false }
            )
        }
    }
}

@Composable
private fun MoreActionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AurelTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AurelTheme.colors.accent,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = AurelTheme.typography.primaryContent,
            color = AurelTheme.colors.primaryText
        )
    }
}
