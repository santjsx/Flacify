package com.flacify.music.feature.lyrics

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.model.Song
import com.flacify.music.feature.nowplaying.LyricsUiState
import kotlinx.coroutines.delay

@Composable
fun LyricsScreen(
    song: Song,
    currentPositionMs: Long,
    lyricsUiState: LyricsUiState,
    onSeek: (Long) -> Unit,
    onDismiss: () -> Unit,
    onImportFile: (Uri) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var isUserScrolling by remember { mutableStateOf(false) }

    val lrcPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportFile(uri)
        }
    }

    // Intercept system Back gesture and 3-button back to dismiss lyrics
    BackHandler(enabled = true) {
        onDismiss()
    }

    // Auto-reset user scrolling flag after 3.5 seconds of inactivity
    LaunchedEffect(isUserScrolling) {
        if (isUserScrolling) {
            delay(3500)
            isUserScrolling = false
        }
    }

    val lyrics = (lyricsUiState as? LyricsUiState.Success)?.lyrics
    val activeIndex = lyrics?.findActiveLineIndex(currentPositionMs) ?: -1

    // Auto-scroll to center active line when not manually scrolling
    LaunchedEffect(activeIndex, isUserScrolling) {
        if (!isUserScrolling && activeIndex >= 0 && lyrics != null && lyrics.lines.isNotEmpty()) {
            listState.animateScrollToItem(
                index = activeIndex,
                scrollOffset = -350 // Center offset on mobile screen
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FlacifyTheme.colors.background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Song Info, Import Button & Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FlacifyTheme.spacing.lg, vertical = FlacifyTheme.spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = FlacifyTheme.typography.heading2,
                        color = FlacifyTheme.colors.primaryText,
                        maxLines = 1
                    )
                    Text(
                        text = song.artistName,
                        style = FlacifyTheme.typography.artistName,
                        color = FlacifyTheme.colors.secondaryText,
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { lrcPickerLauncher.launch("*/*") }) {
                        Icon(
                            imageVector = FlacifyIcons.Lyrics,
                            contentDescription = "Import lyrics file",
                            tint = FlacifyTheme.colors.tertiaryText,
                            modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = FlacifyIcons.Close,
                            contentDescription = "Close lyrics",
                            tint = FlacifyTheme.colors.secondaryText,
                            modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                        )
                    }
                }
            }

            when (lyricsUiState) {
                is LyricsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.md)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = FlacifyTheme.colors.accent,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Finding lyrics...",
                                style = FlacifyTheme.typography.bodyMedium,
                                color = FlacifyTheme.colors.secondaryText
                            )
                        }
                    }
                }

                is LyricsUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(FlacifyTheme.spacing.xxl)
                            .navigationBarsPadding(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.md)
                        ) {
                            Icon(
                                imageVector = FlacifyIcons.Lyrics,
                                contentDescription = null,
                                tint = FlacifyTheme.colors.tertiaryText,
                                modifier = Modifier.size(FlacifyTheme.dimensions.iconHuge)
                            )
                            Text(
                                text = "No lyrics available",
                                style = FlacifyTheme.typography.heading2,
                                color = FlacifyTheme.colors.primaryText
                            )
                            Text(
                                text = lyricsUiState.reason.ifBlank {
                                    "Place an .lrc file in the audio folder or import one directly."
                                },
                                style = FlacifyTheme.typography.bodyMedium,
                                color = FlacifyTheme.colors.secondaryText,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { lrcPickerLauncher.launch("*/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                                shape = FlacifyTheme.shapes.pill
                            ) {
                                Text(
                                    text = "Import .lrc / .txt",
                                    color = FlacifyTheme.colors.primaryText,
                                    style = FlacifyTheme.typography.primaryButton
                                )
                            }

                            OutlinedButton(
                                onClick = onRetry,
                                shape = FlacifyTheme.shapes.pill
                            ) {
                                Icon(
                                    imageVector = FlacifyIcons.Refresh,
                                    contentDescription = null,
                                    tint = FlacifyTheme.colors.secondaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Search Online Again",
                                    color = FlacifyTheme.colors.secondaryText,
                                    style = FlacifyTheme.typography.secondaryContent
                                )
                            }
                        }
                    }
                }

                is LyricsUiState.Success -> {
                    if (lyricsUiState.isInstrumental) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding(),
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
                                    text = "Instrumental Track",
                                    style = FlacifyTheme.typography.heading2,
                                    color = FlacifyTheme.colors.primaryText
                                )
                                Text(
                                    text = "This song has no vocal lyrics.",
                                    style = FlacifyTheme.typography.bodyMedium,
                                    color = FlacifyTheme.colors.secondaryText
                                )
                            }
                        }
                    } else if (lyricsUiState.lyrics.lines.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No lyrics found",
                                style = FlacifyTheme.typography.heading2,
                                color = FlacifyTheme.colors.secondaryText
                            )
                        }
                    } else {
                        // Render Lyrics (Synchronized or Unsynchronized)
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures { _, _ ->
                                        isUserScrolling = true
                                    }
                                },
                            contentPadding = PaddingValues(top = 80.dp, bottom = 180.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            itemsIndexed(lyricsUiState.lyrics.lines) { index, line ->
                                val isActive = index == activeIndex
                                val color = if (lyricsUiState.lyrics.isSynchronized) {
                                    if (isActive) {
                                        FlacifyTheme.colors.primaryText
                                    } else {
                                        FlacifyTheme.colors.tertiaryText.copy(alpha = 0.45f)
                                    }
                                } else {
                                    FlacifyTheme.colors.primaryText
                                }

                                val fontSize = if (lyricsUiState.lyrics.isSynchronized) {
                                    if (isActive) 24.sp else 20.sp
                                } else {
                                    19.sp
                                }

                                val fontWeight = if (isActive && lyricsUiState.lyrics.isSynchronized) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Medium
                                }

                                Text(
                                    text = line.text,
                                    fontSize = fontSize,
                                    fontWeight = fontWeight,
                                    lineHeight = 32.sp,
                                    color = color,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 28.dp)
                                        .clickable {
                                            if (lyricsUiState.lyrics.isSynchronized) {
                                                onSeek(line.timestampMs)
                                                isUserScrolling = false
                                            }
                                        }
                                        .graphicsLayer {
                                            scaleX = if (isActive && lyricsUiState.lyrics.isSynchronized) 1.04f else 1.0f
                                            scaleY = if (isActive && lyricsUiState.lyrics.isSynchronized) 1.04f else 1.0f
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating "Resume Sync" pill when user manually scrolled away
        AnimatedVisibility(
            visible = isUserScrolling && lyrics?.isSynchronized == true,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Button(
                onClick = { isUserScrolling = false },
                colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.surfaceElevated),
                shape = FlacifyTheme.shapes.pill,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(
                    imageVector = FlacifyIcons.Refresh,
                    contentDescription = null,
                    tint = FlacifyTheme.colors.accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Sync with playback",
                    color = FlacifyTheme.colors.primaryText,
                    style = FlacifyTheme.typography.secondaryContent
                )
            }
        }
    }
}
