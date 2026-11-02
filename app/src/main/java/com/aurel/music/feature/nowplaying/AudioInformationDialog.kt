package com.aurel.music.feature.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aurel.music.core.designsystem.theme.AurelTheme
import com.aurel.music.core.model.Song

@Composable
fun AudioInformationDialog(
    song: Song,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Audio Information",
                style = AurelTheme.typography.sectionTitle,
                color = AurelTheme.colors.primaryText
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow(label = "Title", value = song.title)
                InfoRow(label = "Artist", value = song.artistName)
                InfoRow(label = "Album", value = song.albumTitle)
                if (song.year > 0) {
                    InfoRow(label = "Year", value = song.year.toString())
                }

                Spacer(modifier = Modifier.height(6.dp))
                Divider(color = AurelTheme.colors.divider)
                Spacer(modifier = Modifier.height(6.dp))

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
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = AurelTheme.colors.accent)
            }
        },
        containerColor = AurelTheme.colors.surfaceElevated
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
            style = AurelTheme.typography.secondaryContent,
            color = AurelTheme.colors.secondaryText
        )
        Text(
            text = value,
            style = AurelTheme.typography.primaryContentBold,
            color = AurelTheme.colors.primaryText,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

