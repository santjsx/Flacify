package com.flacify.music.core.ui.component

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.designsystem.theme.bounceClick
import com.flacify.music.core.model.Song

@Composable
fun SongRow(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick(scaleDown = 0.98f, onClick = onClick)
            .padding(horizontal = FlacifyTheme.spacing.md, vertical = FlacifyTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail artwork
        Box(
            modifier = Modifier
                .size(FlacifyTheme.dimensions.songThumbnailSize)
                .clip(FlacifyTheme.shapes.small)
                .background(FlacifyTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "content://media/external/audio/albumart/${song.albumId}",
                contentDescription = "${song.title} artwork",
                modifier = Modifier.size(FlacifyTheme.dimensions.songThumbnailSize),
                contentScale = ContentScale.Crop
            )
            if (song.albumId <= 0L) {
                Icon(
                    imageVector = FlacifyIcons.MusicNote,
                    contentDescription = null,
                    tint = FlacifyTheme.colors.tertiaryText,
                    modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                )
            }
        }

        Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))

        // Title and Artist
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                style = FlacifyTheme.typography.songTitle,
                color = if (isPlaying) FlacifyTheme.colors.accent else FlacifyTheme.colors.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${song.artistName} · ${song.albumTitle}",
                style = FlacifyTheme.typography.artistName,
                color = FlacifyTheme.colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xs))

        // Duration
        Text(
            text = song.formattedDuration,
            style = FlacifyTheme.typography.trackDuration,
            color = FlacifyTheme.colors.tertiaryText
        )

        // Overflow More button
        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.size(FlacifyTheme.dimensions.skipButtonSize)
        ) {
            Icon(
                imageVector = FlacifyIcons.More,
                contentDescription = "Song options",
                tint = FlacifyTheme.colors.secondaryText,
                modifier = Modifier.size(FlacifyTheme.dimensions.iconMedium)
            )
        }
    }
}
