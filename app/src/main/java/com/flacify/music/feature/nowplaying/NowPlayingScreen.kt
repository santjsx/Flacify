package com.flacify.music.feature.nowplaying

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.flacify.music.core.audio.equalizer.EqualizerController
import com.flacify.music.core.audio.timer.SleepTimerManager
import com.flacify.music.core.data.repository.PlaylistRepository
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.AdaptiveStatusBarsEffect
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.designsystem.theme.bounceClick
import com.flacify.music.core.designsystem.theme.rememberArtworkBackgroundBrush
import com.flacify.music.core.designsystem.theme.rememberArtworkContrastTheme
import com.flacify.music.core.designsystem.theme.rememberArtworkMood
import com.flacify.music.core.model.RepeatMode
import com.flacify.music.core.model.ShuffleMode
import com.flacify.music.core.ui.component.AudioQualityBadge
import com.flacify.music.core.ui.component.FlacifyGlassDialog
import com.flacify.music.core.ui.component.FlacifyGlassSheet
import com.flacify.music.core.ui.component.HighPrecisionScrubber
import com.flacify.music.core.ui.component.MarqueeText
import com.flacify.music.core.ui.component.SongRow
import com.flacify.music.feature.equalizer.EqualizerSheet
import com.flacify.music.feature.lyrics.LyricsScreen
import com.flacify.music.feature.playlists.AddToPlaylistSheet
import com.flacify.music.feature.timer.SleepTimerSheet

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
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val lyricsUiState by viewModel.lyricsUiState.collectAsStateWithLifecycle()

    val nextSong = remember(queue, currentSong, repeatMode) {
        val song = currentSong ?: return@remember null
        if (queue.isEmpty()) return@remember null
        if (repeatMode == RepeatMode.ONE) return@remember song
        val idx = queue.indexOfFirst { it.id == song.id }
        when {
            idx in 0 until queue.size - 1 -> queue[idx + 1]
            repeatMode == RepeatMode.ALL && queue.isNotEmpty() -> queue.first()
            else -> null
        }
    }

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

    LaunchedEffect(currentSong) {
        if (currentSong == null) {
            onDismiss()
        }
    }

    if (currentSong == null) {
        return
    }

    val song = currentSong!!
    val artworkUri = "content://media/external/audio/albumart/${song.albumId}"
    val backgroundBrush = rememberArtworkBackgroundBrush(
        artworkUri = artworkUri,
        backgroundColor = FlacifyTheme.colors.background,
        isDarkTheme = FlacifyTheme.colors.isDark
    )
    val artworkMood = rememberArtworkMood(
        artworkUri = artworkUri,
        fallbackColor = FlacifyTheme.colors.background,
        isDarkTheme = FlacifyTheme.colors.isDark
    )
    val contrastTheme = rememberArtworkContrastTheme(backgroundColor = artworkMood.topColor)

    // Dynamic System Status Bars Adaptive Tinting (Turns clock, battery, 5G dark on light artwork)
    AdaptiveStatusBarsEffect(isLightBackground = contrastTheme.isLight)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FlacifyTheme.colors.background)
            .background(backgroundBrush)
            .systemBarsPadding()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                // Intercept all empty space clicks to prevent pass-through to underlying MiniPlayer or songs
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = FlacifyTheme.spacing.xl)
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
                        .padding(top = FlacifyTheme.spacing.xs),
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
                            imageVector = FlacifyIcons.ChevronDown,
                            contentDescription = "Dismiss Now Playing",
                            tint = contrastTheme.controlTint,
                            modifier = Modifier.size(FlacifyTheme.dimensions.iconLarge)
                        )
                    }

                    Text(
                        text = "PLAYING FROM LIBRARY",
                        style = FlacifyTheme.typography.playbackTimestamp,
                        color = contrastTheme.secondaryText
                    )

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showSleepTimerSheet = true
                        }
                    ) {
                        Icon(
                            imageVector = FlacifyIcons.SleepTimer,
                            contentDescription = "Sleep timer",
                            tint = contrastTheme.controlInactiveTint,
                            modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))

                // 2. Large Album Artwork (Square, squircle corners, shadow)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(1f)
                        .shadow(elevation = FlacifyTheme.elevation.playerSurface, shape = FlacifyTheme.shapes.extraLarge)
                        .clip(FlacifyTheme.shapes.extraLarge)
                        .background(FlacifyTheme.colors.surfaceVariant),
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
                            imageVector = FlacifyIcons.MusicNote,
                            contentDescription = null,
                            tint = FlacifyTheme.colors.tertiaryText,
                            modifier = Modifier.size(FlacifyTheme.dimensions.iconHuge)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))

            // 3. Track Title & Artist & Favorite Button (Adaptive contrast based on artwork luminance)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    MarqueeText(
                        text = song.title,
                        style = FlacifyTheme.typography.playerSongTitle,
                        color = contrastTheme.primaryText
                    )
                    Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xxs))
                    Text(
                        text = "${song.artistName} — ${song.albumTitle}",
                        style = FlacifyTheme.typography.playerArtistName,
                        color = contrastTheme.secondaryText,
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
                        imageVector = if (isFavorite) FlacifyIcons.HeartFilled else FlacifyIcons.HeartOutline,
                        contentDescription = if (isFavorite) "Remove ${song.title} from favorites" else "Add ${song.title} to favorites",
                        tint = if (isFavorite) FlacifyTheme.colors.accent else contrastTheme.controlInactiveTint,
                        modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                    )
                }
            }

            // 4. Playback Scrubber & Intimately Anchored Lossless Badge (Smooth Elastic Scrubber + Lucide Waveform)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                HighPrecisionScrubber(
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    onSeek = { seekTargetMs ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.seekTo(seekTargetMs)
                    },
                    trackColor = contrastTheme.scrubberTrack,
                    progressColor = if (contrastTheme.isLight) FlacifyTheme.colors.accent else androidx.compose.ui.graphics.Color.White,
                    textColor = contrastTheme.secondaryText
                )

                // Centered Audiophile Lossless Badge with Lucide Audio Waveform moved closer to scrubber
                // Reserved fixed-height container to guarantee zero layout shifts between lossy and lossless tracks
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .offset(y = (-6).dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (song.isLossless) {
                        song.audioQuality?.let { quality ->
                            AudioQualityBadge(
                                quality = quality,
                                isDark = !contrastTheme.isLight,
                                compact = false,
                                showWaveformIcon = true,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showAudioInfoDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // 5. Primary Transport Controls (Prev, 72dp Play/Pause, Next)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = FlacifyTheme.spacing.xs),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.skipToPrevious()
                    },
                    modifier = Modifier.size(FlacifyTheme.dimensions.skipButtonSize)
                ) {
                    Icon(
                        imageVector = FlacifyIcons.SkipBack,
                        contentDescription = "Previous track",
                        tint = contrastTheme.controlTint,
                        modifier = Modifier.size(FlacifyTheme.dimensions.iconLarge)
                    )
                }

                // Prominent Play/Pause 72dp button with Accent Color (1.6x larger than skip buttons, Rule 05)
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.togglePlayPause()
                    },
                    shape = FlacifyTheme.shapes.circle,
                    color = FlacifyTheme.colors.accent,
                    modifier = Modifier.size(FlacifyTheme.dimensions.playButtonHeroSize),
                    shadowElevation = FlacifyTheme.elevation.medium
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) FlacifyIcons.Pause else FlacifyIcons.Play,
                            contentDescription = if (isPlaying) "Pause playback" else "Start playback",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(FlacifyTheme.dimensions.iconLarge)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.skipToNext()
                    },
                    modifier = Modifier.size(FlacifyTheme.dimensions.skipButtonSize)
                ) {
                    Icon(
                        imageVector = FlacifyIcons.SkipForward,
                        contentDescription = "Next track",
                        tint = contrastTheme.controlTint,
                        modifier = Modifier.size(FlacifyTheme.dimensions.iconLarge)
                    )
                }
            }

            // Up Next Preview Capsule Row (Rule 06: Show what's coming before it starts)
            // Fixed width and height with single-line truncated text to guarantee zero layout shifts
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showQueueSheet = true
                },
                shape = FlacifyTheme.shapes.pill,
                color = if (contrastTheme.isLight) {
                    androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.08f)
                } else {
                    FlacifyTheme.colors.surfaceVariant.copy(alpha = 0.85f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .padding(bottom = FlacifyTheme.spacing.xxs)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = FlacifyTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (nextSong != null) {
                        AsyncImage(
                            model = "content://media/external/audio/albumart/${nextSong.albumId}",
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(FlacifyTheme.shapes.small),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = FlacifyIcons.Queue,
                            contentDescription = null,
                            tint = contrastTheme.controlInactiveTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xs))

                    Text(
                        text = "UP NEXT",
                        style = FlacifyTheme.typography.caption,
                        color = FlacifyTheme.colors.accent,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(end = FlacifyTheme.spacing.xs)
                    )

                    Text(
                        text = if (nextSong != null) "${nextSong.title} • ${nextSong.artistName}" else "End of queue",
                        style = FlacifyTheme.typography.caption,
                        color = if (nextSong != null) contrastTheme.primaryText else contrastTheme.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xs))

                    Icon(
                        imageVector = FlacifyIcons.ChevronRight,
                        contentDescription = null,
                        tint = contrastTheme.controlInactiveTint,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // 6. Secondary Controls: Shuffle, Repeat, Queue, Sleep Timer, More (PRD Section 6.1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = FlacifyTheme.spacing.md),
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
                        imageVector = FlacifyIcons.Shuffle,
                        contentDescription = "Toggle shuffle",
                        tint = if (shuffleMode == ShuffleMode.ON) FlacifyTheme.colors.accent else contrastTheme.controlInactiveTint,
                        modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleRepeat()
                    }
                ) {
                    Icon(
                        imageVector = if (repeatMode == RepeatMode.ONE) FlacifyIcons.RepeatOne else FlacifyIcons.Repeat,
                        contentDescription = "Toggle repeat",
                        tint = if (repeatMode != RepeatMode.OFF) FlacifyTheme.colors.accent else contrastTheme.controlInactiveTint,
                        modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showQueueSheet = true
                    }
                ) {
                    Icon(
                        imageVector = FlacifyIcons.Queue,
                        contentDescription = "Open Up Next queue",
                        tint = contrastTheme.controlInactiveTint,
                        modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showLyricsScreen = true
                    }
                ) {
                    Icon(
                        imageVector = FlacifyIcons.Lyrics,
                        contentDescription = "Synchronized lyrics",
                        tint = contrastTheme.controlInactiveTint,
                        modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
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
                        imageVector = FlacifyIcons.More,
                        contentDescription = "More playback options",
                        tint = contrastTheme.controlInactiveTint,
                        modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                    )
                }
            }
        }

        // Up Next Queue Bottom Sheet (mid-screen 55% viewport height)
        if (showQueueSheet) {
            FlacifyGlassSheet(
                onDismissRequest = { showQueueSheet = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f),
                contentModifier = Modifier.fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = FlacifyTheme.spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Up Next (${queue.size} tracks)",
                        style = FlacifyTheme.typography.heading2,
                        color = FlacifyTheme.colors.primaryText
                    )

                    if (queue.isNotEmpty()) {
                        TextButton(
                            onClick = { showClearQueueDialog = true },
                            modifier = Modifier.bounceClick()
                        ) {
                            Text(
                                text = "Clear",
                                color = FlacifyTheme.colors.error,
                                style = FlacifyTheme.typography.primaryButton
                            )
                        }
                    }
                }

                if (queue.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Queue is empty",
                            style = FlacifyTheme.typography.bodyMedium,
                            color = FlacifyTheme.colors.secondaryText
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
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
                                    },
                                    modifier = Modifier.bounceClick()
                                ) {
                                    Icon(
                                        imageVector = FlacifyIcons.Close,
                                        contentDescription = "Remove from queue",
                                        tint = FlacifyTheme.colors.tertiaryText,
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
            FlacifyGlassSheet(
                onDismissRequest = { showMoreMenuSheet = false }
            ) {
                // Header with current track preview thumbnail
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = FlacifyTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(FlacifyTheme.shapes.medium)
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
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = FlacifyTheme.typography.songTitle,
                            color = FlacifyTheme.colors.primaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${song.artistName} • ${song.albumTitle}",
                            style = FlacifyTheme.typography.artistName,
                            color = FlacifyTheme.colors.secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                MoreActionRow(
                    icon = if (isFavorite) FlacifyIcons.HeartFilled else FlacifyIcons.HeartOutline,
                    title = if (isFavorite) "Favorited" else "Add to Favorites",
                    iconTint = if (isFavorite) FlacifyTheme.colors.accent else androidx.compose.ui.graphics.Color(0xFFFA2D48),
                    onClick = {
                        viewModel.toggleFavorite(song)
                    }
                )
                MoreActionRow(
                    icon = FlacifyIcons.Lyrics,
                    title = "Synchronized Lyrics",
                    iconTint = androidx.compose.ui.graphics.Color(0xFF6366F1),
                    onClick = {
                        showMoreMenuSheet = false
                        showLyricsScreen = true
                    }
                )
                MoreActionRow(
                    icon = FlacifyIcons.Equalizer,
                    title = "Audio Equalizer",
                    iconTint = androidx.compose.ui.graphics.Color(0xFFA855F7),
                    onClick = {
                        showMoreMenuSheet = false
                        showEqualizerSheet = true
                    }
                )
                MoreActionRow(
                    icon = FlacifyIcons.PlaylistAdd,
                    title = "Add to Playlist",
                    iconTint = androidx.compose.ui.graphics.Color(0xFF10B981),
                    onClick = {
                        showMoreMenuSheet = false
                        showAddToPlaylistSheet = true
                    }
                )
                MoreActionRow(
                    icon = FlacifyIcons.Album,
                    title = "Go to Album",
                    iconTint = androidx.compose.ui.graphics.Color(0xFFF59E0B),
                    onClick = {
                        showMoreMenuSheet = false
                        onGoToAlbum(song.albumId)
                    }
                )
                MoreActionRow(
                    icon = FlacifyIcons.Artist,
                    title = "Go to Artist",
                    iconTint = androidx.compose.ui.graphics.Color(0xFF0EA5E9),
                    onClick = {
                        showMoreMenuSheet = false
                        onGoToArtist(song.artistId)
                    }
                )
                MoreActionRow(
                    icon = FlacifyIcons.Info,
                    title = "Audio Information",
                    iconTint = androidx.compose.ui.graphics.Color(0xFF94A3B8),
                    onClick = {
                        showMoreMenuSheet = false
                        showAudioInfoDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
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
            FlacifyGlassDialog(
                onDismissRequest = { showClearQueueDialog = false },
                title = { Text(text = "Clear Queue?", style = FlacifyTheme.typography.heading2, color = FlacifyTheme.colors.primaryText) },
                text = { Text(text = "This will remove all upcoming tracks from your queue.", style = FlacifyTheme.typography.bodyMedium, color = FlacifyTheme.colors.secondaryText) },
                confirmButton = {
                    Box(
                        modifier = Modifier
                            .clip(FlacifyTheme.shapes.medium)
                            .background(FlacifyTheme.colors.error)
                            .bounceClick {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.clearQueue()
                                showClearQueueDialog = false
                            }
                            .padding(horizontal = FlacifyTheme.spacing.md, vertical = 10.dp)
                    ) {
                        Text(text = "Clear", color = FlacifyTheme.colors.primaryText, style = FlacifyTheme.typography.primaryButton)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showClearQueueDialog = false },
                        modifier = Modifier.bounceClick()
                    ) {
                        Text(text = "Cancel", color = FlacifyTheme.colors.primaryText)
                    }
                }
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
                lyricsUiState = lyricsUiState,
                onSeek = { seekPosMs ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.seekTo(seekPosMs)
                },
                onDismiss = { showLyricsScreen = false },
                onImportFile = { uri ->
                    viewModel.importLyricsFile(uri)
                },
                onRetry = {
                    viewModel.refreshLyrics()
                }
            )
        }
    }
}

@Composable
private fun MoreActionRow(
    icon: ImageVector,
    title: String,
    iconTint: androidx.compose.ui.graphics.Color = FlacifyTheme.colors.accent,
    onClick: () -> Unit
) {
    val isDark = FlacifyTheme.colors.isDark
    val rowBg = if (isDark) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.04f) else androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.03f)
    val rowBorder = if (isDark) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.08f) else androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.06f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FlacifyTheme.shapes.medium)
            .background(rowBg)
            .border(
                BorderStroke(1.dp, rowBorder),
                shape = FlacifyTheme.shapes.medium
            )
            .bounceClick(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(FlacifyTheme.shapes.small)
                .background(iconTint.copy(alpha = if (isDark) 0.16f else 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = FlacifyTheme.typography.bodyLarge,
            color = FlacifyTheme.colors.primaryText,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = FlacifyIcons.ChevronRight,
            contentDescription = null,
            tint = FlacifyTheme.colors.secondaryText.copy(alpha = 0.40f),
            modifier = Modifier.size(16.dp)
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
}
