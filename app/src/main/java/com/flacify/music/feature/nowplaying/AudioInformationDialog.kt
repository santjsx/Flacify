package com.flacify.music.feature.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.designsystem.theme.bounceClick
import com.flacify.music.core.model.Song
import com.flacify.music.core.ui.component.FlacifyGlassDialog

@Composable
fun AudioInformationDialog(
    song: Song,
    onDismiss: () -> Unit
) {
    FlacifyGlassDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Audio Information",
                style = FlacifyTheme.typography.heading2,
                color = FlacifyTheme.colors.primaryText
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.xs)
            ) {
                InfoRow(label = "Title", value = song.title)
                InfoRow(label = "Artist", value = song.artistName)
                InfoRow(label = "Album", value = song.albumTitle)
                if (song.year > 0) {
                    InfoRow(label = "Year", value = song.year.toString())
                }

                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xxs))
                HorizontalDivider(color = if (FlacifyTheme.colors.isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f))
                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xxs))

                val format = when {
                    song.mimeType.contains("flac", ignoreCase = true) -> "FLAC (Lossless)"
                    song.mimeType.contains("wav", ignoreCase = true) -> "WAV (Uncompressed PCM)"
                    song.mimeType.contains("ogg", ignoreCase = true) -> "OGG Vorbis"
                    song.mimeType.contains("opus", ignoreCase = true) -> "Opus"
                    song.mimeType.contains("mp4", ignoreCase = true) || song.mimeType.contains("m4a", ignoreCase = true) -> "AAC / ALAC (MP4)"
                    song.mimeType.contains("mpeg", ignoreCase = true) -> "MP3"
                    else -> song.mimeType.ifBlank { "Audio Track" }
                }
                InfoRow(label = "Format", value = format)

                if (song.sampleRate > 0) {
                    InfoRow(label = "Sample Rate", value = "${song.sampleRate} Hz")
                }
                if (song.bitrate > 0) {
                    InfoRow(label = "Bitrate", value = "${song.bitrate / 1000} kbps")
                }
                if (song.fileSize > 0) {
                    val mb = "%.2f MB".format(song.fileSize / (1024.0 * 1024.0))
                    InfoRow(label = "File Size", value = mb)
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(FlacifyTheme.shapes.medium)
                    .background(FlacifyTheme.colors.accent)
                    .bounceClick { onDismiss() }
                    .padding(horizontal = FlacifyTheme.spacing.lg, vertical = 10.dp)
            ) {
                Text(
                    text = "Close",
                    color = Color.White,
                    style = FlacifyTheme.typography.primaryButton
                )
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = FlacifyTheme.typography.caption,
            color = FlacifyTheme.colors.secondaryText
        )
        Text(
            text = value,
            style = FlacifyTheme.typography.bodyMedium,
            color = FlacifyTheme.colors.primaryText,
            modifier = Modifier.padding(start = FlacifyTheme.spacing.sm)
        )
    }
}
