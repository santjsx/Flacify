package com.aurel.music.core.model

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

