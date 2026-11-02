package com.aurel.music.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aurel.music.core.designsystem.icon.AurelIcons
import com.aurel.music.core.designsystem.theme.AurelSprings
import com.aurel.music.core.designsystem.theme.AurelTheme
import com.aurel.music.core.model.Song

@Composable
fun SongRow(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = AurelSprings.QuickPress,
        label = "songRowScale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(Unit) {
                while (true) {
                    awaitPointerEventScope {
                        awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        val upOrCancel = waitForUpOrCancellation()
                        isPressed = false
                        if (upOrCancel != null) {
                            onClick()
                        }
                    }
                }
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail artwork
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(AurelTheme.shapes.small)
                .background(AurelTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "content://media/external/audio/albumart/${song.albumId}",
                contentDescription = "${song.title} artwork",
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Crop
            )
            if (song.albumId <= 0L) {
                Icon(
                    imageVector = AurelIcons.MusicNote,
                    contentDescription = null,
                    tint = AurelTheme.colors.tertiaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Artist
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                style = AurelTheme.typography.primaryContentBold,
                color = if (isPlaying) AurelTheme.colors.accent else AurelTheme.colors.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${song.artistName} · ${song.albumTitle}",
                style = AurelTheme.typography.secondaryContent,
                color = AurelTheme.colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Duration
        Text(
            text = song.formattedDuration,
            style = AurelTheme.typography.metadata,
            color = AurelTheme.colors.tertiaryText
        )

        // Overflow More button
        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = AurelIcons.More,
                contentDescription = "Song options",
                tint = AurelTheme.colors.secondaryText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

