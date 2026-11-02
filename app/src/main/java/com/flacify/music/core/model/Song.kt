package com.flacify.music.core.model

import androidx.compose.runtime.Immutable

/**
 * Immutable domain representation of a local audio track.
 * Decoupled from MediaStore and Room database entities.
 */
@Immutable
data class Song(
    val id: Long,
    val mediaUri: String,
    val title: String,
    val artistId: Long,
    val artistName: String,
    val albumId: Long,
    val albumTitle: String,
    val durationMs: Long,
    val trackNumber: Int = 0,
    val discNumber: Int = 1,
    val year: Int = 0,
    val dateAdded: Long = 0,
    val dateModified: Long = 0,
    val fileSize: Long = 0,
    val mimeType: String = "",
    val bitrate: Int = 0,
    val sampleRate: Int = 0,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0,
    val isFavorite: Boolean = false
) {
    /**
     * URI for resolving local album artwork thumbnail.
     */
    val artworkUri: String
        get() = "content://media/external/audio/albumart/$albumId"

    /**
     * Resolves audiophile quality badge: LOSSLESS (FLAC, WAV, ALAC, AIFF) or HI-RES LOSSLESS (>48kHz / >=1.5Mbps).
     */
    val audioQuality: AudioQuality?
        get() {
            val lowerMime = mimeType.lowercase()
            val lowerUri = mediaUri.lowercase()
            val isFlac = lowerMime.contains("flac") || lowerUri.endsWith(".flac")
            val isWav = lowerMime.contains("wav") || lowerUri.endsWith(".wav")
            val isAlac = lowerMime.contains("alac") || lowerUri.endsWith(".alac")
            val isAiff = lowerMime.contains("aiff") || lowerUri.endsWith(".aiff") || lowerUri.endsWith(".aif")
            val isLosslessCodec = isFlac || isWav || isAlac || isAiff || bitrate >= 800_000

            if (!isLosslessCodec) return null

            return if (sampleRate > 48000 || bitrate >= 1_500_000) {
                AudioQuality.HI_RES_LOSSLESS
            } else {
                AudioQuality.LOSSLESS
            }
        }

    val isLossless: Boolean
        get() = audioQuality != null

    /**
     * Formats duration in standard `mm:ss` or `hh:mm:ss`.
     */
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }
}

