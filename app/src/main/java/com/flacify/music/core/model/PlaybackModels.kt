package com.flacify.music.core.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface PlaybackState {
    data object Idle : PlaybackState
    data object Preparing : PlaybackState
    data object Buffering : PlaybackState
    data object Ready : PlaybackState
    data object Playing : PlaybackState
    data object Paused : PlaybackState
    data object Ended : PlaybackState
    data class Error(val message: String, val throwable: Throwable? = null) : PlaybackState
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE;

    fun next(): RepeatMode = when (this) {
        OFF -> ALL
        ALL -> ONE
        ONE -> OFF
    }
}

enum class ShuffleMode {
    OFF,
    ON;

    fun toggle(): ShuffleMode = if (this == OFF) ON else OFF
}

enum class SortOption(val displayName: String) {
    TITLE_ASC("Title (A–Z)"),
    TITLE_DESC("Title (Z–A)"),
    ARTIST("Artist"),
    ALBUM("Album"),
    DURATION("Duration"),
    DATE_ADDED("Recently Added"),
    PLAY_COUNT("Most Played")
}

enum class AlbumSortOption(val displayName: String) {
    TITLE_ASC("Title (A–Z)"),
    TITLE_DESC("Title (Z–A)"),
    ARTIST("Artist"),
    YEAR_DESC("Year (Newest)"),
    TRACK_COUNT_DESC("Track Count")
}

enum class AudioQuality(val badgeText: String) {
    LOSSLESS("LOSSLESS"),
    HI_RES_LOSSLESS("HI-RES LOSSLESS");

    val isHiRes: Boolean get() = this == HI_RES_LOSSLESS
}

@Immutable
data class AudioFormatInfo(
    val codec: String,
    val sampleRateHz: Int,
    val bitDepth: Int,
    val bitrateKbps: Int,
    val isLossless: Boolean
)

